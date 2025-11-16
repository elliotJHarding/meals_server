package com.harding.meals.service.ai;

import com.google.api.client.auth.oauth2.Credential;
import com.harding.meals.dto.ai.*;
import com.harding.meals.dto.meal.MealDto;
import com.harding.meals.entity.meal.Effort;
import com.harding.meals.entity.meal.Meal;
import com.harding.meals.entity.meal.Recipe;
import com.harding.meals.entity.meal.ingredient.Ingredient;
import com.harding.meals.entity.meal.ingredient.IngredientMetadata;
import com.harding.meals.entity.meal.ingredient.Longevity;
import com.harding.meals.entity.user.AppUser;
import com.harding.meals.mapping.MealMapper;
import com.harding.meals.properties.AiServiceProperties;
import com.harding.meals.repository.MealRepository;
import com.harding.meals.service.auth.google.GoogleAuthService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class AiService {

    private final RestClient restClient;
    private final AiServiceProperties aiServiceProperties;
    private final GoogleAuthService googleAuthService;
    private final MealRepository mealRepository;
    private final MealMapper mealMapper;

    public AiService(AiServiceProperties aiServiceProperties, RestClient.Builder restClientBuilder, GoogleAuthService googleAuthService, MealRepository mealRepository, MealMapper mealMapper) {
        this.aiServiceProperties = aiServiceProperties;
        this.googleAuthService = googleAuthService;
        this.mealRepository = mealRepository;
        this.mealMapper = mealMapper;
        this.restClient = restClientBuilder
                .baseUrl(aiServiceProperties.getBaseUrl())
                .build();
    }

    private String getAccessToken(AppUser user) throws IOException {
        Credential credential = googleAuthService.getCredential(user);
        if (credential == null) {
            throw new IllegalStateException("No OAuth credential found for user: " + user.getEmail());
        }
        return credential.getAccessToken();
    }

    public Ingredient parseIngredient(AppUser user, String ingredientText) {
        try {
            String accessToken = getAccessToken(user);

            ParseIngredientRequest request = new ParseIngredientRequest(ingredientText);

            ParseIngredientResponse response = restClient.post()
                    .uri("/parse-ingredient")
                    .header("Authorization", "Bearer " + accessToken)
                    .body(request)
                    .retrieve()
                    .body(ParseIngredientResponse.class);

            if (response == null) {
                throw new RuntimeException("Failed to parse ingredient: empty response");
            }

            Ingredient ingredient = new Ingredient();
            ingredient.setName(response.name());

            if (response.amount() != null) {
                try {
                    ingredient.setAmount(Double.parseDouble(response.amount()));
                } catch (NumberFormatException e) {
                    // If amount is not a simple number (e.g., "1-2"), store as 0
                    ingredient.setAmount(0.0);
                }
            }

            // Unit would need to be looked up or created separately
            // For now, we'll leave it null - you may want to add Unit lookup logic

            return ingredient;
        } catch (IOException e) {
            throw new RuntimeException("Failed to get OAuth token for user", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse ingredient", e);
        }
    }

    public IngredientMetadata getIngredientMetadata(AppUser user, String ingredientName) {
        try {
            String accessToken = getAccessToken(user);

            IngredientMetadataRequest request = new IngredientMetadataRequest(ingredientName);

            IngredientMetadataResponse response = restClient.post()
                    .uri("/ingredient-metadata")
                    .header("Authorization", "Bearer " + accessToken)
                    .body(request)
                    .retrieve()
                    .body(IngredientMetadataResponse.class);

            if (response == null) {
                throw new RuntimeException("Failed to get ingredient metadata: empty response");
            }

            IngredientMetadata metadata = new IngredientMetadata();
            metadata.setName(response.ingredientName());

            // Map storage type to Longevity enum
            Longevity longevity = switch (response.storageType()) {
                case CUPBOARD -> Longevity.CUPBOARD;
                case FRESH -> Longevity.FRESH;
                case FREEZER -> Longevity.FREEZER;
            };
            metadata.setLongevity(longevity);

            return metadata;
        } catch (IOException e) {
            throw new RuntimeException("Failed to get OAuth token for user", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get ingredient metadata", e);
        }
    }

    public Meal parseRecipe(AppUser user, String recipeUrl) {
        try {
            String accessToken = getAccessToken(user);

            ParseRecipeRequest request = new ParseRecipeRequest(recipeUrl);

            ParseRecipeResponse response = restClient.post()
                    .uri("/parse-recipe")
                    .header("Authorization", "Bearer " + accessToken)
                    .body(request)
                    .retrieve()
                    .body(ParseRecipeResponse.class);

            if (response == null) {
                throw new RuntimeException("Failed to parse recipe: empty response");
            }

            Meal meal = new Meal();
            meal.setName(response.title() != null ? response.title() : "Untitled Recipe");
            meal.setDescription(response.description());
            meal.setPrepTimeMinutes(response.totalTimeMinutes());
            meal.setEffort(response.effort() != null ? response.effort() : Effort.MEDIUM);
            meal.setUser(user);

            // Create and set recipe
            Recipe recipe = new Recipe();
            recipe.setUrl(response.url());
            recipe.setTitle(response.title());
            meal.setRecipe(recipe);

            // Parse ingredients
            if (response.ingredients() != null && !response.ingredients().isEmpty()) {
                HashSet<Ingredient> ingredients = new HashSet<>();
                int index = 0;

                for (ParsedIngredientDto parsedIngredient : response.ingredients()) {
                    Ingredient ingredient = new Ingredient();
                    ingredient.setName(parsedIngredient.name());
                    ingredient.setIndex(index++);
                    ingredient.setMeal(meal);

                    if (parsedIngredient.amount() != null) {
                        try {
                            ingredient.setAmount(Double.parseDouble(parsedIngredient.amount()));
                        } catch (NumberFormatException e) {
                            ingredient.setAmount(0.0);
                        }
                    }

                    // Unit would need to be looked up or created
                    // For now, we'll leave it null

                    ingredients.add(ingredient);
                }

                meal.setIngredients(ingredients);
            }

            return meal;
        } catch (IOException e) {
            throw new RuntimeException("Failed to get OAuth token for user", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse recipe", e);
        }
    }

    public DayMealPlanChatResponse planMealChat(AppUser user, DayMealPlanChatRequest request) {
        try {
            String accessToken = getAccessToken(user);

            DayMealPlanChatResponse response = restClient.post()
                    .uri("/chat-meal-plan-day")
                    .header("Authorization", "Bearer " + accessToken)
                    .body(request)
                    .retrieve()
                    .body(DayMealPlanChatResponse.class);

            if (response == null) {
                throw new RuntimeException("Failed to generate meal plan chat: empty response");
            }

            // Enrich the suggested meals with full meal details
            List<SuggestedMealDto> enrichedSuggestions = enrichSuggestedMeals(response.suggestions());

            // Return the response with enriched suggestions
            return new DayMealPlanChatResponse(
                    enrichedSuggestions,
                    response.reasoning(),
                    response.conversationComplete(),
                    response.updatedChatContext()
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to get OAuth token for user", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate meal plan chat", e);
        }
    }

    private List<SuggestedMealDto> enrichSuggestedMeals(List<SuggestedMealDto> suggestions) {
        if (suggestions == null || suggestions.isEmpty()) {
            return suggestions;
        }

        // Fetch all meal IDs and get them from the repository
        List<Long> mealIds = suggestions.stream()
                .map(SuggestedMealDto::mealId)
                .toList();

        // Fetch all meals in a single query
        Iterable<Meal> meals = mealRepository.findAllById(mealIds);
        Map<Long, MealDto> mealDtoMap = StreamSupport.stream(meals.spliterator(), false)
                .collect(Collectors.toMap(
                        Meal::getId,
                        mealMapper::toDto
                ));

        // Enrich each suggestion with the full meal details
        return suggestions.stream()
                .map(suggestion -> new SuggestedMealDto(
                        suggestion.mealName(),
                        suggestion.mealId(),
                        suggestion.rank(),
                        suggestion.suitabilityScore(),
                        mealDtoMap.get(suggestion.mealId())
                ))
                .toList();
    }

}