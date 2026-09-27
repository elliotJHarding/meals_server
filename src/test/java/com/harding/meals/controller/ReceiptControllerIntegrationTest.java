package com.harding.meals.controller;

import com.harding.meals.base.BaseControllerTest;
import com.harding.meals.dto.*;
import com.harding.meals.entity.meal.Meal;
import com.harding.meals.entity.plan.Plan;
import com.harding.meals.entity.plan.PlanMeal;
import com.harding.meals.entity.receipt.GroceryItemMealLink;
import com.harding.meals.entity.user.AppUser;
import com.harding.meals.repository.GroceryItemMealLinkRepository;
import com.harding.meals.repository.ReceiptRepository;
import com.harding.meals.service.ai.AiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the receipt ingestion pipeline with the AI calls
 * mocked: upload -> grocery items persisted -> free-text plan entries resolved
 * to (new) library meals -> ingredients built -> evidence links recorded.
 */
class ReceiptControllerIntegrationTest extends BaseControllerTest {

    @MockitoBean
    private AiService aiService;

    @Autowired
    private ReceiptRepository receiptRepository;

    @Autowired
    private GroceryItemMealLinkRepository groceryItemMealLinkRepository;

    private AppUser user;

    @BeforeEach
    void setUp() {
        super.baseSetUp();
        user = createTestUser("ingest@test.com", "Ingestor");

        Plan tuesday = new Plan();
        tuesday.setDate(LocalDate.of(2026, 6, 2));
        tuesday.setUser(user);
        PlanMeal entry = new PlanMeal();
        entry.setPlan(tuesday);
        entry.setFreeText("Spaghetti Bolognese");
        tuesday.setPlanMeals(new ArrayList<>(List.of(entry)));
        planRepository.save(tuesday);

        when(aiService.parseReceipt(any(), anyString())).thenReturn(parsedReceipt());
        when(aiService.linkWeek(any(), any())).thenReturn(linkedWeek());
    }

    private ParseReceiptEmailResponse parsedReceipt() {
        return new ParseReceiptEmailResponse()
                .orderReference("1234-5678-901")
                .orderDate(LocalDate.of(2026, 6, 1))
                .total(100.86)
                .addItemsItem(new GroceryItemDto("Tesco Lean Beef Steak Mince 5% Fat 500g", 1, false)
                        .storageGroup("Fridge").unitPrice(5.19).totalPrice(5.19))
                .addItemsItem(new GroceryItemDto("Tesco Finest Spaghetti Bucatini 500G", 1, false)
                        .storageGroup("Cupboard").unitPrice(2.15).totalPrice(1.60))
                .addItemsItem(new GroceryItemDto("† Fairy Non Bio Washing Pods 33 Washes 679.8g", 1, true)
                        .storageGroup("Cupboard").unitPrice(9.00).totalPrice(9.00));
    }

    private LinkWeekResponse linkedWeek() {
        return new LinkWeekResponse()
                .addMealLinksItem(new AiMealLinkDto("Spaghetti Bolognese")
                        .date(LocalDate.of(2026, 6, 2))
                        .confidence("high")
                        .addIngredientsItem(new ProposedIngredientDto("beef mince")
                                .fromGroceryItem("Tesco Lean Beef Steak Mince 5% Fat 500g"))
                        // The AI sometimes echoes the decorated prompt line
                        // rather than the bare raw name; matching must survive it
                        .addIngredientsItem(new ProposedIngredientDto("spaghetti")
                                .fromGroceryItem("1x \"Tesco Finest Spaghetti Bucatini 500G\" [Cupboard]"))
                        .addIngredientsItem(new ProposedIngredientDto("canned tomatoes")))
                .addUnlinkedItemsItem(new UnlinkedItemDto("† Fairy Non Bio Washing Pods 33 Washes 679.8g", "household"));
    }

    private static final String UPLOAD_BODY = """
            {"rawContent": "Receipt text here", "format": "TEXT"}
            """;

    @Test
    void ingest_createsMealLinksPlanEntryAndBuildsIngredients() throws Exception {
        mockMvc.perform(authenticatedPost("/receipts", user).content(UPLOAD_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receipt.orderReference", is("1234-5678-901")))
                .andExpect(jsonPath("$.receipt.groceryItems", hasSize(3)))
                .andExpect(jsonPath("$.linkedMeals", hasSize(1)))
                .andExpect(jsonPath("$.linkedMeals[0].newMeal", is(true)))
                .andExpect(jsonPath("$.linkedMeals[0].ingredientsAdded", is(3)))
                .andExpect(jsonPath("$.unlinkedItems[0].category", is("household")));

        // The meal was created in the library with the proposed ingredients
        Meal meal = mealRepository.findByUser(user).stream()
                .filter(m -> m.getName().equals("Spaghetti Bolognese"))
                .findFirst().orElseThrow();
        assertEquals(3, meal.getIngredients().size());

        // The free-text plan entry is now linked, original text preserved
        PlanMeal entry = planRepository
                .findByDateBetweenAndUser(LocalDate.of(2026, 6, 2), LocalDate.of(2026, 6, 2), user)
                .get(0).getPlanMeals().get(0);
        assertEquals("Spaghetti Bolognese", entry.getFreeText());
        assertNotNull(entry.getMeal());
        assertEquals(meal.getId(), entry.getMeal().getId());

        // Purchase evidence rows exist for the two receipt-evidenced ingredients
        List<GroceryItemMealLink> links = StreamSupport
                .stream(groceryItemMealLinkRepository.findAll().spliterator(), false)
                .toList();
        assertEquals(2, links.size());
        assertTrue(links.stream().allMatch(link -> link.getMeal().getId().equals(meal.getId())));
    }

    @Test
    void ingest_matchesExistingLibraryMealWithoutDuplicatingIngredients() throws Exception {
        Meal existing = new Meal();
        existing.setName("spaghetti bolognese");
        existing.setUser(user);
        existing.setIngredients(new HashSet<>());
        mealRepository.save(existing);

        mockMvc.perform(authenticatedPost("/receipts", user).content(UPLOAD_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.linkedMeals[0].newMeal", is(false)));

        assertEquals(1, mealRepository.findByUser(user).size(),
                "Existing meal should be matched case-insensitively, not duplicated");
    }

    @Test
    void ingest_withNoPlannedMeals_storesReceiptWithoutLinks() throws Exception {
        AppUser planlessUser = createTestUser("noplans@test.com", "No Plans");

        mockMvc.perform(authenticatedPost("/receipts", planlessUser).content(UPLOAD_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receipt.groceryItems", hasSize(3)))
                .andExpect(jsonPath("$.linkedMeals", hasSize(0)));

        assertEquals(1, receiptRepository.findByFamilyGroup(planlessUser).size());
    }

    @Test
    void getReceipts_returnsFamilyReceiptsMostRecentFirst() throws Exception {
        mockMvc.perform(authenticatedPost("/receipts", user).content(UPLOAD_BODY))
                .andExpect(status().isOk());

        mockMvc.perform(authenticatedGet("/receipts", user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].total", is(100.86)));
    }

    @Test
    void ingest_rejectsEmptyContent() throws Exception {
        mockMvc.perform(authenticatedPost("/receipts", user)
                        .content("""
                                {"rawContent": "", "format": "TEXT"}
                                """))
                .andExpect(status().isBadRequest());
    }
}
