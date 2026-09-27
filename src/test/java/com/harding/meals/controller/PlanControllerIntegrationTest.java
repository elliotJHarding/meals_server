package com.harding.meals.controller;

import com.harding.meals.base.BaseControllerTest;
import com.harding.meals.entity.meal.Effort;
import com.harding.meals.entity.meal.Meal;
import com.harding.meals.entity.plan.Plan;
import com.harding.meals.entity.plan.PlanMeal;
import com.harding.meals.entity.user.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for PlanController free-text plan meals: entries typed
 * into the week view exist with only freeText until ingestion links a meal.
 */
class PlanControllerIntegrationTest extends BaseControllerTest {

    private AppUser user;
    private Meal libraryMeal;

    @BeforeEach
    void setUp() {
        super.baseSetUp();
        user = createTestUser("planner@test.com", "Planner");

        libraryMeal = new Meal();
        libraryMeal.setName("Stew");
        libraryMeal.setEffort(Effort.LOW);
        libraryMeal.setUser(user);
        libraryMeal.setIngredients(new HashSet<>());
        libraryMeal = mealRepository.save(libraryMeal);
    }

    @Test
    void createPlan_withFreeTextEntries_persistsThem() throws Exception {
        String body = """
                {
                  "date": "2025-03-10",
                  "planMeals": [
                    {"freeText": "Pasta bake"},
                    {"freeText": "Soup and rolls"}
                  ]
                }
                """;

        mockMvc.perform(authenticatedPost("/plans", user).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.planMeals", hasSize(2)))
                .andExpect(jsonPath("$.planMeals[*].freeText",
                        containsInAnyOrder("Pasta bake", "Soup and rolls")));

        List<PlanMeal> saved = planRepository
                .findByDateBetweenAndUser(LocalDate.of(2025, 3, 10), LocalDate.of(2025, 3, 10), user)
                .get(0).getPlanMeals();
        assertEquals(2, saved.size());
        assertNull(saved.get(0).getMeal());
        assertNull(saved.get(0).getRequiredServings());
    }

    @Test
    void getPlans_returnsFreeTextEntries() throws Exception {
        Plan plan = new Plan();
        plan.setDate(LocalDate.of(2025, 3, 12));
        plan.setUser(user);
        PlanMeal entry = new PlanMeal();
        entry.setPlan(plan);
        entry.setFreeText("Takeaway");
        plan.setPlanMeals(new ArrayList<>(List.of(entry)));
        planRepository.save(plan);

        mockMvc.perform(authenticatedGet("/plans/2025-03-10/2025-03-16", user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(7)))
                .andExpect(jsonPath("$[2].planMeals[0].freeText", is("Takeaway")));
    }

    @Test
    void updatePlan_withFreeTextOnlyEntries_doesNotRequireMeal() throws Exception {
        Plan plan = new Plan();
        plan.setDate(LocalDate.of(2025, 3, 10));
        plan.setUser(user);
        plan.setPlanMeals(new ArrayList<>());
        Long planId = planRepository.save(plan).getId();

        String body = """
                {
                  "id": %d,
                  "date": "2025-03-10",
                  "planMeals": [{"freeText": "Leftovers"}]
                }
                """.formatted(planId);

        mockMvc.perform(authenticatedPut("/plans/" + planId, user).content(body))
                .andExpect(status().isOk());

        List<PlanMeal> saved = planRepository.findById(planId).orElseThrow().getPlanMeals();
        assertEquals(1, saved.size());
        assertEquals("Leftovers", saved.get(0).getFreeText());
        assertNull(saved.get(0).getMeal());
    }

    @Test
    void updatePlan_withMixedFreeTextAndLinkedMeal_resolvesTheLinkedMeal() throws Exception {
        Plan plan = new Plan();
        plan.setDate(LocalDate.of(2025, 3, 11));
        plan.setUser(user);
        plan.setPlanMeals(new ArrayList<>());
        Long planId = planRepository.save(plan).getId();

        String body = """
                {
                  "id": %d,
                  "date": "2025-03-11",
                  "planMeals": [
                    {"freeText": "Something new"},
                    {"meal": {"id": %d, "name": "Stew"}, "requiredServings": 4}
                  ]
                }
                """.formatted(planId, libraryMeal.getId());

        mockMvc.perform(authenticatedPut("/plans/" + planId, user).content(body))
                .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(authenticatedGet("/plans/2025-03-11/2025-03-11", user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].planMeals", hasSize(2)))
                .andExpect(jsonPath("$[0].planMeals[*].freeText", hasItem("Something new")))
                .andExpect(jsonPath("$[0].planMeals[*].meal.name", hasItem("Stew")))
                .andReturn();

        assertEquals(200, result.getResponse().getStatus());
    }
}
