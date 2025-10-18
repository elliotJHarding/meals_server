package com.harding.meals.dto.image;

import java.util.List;

public record PexelsSearchResponse(
    int total_results,
    int page,
    int per_page,
    List<PexelsPhoto> photos,
    String next_page
) {}
