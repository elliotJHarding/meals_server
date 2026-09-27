package com.harding.meals.service.ingredient;

import com.harding.meals.entity.meal.Meal;
import com.harding.meals.entity.meal.ingredient.Ingredient;
import com.harding.meals.entity.plan.Plan;
import com.harding.meals.entity.plan.PlanMeal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for IngredientService shopping list population, in particular
 * that free-text-only plan meals (no linked meal, no servings) are tolerated.
 */
class IngredientServiceTest {

    private IngredientService ingredientService;

    @BeforeEach
    void setUp() {
        ingredientService = new IngredientService(null, null, null);
    }

    @Test
    void populatePlanShoppingList_skipsFreeTextOnlyPlanMeals() {
        Plan plan = planWith(
                freeTextPlanMeal("Takeaway"),
                mealPlanMeal(mealWithOneIngredient(), 4)
        );

        ingredientService.populatePlanShoppingList(plan);

        assertEquals(1, plan.getShoppingListItems().size(),
                "Only the linked meal should contribute shopping items");
    }

    @Test
    void populatePlanShoppingList_withOnlyFreeTextPlanMeals_producesNoItems() {
        Plan plan = planWith(freeTextPlanMeal("Out for dinner"));

        ingredientService.populatePlanShoppingList(plan);

        assertTrue(plan.getShoppingListItems() == null || plan.getShoppingListItems().isEmpty());
    }

    @Test
    void populatePlanShoppingList_toleratesMetadataWithoutLongevity() {
        // Receipt ingestion creates IngredientMetadata before any storage
        // evidence exists; a null longevity must not break list population
        Meal meal = mealWithOneIngredient();
        com.harding.meals.entity.meal.ingredient.IngredientMetadata metadata =
                new com.harding.meals.entity.meal.ingredient.IngredientMetadata();
        metadata.setName("Carrot");
        meal.getIngredients().iterator().next().setMetadata(metadata);
        Plan plan = planWith(mealPlanMeal(meal, 4));

        ingredientService.populatePlanShoppingList(plan);

        assertEquals(1, plan.getShoppingListItems().size());
        assertTrue(plan.getShoppingListItems().get(0).isChecked(),
                "Unknown longevity should default to checked");
    }

    @Test
    void populatePlanShoppingList_defaultsNullRequiredServingsToMealServes() {
        Meal meal = mealWithOneIngredient();
        PlanMeal planMeal = mealPlanMeal(meal, null);
        Plan plan = planWith(planMeal);

        ingredientService.populatePlanShoppingList(plan);

        // amount = (ingredientAmount / serves) * requiredServings; with required
        // servings defaulted to the meal's serves the ingredient amount is unscaled
        assertEquals(2.0, plan.getShoppingListItems().get(0).getAmount());
    }

    private Plan planWith(PlanMeal... planMeals) {
        Plan plan = new Plan();
        plan.setDate(LocalDate.of(2025, 1, 10));
        List<PlanMeal> meals = new ArrayList<>(List.of(planMeals));
        meals.forEach(planMeal -> planMeal.setPlan(plan));
        plan.setPlanMeals(meals);
        return plan;
    }

    private PlanMeal freeTextPlanMeal(String freeText) {
        PlanMeal planMeal = new PlanMeal();
        planMeal.setFreeText(freeText);
        return planMeal;
    }

    private PlanMeal mealPlanMeal(Meal meal, Integer requiredServings) {
        PlanMeal planMeal = new PlanMeal();
        planMeal.setMeal(meal);
        planMeal.setRequiredServings(requiredServings);
        return planMeal;
    }

    private Meal mealWithOneIngredient() {
        Ingredient ingredient = new Ingredient();
        ingredient.setName("Carrot");
        ingredient.setAmount(2.0);

        Meal meal = new Meal();
        meal.setName("Stew");
        meal.setServes(4);
        meal.setIngredients(Set.of(ingredient));
        return meal;
    }
}
