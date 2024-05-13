package com.harding.meals.dto.meal.ingredient;

public record IngredientDto (
    long id,
    String name,
    double amount,
    UnitDto unit,
    long index
) {}

