package com.harding.meals.dto.meal;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.harding.meals.dto.DataTransferObject;
import com.harding.meals.dto.meal.ingredient.IngredientDto;
import com.harding.meals.entity.meal.Effort;
import com.harding.meals.entity.meal.MealTag;

import java.util.HashSet;
import java.util.Set;

public record MealDto (
    Long id,
    String name,
    Effort effort,
    ImageDto image,
    String description,
    Integer serves,
    Integer prepTimeMinutes,
    @JsonDeserialize(as = HashSet.class)
    Set<IngredientDto> ingredients,
    RecipeDto recipe,
    @JsonDeserialize(as = HashSet.class)
    Set<MealTag> tags
) implements DataTransferObject {}
