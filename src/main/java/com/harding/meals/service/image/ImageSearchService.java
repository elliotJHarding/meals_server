package com.harding.meals.service.image;

import com.harding.meals.dto.image.ImageSearchResponse;
import com.harding.meals.dto.image.PexelsSearchResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ImageSearchService {

    private final PexelsServiceClient pexelsServiceClient;

    public ImageSearchService(PexelsServiceClient pexelsServiceClient) {
        this.pexelsServiceClient = pexelsServiceClient;
    }

    public ImageSearchResponse searchImages(String query) {
        PexelsSearchResponse pexelsResponse = pexelsServiceClient.searchImages(query);

        List<String> imageUrls = pexelsResponse.photos().stream()
                .map(photo -> photo.src().large())
                .collect(Collectors.toList());

        return new ImageSearchResponse(imageUrls);
    }
}
