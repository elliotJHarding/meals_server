package com.harding.meals.controller;

import com.harding.meals.dto.ai.GenerateMealPlanRequest;
import com.harding.meals.dto.plan.PlanDto;
import com.harding.meals.entity.plan.Plan;
import com.harding.meals.entity.shopping.ShoppingListItem;
import com.harding.meals.entity.user.AppUser;
import com.harding.meals.mapping.PlanMapper;
import com.harding.meals.repository.MealRepository;
import com.harding.meals.repository.PlanRepository;
import com.harding.meals.repository.ShoppingListItemRepository;
import com.harding.meals.service.IngredientService;
import com.harding.meals.service.ai.MealPlanGenerationService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.nonNull;

@RestController
public class PlanController {

    private final PlanMapper planMapper;
    private final PlanRepository planRepository;
    private final IngredientService ingredientService;
    private final ShoppingListItemRepository shoppingListItemRepository;
    private final MealPlanGenerationService mealPlanGenerationService;
    private final MealRepository mealRepository;

    public PlanController(PlanRepository planRepository, PlanMapper planMapper, IngredientService ingredientService, ShoppingListItemRepository shoppingListItemRepository, MealPlanGenerationService mealPlanGenerationService, MealRepository mealRepository) {
        this.planRepository = planRepository;
        this.planMapper = planMapper;
        this.ingredientService = ingredientService;
        this.shoppingListItemRepository = shoppingListItemRepository;
        this.mealPlanGenerationService = mealPlanGenerationService;
        this.mealRepository = mealRepository;
    }

    @GetMapping("/plans/{start}/{end}")
    public List<PlanDto> getPlans(@PathVariable LocalDate start, @PathVariable LocalDate end, @AuthenticationPrincipal AppUser user) {
        {
            if (start == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date cannot be null");
            }
            if (end == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date cannot be null");
            }
            if (start.isAfter(end)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date cannot be after end date");
            }
        }

        List<Plan> savedPlans = planRepository.findByFamilyGroupAndDateBetween(start, end, user);

        List<LocalDate> datesBetween = start
                .datesUntil(end.plusDays(1))
                .toList();

        Predicate<LocalDate> dateHasPlan = (date) -> savedPlans.stream()
                .anyMatch(plan -> plan.getDate().equals(date));

        Function<LocalDate, Plan> getPlan = (date) -> savedPlans.stream()
                .filter(plan -> plan.getDate().equals(date))
                .findFirst()
                .orElseThrow();

        Function<LocalDate, Plan> createEmptyPlan = (date) ->
            new Plan(date, user);

        return datesBetween.stream()
            .map(date ->
                dateHasPlan.test(date) ?
                getPlan.apply(date) :
                createEmptyPlan.apply(date))
            .peek(ingredientService::populatePlanShoppingList)
            .map(Plan::withFilteredShoppingListItems)
            .map(planMapper::toDto)
            .toList();
    }

    @PostMapping("/plans")
    PlanDto create(@RequestBody PlanDto mealPlanDto, @AuthenticationPrincipal AppUser user) {
        Plan plan = planMapper.toEntity(mealPlanDto);
        plan.setUser(user);

        plan.getPlanMeals().forEach(planMeal -> planMeal.setPlan(plan));

        ingredientService.populatePlanShoppingList(plan);

        return planMapper.toDto(planRepository.save(plan));
    }

    @PutMapping("/plans/{id}")
    void updateById(@RequestBody PlanDto newPlan, @PathVariable Long id, @AuthenticationPrincipal AppUser user) {
        Plan oldPlan = planRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("Plan does not exist"));

        validateOwnership(user, oldPlan);

        Plan plan = planMapper.toEntity(newPlan);
        plan.getPlanMeals().forEach(planMeal -> {
            planMeal.setPlan(plan);
            planMeal.setMeal(mealRepository.findById(planMeal.getMeal().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Meal with id %s does not exist".formatted(planMeal.getMeal().getId()))));
        });

        plan.setUser(user);
        plan.setId(id);

        ingredientService.populatePlanShoppingList(plan);

        planRepository.save(plan);
    }

    @PostMapping("plans/shoppingList")
    void updateShoppingList(@RequestBody List<PlanDto> planDtos, @AuthenticationPrincipal AppUser user) {
        planDtos.stream()
                .map(planMapper::toEntity)
                .forEach(plan -> {
                    List<ShoppingListItem> shoppingListItems = plan.getShoppingListItems();
                    if (nonNull(shoppingListItems)) {
                        Plan existingPlan = planRepository.findById(plan.getId()).orElse(null);
                        if (nonNull(existingPlan)) {
                            validateOwnership(user, existingPlan);
                            shoppingListItems.forEach(shoppingListItem -> {
                                shoppingListItem.setPlan(existingPlan);
                            });
                            shoppingListItemRepository.saveAll(shoppingListItems);
                        }
                    }
                });
    }

    @DeleteMapping("plans/{date}")
    @Transactional
    void deleteByDate(@PathVariable LocalDate date, @AuthenticationPrincipal AppUser user) {
        planRepository.deleteAllByUserAndDate(user, date);
    }

    private void validateOwnership(AppUser user, long id) {
        Plan plan = planRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("Plan does not exist"));

        validateOwnership(user, plan);
    }

    @PostMapping("/plans/generate")
    public List<PlanDto> generateMealPlan(@RequestBody GenerateMealPlanRequest request, @AuthenticationPrincipal AppUser user) {
        try {
            return mealPlanGenerationService.generateMealPlan(
                request.weekStartDate(),
                request.weekEndDate(),
                request.prompt(),
                user
            );
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Calendar service unavailable", e);
        } catch (GeneralSecurityException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Security error accessing calendar", e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate meal plan", e);
        }
    }

    private void validateOwnership(AppUser user, Plan plan) {
        if (!Objects.equals(plan.getUser().getId(), user.getId())) {
            throw new IllegalArgumentException("User does not own this plan");
        }
    }

}
