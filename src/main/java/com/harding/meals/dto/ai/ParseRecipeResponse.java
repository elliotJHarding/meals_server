package com.harding.meals.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.harding.meals.entity.meal.Effort;

import java.util.List;

public record ParseRecipeResponse(
        String title,
        String description,
        @JsonProperty("total_time_minutes")
        Integer totalTimeMinutes,
        Effort effort,
        List<ParsedIngredientDto> ingredients,
        String url
) {
}
