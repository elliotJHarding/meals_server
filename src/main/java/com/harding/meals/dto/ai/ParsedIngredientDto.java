package com.harding.meals.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ParsedIngredientDto(
        String name,
        String amount,
        String unit,
        @JsonProperty("is_well_formed")
        boolean isWellFormed,
        @JsonProperty("raw_text")
        String rawText
) {
}
