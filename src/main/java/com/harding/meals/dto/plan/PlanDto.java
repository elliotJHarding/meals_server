package com.harding.meals.dto.plan;

import com.harding.meals.dto.DataTransferObject;
import com.harding.meals.dto.meal.MealDto;

import java.util.Date;
import java.util.List;

public record PlanDto (
    Integer id,
    Date date,
    MealDto dinner,
    List<ShoppingListItemDto> shoppingListItems
) implements DataTransferObject {}
