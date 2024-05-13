package com.harding.meals.dto.meal;

import com.harding.meals.dto.DataTransferObject;

public record RecipeDto(
    Long id,
    String url,
    String title,
    ImageDto image
) implements DataTransferObject {
}
