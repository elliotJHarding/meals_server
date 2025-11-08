package com.harding.meals.dto.plan;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.harding.meals.dto.DataTransferObject;
import com.harding.meals.dto.meal.MealDto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public record PlanDto (
    Long id,
    Date date,
    String note,
    @JsonDeserialize(as = ArrayList.class)
    List<PlanMealDto> planMeals,
    @JsonDeserialize(as = ArrayList.class)
    List<ShoppingListItemDto> shoppingListItems
) implements DataTransferObject {

    public List<MealDto> getMeals() {
        return planMeals != null ?
            planMeals.stream().map(PlanMealDto::meal).toList() :
            List.of();
    }
}
