package com.harding.meals.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;

public record IngredientMetadataRequest(
        @JsonProperty("ingredient_name")
        String ingredientName
) {
}
