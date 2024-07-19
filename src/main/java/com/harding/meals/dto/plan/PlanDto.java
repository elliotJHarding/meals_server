package com.harding.meals.dto.plan;

import com.harding.meals.dto.DataTransferObject;
import com.harding.meals.dto.meal.MealDto;

import java.util.Date;

public record PlanDto (
    Integer id,
    Date date,
    MealDto dinner
) implements DataTransferObject {}
