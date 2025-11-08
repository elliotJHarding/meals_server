package com.harding.meals.controller;

import com.harding.meals.dto.ai.DayMealPlanChatRequest;
import com.harding.meals.dto.ai.DayMealPlanChatResponse;
import com.harding.meals.entity.meal.Meal;
import com.harding.meals.entity.meal.ingredient.Ingredient;
import com.harding.meals.entity.meal.ingredient.IngredientMetadata;
import com.harding.meals.entity.user.AppUser;
import com.harding.meals.service.ai.AiService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/parse-ingredient")
    public Ingredient parseIngredient(
            @RequestBody ParseIngredientRequestBody request,
            @AuthenticationPrincipal AppUser user
    ) {
        return aiService.parseIngredient(user, request.ingredientText());
    }

    @PostMapping("/ingredient-metadata")
    public IngredientMetadata getIngredientMetadata(
            @RequestBody IngredientMetadataRequestBody request,
            @AuthenticationPrincipal AppUser user
    ) {
        return aiService.getIngredientMetadata(user, request.ingredientName());
    }

    @PostMapping("/parse-recipe")
    public Meal parseRecipe(
            @RequestBody ParseRecipeRequestBody request,
            @AuthenticationPrincipal AppUser user
    ) {
        return aiService.parseRecipe(user, request.recipeUrl());
    }

    @PostMapping("/meal-plan-chat")
    public DayMealPlanChatResponse planMealChat(
            @RequestBody DayMealPlanChatRequest request,
            @AuthenticationPrincipal AppUser user
    ) {
        return aiService.planMealChat(user, request);
    }

    // Request body records for simple endpoints
    public record ParseIngredientRequestBody(String ingredientText) {}
    public record IngredientMetadataRequestBody(String ingredientName) {}
    public record ParseRecipeRequestBody(String recipeUrl) {}
}
