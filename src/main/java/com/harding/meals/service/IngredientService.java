package com.harding.meals.service;

import com.harding.meals.entity.meal.Meal;
import com.harding.meals.entity.meal.ingredient.Ingredient;
import com.harding.meals.entity.meal.ingredient.IngredientMetadata;
import com.harding.meals.entity.meal.ingredient.Longevity;
import com.harding.meals.entity.plan.Plan;
import com.harding.meals.entity.shopping.ShoppingListItem;
import com.harding.meals.repository.IngredientMetadataRepository;
import com.harding.meals.repository.MealRepository;
import com.harding.meals.repository.PlanRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
public class IngredientService {

    private final MealRepository mealRepository;
    private final PlanRepository planRepository;
    IngredientMetadataRepository ingredientMetadataRepository;

    public IngredientService(IngredientMetadataRepository ingredientMetadataRepository, MealRepository mealRepository, PlanRepository planRepository) {
        this.ingredientMetadataRepository = ingredientMetadataRepository;
        this.mealRepository = mealRepository;
        this.planRepository = planRepository;
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

        List<Meal> meals = plan.getMeals();

        // Skip if plan has no meals
        if(meals == null || meals.isEmpty()) {
            return;
        }

        // Create shopping list items for all ingredients not already in the list
        List<ShoppingListItem> items = meals.stream()
                .flatMap(meal ->
                        meal.getIngredients().stream()
                                .map(ingredient -> {
                                        ShoppingListItem item = new ShoppingListItem(
                                                ingredient,
                                                meal,
                                                isIngredientCheckedByDefault(ingredient)
                                        );
                                        item.setPlan(plan);  // Fix Bug 2: Set plan association
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
