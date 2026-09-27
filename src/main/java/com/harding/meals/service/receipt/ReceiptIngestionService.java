package com.harding.meals.service.receipt;

import com.harding.meals.dto.*;
import com.harding.meals.entity.meal.Meal;
import com.harding.meals.entity.meal.ingredient.Ingredient;
import com.harding.meals.entity.meal.ingredient.IngredientMetadata;
import com.harding.meals.entity.meal.ingredient.Longevity;
import com.harding.meals.entity.plan.Plan;
import com.harding.meals.entity.plan.PlanMeal;
import com.harding.meals.entity.receipt.GroceryItem;
import com.harding.meals.entity.receipt.GroceryItemMealLink;
import com.harding.meals.entity.receipt.Receipt;
import com.harding.meals.entity.receipt.ReceiptStatus;
import com.harding.meals.entity.user.AppUser;
import com.harding.meals.mapping.ReceiptMapper;
import com.harding.meals.repository.*;
import com.harding.meals.service.ai.AiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

import static java.util.Objects.nonNull;

/**
 * The meals 2.0 ingestion pipeline (see SPEC.md): a receipt upload is parsed
 * into grocery items, items are linked to the week's planned meals, free-text
 * plan entries are resolved to library meals (created if new), and meal
 * ingredient lists grow from the receipt evidence.
 *
 * Tolerant of noisy data by design: meals with no matching purchases are left
 * alone, items that belong to no meal stay unlinked, and the user's typed text
 * is never modified.
 */
@Service
public class ReceiptIngestionService {

    private static final Logger log = LoggerFactory.getLogger(ReceiptIngestionService.class);

    private final EmlTextExtractor emlTextExtractor;
    private final AiService aiService;
    private final ReceiptRepository receiptRepository;
    private final PlanRepository planRepository;
    private final MealRepository mealRepository;
    private final IngredientMetadataRepository ingredientMetadataRepository;
    private final GroceryItemMealLinkRepository groceryItemMealLinkRepository;
    private final ReceiptMapper receiptMapper;

    public ReceiptIngestionService(EmlTextExtractor emlTextExtractor, AiService aiService,
                                   ReceiptRepository receiptRepository, PlanRepository planRepository,
                                   MealRepository mealRepository,
                                   IngredientMetadataRepository ingredientMetadataRepository,
                                   GroceryItemMealLinkRepository groceryItemMealLinkRepository,
                                   ReceiptMapper receiptMapper) {
        this.emlTextExtractor = emlTextExtractor;
        this.aiService = aiService;
        this.receiptRepository = receiptRepository;
        this.planRepository = planRepository;
        this.mealRepository = mealRepository;
        this.ingredientMetadataRepository = ingredientMetadataRepository;
        this.groceryItemMealLinkRepository = groceryItemMealLinkRepository;
        this.receiptMapper = receiptMapper;
    }

    @Transactional
    public ReceiptIngestionResultDto ingest(AppUser user, IngestReceiptRequest request) {
        String receiptText = request.getFormat() == IngestReceiptRequest.FormatEnum.EML
                ? emlTextExtractor.extract(request.getRawContent())
                : request.getRawContent();

        ParseReceiptEmailResponse parsed = aiService.parseReceipt(user, receiptText);

        Receipt receipt = new Receipt(user, receiptText);
        receipt.setOrderReference(parsed.getOrderReference());
        receipt.setOrderDate(parsed.getOrderDate());
        receipt.setTotal(parsed.getTotal());
        receipt.setStatus(ReceiptStatus.PROCESSED);
        parsed.getItems().forEach(itemDto -> receipt.getGroceryItems().add(toEntity(itemDto, receipt)));

        // Persist before linking: evidence links reference the grocery item rows
        receiptRepository.save(receipt);

        ReceiptIngestionResultDto result = new ReceiptIngestionResultDto();

        List<Plan> weekPlans = plansForWeekOf(receipt, user);
        List<PlannedMealRefDto> plannedMeals = plannedMealRefs(weekPlans);

        if (!plannedMeals.isEmpty()) {
            LinkWeekResponse linked = aiService.linkWeek(user,
                    new LinkWeekRequest(plannedMeals, parsed.getItems()));
            applyLinks(user, receipt, weekPlans, linked, result);
            linked.getUnlinkedItems().forEach(result::addUnlinkedItemsItem);
        } else {
            log.info("No planned meals found for week of {}; storing receipt without links", receipt.getOrderDate());
        }

        result.setReceipt(receiptMapper.toDto(receipt));
        return result;
    }

    private GroceryItem toEntity(GroceryItemDto dto, Receipt receipt) {
        GroceryItem item = new GroceryItem();
        item.setReceipt(receipt);
        item.setRawName(dto.getRawName());
        item.setQuantity(dto.getQuantity());
        item.setUnitPrice(dto.getUnitPrice());
        item.setTotalPrice(dto.getTotalPrice());
        item.setStorageGroup(dto.getStorageGroup());
        item.setHousehold(Boolean.TRUE.equals(dto.getHousehold()));
        item.setSubstitutedFrom(dto.getSubstitutedFrom());
        return item;
    }

    private List<Plan> plansForWeekOf(Receipt receipt, AppUser user) {
        LocalDate reference = nonNull(receipt.getOrderDate()) ? receipt.getOrderDate() : LocalDate.now();
        LocalDate monday = reference.minusDays(reference.getDayOfWeek().getValue() - 1);
        return planRepository.findByFamilyGroupAndDateBetween(monday, monday.plusDays(6), user);
    }

    private List<PlannedMealRefDto> plannedMealRefs(List<Plan> plans) {
        return plans.stream()
                .flatMap(plan -> plan.getPlanMeals().stream()
                        .map(planMeal -> new PlannedMealRefDto(plan.getDate(), entryName(planMeal)))
                        .filter(ref -> nonNull(ref.getName())))
                .toList();
    }

    private String entryName(PlanMeal planMeal) {
        if (nonNull(planMeal.getFreeText()) && !planMeal.getFreeText().isBlank()) {
            return planMeal.getFreeText().trim();
        }
        return nonNull(planMeal.getMeal()) ? planMeal.getMeal().getName() : null;
    }

    private void applyLinks(AppUser user, Receipt receipt, List<Plan> weekPlans,
                            LinkWeekResponse linked, ReceiptIngestionResultDto result) {
        List<Meal> libraryMeals = new ArrayList<>(nonNull(user.getFamilyGroup())
                ? mealRepository.findByFamilyGroup(user)
                : mealRepository.findByUser(user));

        for (AiMealLinkDto mealLink : linked.getMealLinks()) {
            Meal meal = findByNameIgnoreCase(libraryMeals, mealLink.getMealName()).orElse(null);
            boolean newMeal = meal == null;
            if (newMeal) {
                meal = new Meal();
                meal.setName(mealLink.getMealName());
                meal.setUser(user);
                meal.setIngredients(new HashSet<>());
                libraryMeals.add(meal);
            }

            int ingredientsAdded = addProposedIngredients(meal, mealLink.getIngredients());
            meal = mealRepository.save(meal);

            linkPlanEntry(weekPlans, mealLink, meal);
            recordEvidence(receipt, mealLink, meal);

            result.addLinkedMealsItem(new LinkedMealDto(mealLink.getMealName())
                    .date(mealLink.getDate())
                    .mealId(meal.getId())
                    .newMeal(newMeal)
                    .ingredientsAdded(ingredientsAdded)
                    .confidence(mealLink.getConfidence())
                    .notes(mealLink.getNotes()));
        }
    }

    private int addProposedIngredients(Meal meal, List<ProposedIngredientDto> proposals) {
        Set<String> existingNames = new HashSet<>();
        meal.getIngredients().forEach(ingredient -> existingNames.add(ingredient.getName().toLowerCase()));

        int added = 0;
        long index = meal.getIngredients().size();
        for (ProposedIngredientDto proposal : proposals) {
            if (!existingNames.add(proposal.getName().toLowerCase())) {
                continue;
            }
            Ingredient ingredient = new Ingredient();
            ingredient.setName(proposal.getName());
            ingredient.setMeal(meal);
            ingredient.setIndex(index++);
            ingredient.setMetadata(findOrCreateMetadata(proposal.getName()));
            meal.getIngredients().add(ingredient);
            added++;
        }
        return added;
    }

    private IngredientMetadata findOrCreateMetadata(String name) {
        IngredientMetadata existing = ingredientMetadataRepository.findByName(name);
        if (nonNull(existing)) {
            return existing;
        }
        IngredientMetadata metadata = new IngredientMetadata();
        metadata.setName(name);
        return ingredientMetadataRepository.save(metadata);
    }

    private void linkPlanEntry(List<Plan> weekPlans, AiMealLinkDto mealLink, Meal meal) {
        if (mealLink.getDate() == null) {
            return;
        }
        weekPlans.stream()
                .filter(plan -> mealLink.getDate().equals(plan.getDate()))
                .flatMap(plan -> plan.getPlanMeals().stream())
                .filter(planMeal -> planMeal.getMeal() == null)
                .filter(planMeal -> mealLink.getMealName().equalsIgnoreCase(entryName(planMeal)))
                .findFirst()
                .ifPresent(planMeal -> {
                    boolean mealAlreadyOnDay = planMeal.getPlan().getPlanMeals().stream()
                            .anyMatch(other -> nonNull(other.getMeal())
                                    && other.getMeal().getId() != null
                                    && other.getMeal().getId().equals(meal.getId()));
                    if (mealAlreadyOnDay) {
                        // (plan_id, meal_id) is unique; leave the duplicate entry as free text
                        return;
                    }
                    planMeal.setMeal(meal);
                    planRepository.save(planMeal.getPlan());
                });
    }

    private void recordEvidence(Receipt receipt, AiMealLinkDto mealLink, Meal meal) {
        Map<String, GroceryItem> itemsByName = new HashMap<>();
        receipt.getGroceryItems().forEach(item -> itemsByName.put(normaliseItemRef(item.getRawName()), item));

        Map<String, Ingredient> mealIngredientsByName = new HashMap<>();
        meal.getIngredients().forEach(ingredient ->
                mealIngredientsByName.put(ingredient.getName().toLowerCase(), ingredient));

        Set<Long> linkedItemIds = new HashSet<>();
        for (ProposedIngredientDto proposal : mealLink.getIngredients()) {
            if (proposal.getFromGroceryItem() == null) {
                continue;
            }
            GroceryItem item = itemsByName.get(normaliseItemRef(proposal.getFromGroceryItem()));
            if (item == null) {
                log.warn("AI referenced unknown grocery item '{}'", proposal.getFromGroceryItem());
                continue;
            }
            if (item.getMetadata() == null) {
                Ingredient ingredient = mealIngredientsByName.get(proposal.getName().toLowerCase());
                if (nonNull(ingredient)) {
                    item.setMetadata(ingredient.getMetadata());
                    if (nonNull(ingredient.getMetadata()) && ingredient.getMetadata().getLongevity() == null) {
                        ingredient.getMetadata().setLongevity(longevityFrom(item.getStorageGroup()));
                    }
                }
            }
            if (item.getId() == null || linkedItemIds.add(item.getId())) {
                groceryItemMealLinkRepository.save(
                        new GroceryItemMealLink(item, meal, mealLink.getDate(), mealLink.getConfidence()));
            }
        }
    }

    private Longevity longevityFrom(String storageGroup) {
        if (storageGroup == null) {
            return null;
        }
        return switch (storageGroup.toLowerCase()) {
            case "fridge" -> Longevity.FRESH;
            case "freezer" -> Longevity.FREEZER;
            case "cupboard" -> Longevity.CUPBOARD;
            default -> null;
        };
    }

    /**
     * The AI sometimes echoes items decorated the way the prompt listed them
     * ("2x Name [Fridge]", quoted) rather than the bare raw name; normalise
     * both sides so evidence matching survives that.
     */
    private String normaliseItemRef(String reference) {
        return reference
                .replaceAll("^\\s*-\\s*", "")
                .replaceAll("^\\d+x\\s+", "")
                .replaceAll("\\s*\\[[^]]*]", "")
                .replaceAll("\\s*\\(x\\d+[^)]*\\)\\s*$", "")
                .replace("\"", "")
                .strip()
                .toLowerCase();
    }

    private Optional<Meal> findByNameIgnoreCase(List<Meal> meals, String name) {
        return meals.stream()
                .filter(meal -> meal.getName() != null && meal.getName().equalsIgnoreCase(name))
                .findFirst();
    }
}
