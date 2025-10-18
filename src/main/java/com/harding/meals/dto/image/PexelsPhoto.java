package com.harding.meals.dto.image;

public record PexelsPhoto(
    long id,
    int width,
    int height,
    String url,
    String photographer,
    String photographer_url,
    int photographer_id,
    String avg_color,
    PexelsPhotoSrc src,
    boolean liked,
    String alt
) {}
