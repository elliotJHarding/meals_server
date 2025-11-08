package com.harding.meals.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ParseIngredientRequest(
        @JsonProperty("ingredient_string")
        String ingredientString
) {
}
