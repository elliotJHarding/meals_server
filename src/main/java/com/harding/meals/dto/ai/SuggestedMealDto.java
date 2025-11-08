package com.harding.meals.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.harding.meals.dto.meal.MealDto;

public record SuggestedMealDto(
        MealDto meal,
        int rank,
        @JsonProperty("suitability_score")
        Double suitabilityScore
) {
}
