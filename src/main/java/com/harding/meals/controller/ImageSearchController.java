package com.harding.meals.controller;

import com.harding.meals.dto.image.ImageSearchResponse;
import com.harding.meals.entity.user.AppUser;
import com.harding.meals.service.image.ImageSearchService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class ImageSearchController {

    private final ImageSearchService imageSearchService;

    public ImageSearchController(ImageSearchService imageSearchService) {
        this.imageSearchService = imageSearchService;
    }

    @GetMapping("/images/search")
    public ImageSearchResponse searchImages(
            @RequestParam String query,
            @AuthenticationPrincipal AppUser user) {
        try {
            if (query == null || query.trim().isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Query parameter is required"
                );
            }
            return imageSearchService.searchImages(query);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to search images: " + e.getMessage(),
                    e
            );
        }
    }
}
