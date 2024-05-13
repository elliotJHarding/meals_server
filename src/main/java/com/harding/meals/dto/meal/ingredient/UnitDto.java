package com.harding.meals.dto.meal.ingredient;

public record UnitDto (
        Long id,
        String code,
        String shortStem,
        boolean shortSpace,
        boolean shortPluralise,
        String longStem,
        boolean longSpace,
        boolean longPluralise
) {}
