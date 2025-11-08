package com.harding.meals.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;

public record IngredientMetadataResponse(
        @JsonProperty("ingredient_name")
        String ingredientName,
        @JsonProperty("storage_type")
        IngredientStorageType storageType,
        String description
) {
}
