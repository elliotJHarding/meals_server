package com.harding.meals.dto.image;

import com.harding.meals.dto.DataTransferObject;

import java.util.List;

public record ImageSearchResponse(
    List<String> imageUrls
) implements DataTransferObject {}
