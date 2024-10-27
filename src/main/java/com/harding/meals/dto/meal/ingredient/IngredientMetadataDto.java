package com.harding.meals.dto.meal.ingredient;

import com.harding.meals.entity.meal.ingredient.Longevity;

public record IngredientMetadataDto(
    long id,
    Longevity longevity
) {}
