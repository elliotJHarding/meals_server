package com.harding.meals.dto.plan;

import com.harding.meals.dto.meal.ingredient.IngredientDto;

public record ShoppingListItemDto(
        Long id,
        IngredientDto ingredient,
        boolean checked
) { }
