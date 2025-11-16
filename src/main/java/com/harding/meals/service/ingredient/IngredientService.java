package com.harding.meals.service.ingredient;

import com.harding.meals.entity.meal.Meal;
import com.harding.meals.entity.meal.ingredient.Ingredient;
import com.harding.meals.entity.meal.ingredient.IngredientMetadata;
import com.harding.meals.entity.meal.ingredient.Longevity;
import com.harding.meals.entity.plan.Plan;
import com.harding.meals.entity.plan.PlanMeal;
import com.harding.meals.entity.shopping.ShoppingListItem;
import com.harding.meals.repository.IngredientMetadataRepository;
import com.harding.meals.repository.MealRepository;
import com.harding.meals.repository.PlanRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
public class IngredientService {

    IngredientMetadataRepository ingredientMetadataRepository;

    public IngredientService(IngredientMetadataRepository ingredientMetadataRepository, MealRepository mealRepository, PlanRepository planRepository) {
        this.ingredientMetadataRepository = ingredientMetadataRepository;
    }

    public IngredientMetadata matchMetadata(Ingredient ingredient) {
        return ingredientMetadataRepository.findByName(ingredient.getName());
    }

    public void enrichWithIngredientMetadata(Meal meal) {
        meal.getIngredients().forEach(ingredient ->
                ingredient.setMetadata(
                       matchMetadata(ingredient)
                )
        );
    }

    public void populatePlanShoppingList(Plan plan) {
        if(isNull(plan)) {
            return;
        }

        List<PlanMeal> planMeals = plan.getPlanMeals();

        // Skip if plan has no meals
        if(planMeals == null || planMeals.isEmpty()) {
            return;
        }

        BiFunction<Ingredient, PlanMeal, Double> calculateAmount = (ingredient, planMeal) -> {
            int serves = nonNull(planMeal.getMeal()) &&
                    nonNull(planMeal.getMeal().getServes())?
                    planMeal.getMeal().getServes() : 3;
            return (ingredient.getAmount() / serves) * planMeal.getRequiredServings();
        };

        if (plan.getShoppingListItems() != null && !plan.getShoppingListItems().isEmpty()) {
            plan.getShoppingListItems().clear();
        }

        // Create shopping list items for all ingredients not already in the list
        List<ShoppingListItem> items = planMeals.stream()
                .filter(planMeal -> planMeal.getMeal().getIngredients() != null)
                .flatMap(planMeal ->
                        planMeal.getMeal().getIngredients().stream()
                                .map(ingredient -> {
                                        ShoppingListItem item = new ShoppingListItem(
                                                ingredient,
                                                planMeal.getMeal(),
                                                isIngredientCheckedByDefault(ingredient),
                                                calculateAmount.apply(ingredient, planMeal)
                                        );
                                        item.setPlan(plan);
                                        return item;
                                })
                )
                .toList();

        if (plan.getShoppingListItems() == null) {
            plan.setShoppingListItems(new ArrayList<>());
        }

        plan.getShoppingListItems().addAll(items);
    }

    private boolean isIngredientCheckedByDefault(Ingredient ingredient) {
        Set<Longevity> notCheckedByDefault = Set.of(Longevity.CUPBOARD);

        boolean ingredientHasMetadata = nonNull(ingredient.getMetadata());


        return !ingredientHasMetadata || !notCheckedByDefault.contains(ingredient.getMetadata().getLongevity());
    }
}
