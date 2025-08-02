package com.harding.meals.dto.plan;

import com.harding.meals.dto.DataTransferObject;
import com.harding.meals.dto.meal.MealDto;

public record PlanMealDto(
    Long id,
    MealDto meal,
    Integer requiredServings
) implements DataTransferObject {}