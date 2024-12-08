package com.harding.meals.dto.meal;

import com.harding.meals.dto.DataTransferObject;
import com.harding.meals.dto.meal.ingredient.IngredientDto;
import com.harding.meals.entity.meal.Effort;
import com.harding.meals.entity.meal.MealTag;

import java.util.Set;

public record MealDto (
    Long id,
    String name,
    Effort effort,
    ImageDto image,
    String description,
    Integer serves,
    Integer prepTimeMinutes,
    Set<IngredientDto> ingredients,
    RecipeDto recipe,
    Set<MealTag> tags
) implements DataTransferObject {}
