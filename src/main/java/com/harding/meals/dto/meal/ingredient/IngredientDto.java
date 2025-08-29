package com.harding.meals.dto.meal.ingredient;

public record IngredientDto (
    Long id,
    String name,
    double amount,
    UnitDto unit,
    long index,
    IngredientMetadataDto metadata
) {}

