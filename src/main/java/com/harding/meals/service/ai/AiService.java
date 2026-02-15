package com.harding.meals.service.ai;

import com.google.api.client.auth.oauth2.Credential;
import com.harding.meals.dto.*;
import com.harding.meals.entity.meal.Effort;
import com.harding.meals.entity.meal.Meal;
import com.harding.meals.entity.meal.Recipe;
import com.harding.meals.entity.meal.ingredient.Ingredient;
import com.harding.meals.entity.meal.ingredient.IngredientMetadata;
import com.harding.meals.entity.meal.ingredient.Longevity;
import com.harding.meals.entity.user.AppUser;
import com.harding.meals.properties.AiServiceProperties;
import com.harding.meals.service.auth.google.GoogleAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.HashSet;

import org.springframework.http.client.JdkClientHttpRequestFactory;

@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private final RestClient restClient;
    private final AiServiceProperties aiServiceProperties;
    private final GoogleAuthService googleAuthService;

    public AiService(AiServiceProperties aiServiceProperties, RestClient.Builder restClientBuilder, GoogleAuthService googleAuthService) {
        this.aiServiceProperties = aiServiceProperties;
        this.googleAuthService = googleAuthService;

        // Use HTTP/1.1 to avoid issues with HTTP/2 upgrade and chunked encoding
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);

        this.restClient = restClientBuilder
                .baseUrl(aiServiceProperties.getBaseUrl())
                .requestFactory(requestFactory)
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
            ingredient.setName(response.getName());

            if (response.getAmount() != null) {
                try {
                    ingredient.setAmount(Double.parseDouble(response.getAmount()));
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
            metadata.setName(response.getIngredientName());

            // Map storage type to Longevity enum
            Longevity longevity = switch (response.getStorageType()) {
                case CUPBOARD -> Longevity.CUPBOARD;
                case FRESH -> Longevity.FRESH;
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
            meal.setName(response.getTitle() != null ? response.getTitle() : "Untitled Recipe");
            meal.setDescription(response.getDescription());
            meal.setPrepTimeMinutes(response.getTotalTimeMinutes());
            meal.setEffort(response.getEffort() != null ?
                Effort.valueOf(response.getEffort().name()) : Effort.MEDIUM);
            meal.setUser(user);

            // Create and set recipe
            Recipe recipe = new Recipe();
            recipe.setUrl(response.getUrl() != null ? response.getUrl().toString() : null);
            recipe.setTitle(response.getTitle());
            meal.setRecipe(recipe);

            // Parse ingredients
            if (response.getIngredients() != null && !response.getIngredients().isEmpty()) {
                HashSet<Ingredient> ingredients = new HashSet<>();
                int index = 0;

                for (ParsedIngredient parsedIngredient : response.getIngredients()) {
                    Ingredient ingredient = new Ingredient();
                    ingredient.setName(parsedIngredient.getName());
                    ingredient.setIndex(index++);
                    ingredient.setMeal(meal);

                    if (parsedIngredient.getAmount() != null) {
                        try {
                            ingredient.setAmount(Double.parseDouble(parsedIngredient.getAmount()));
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

    public SuggestIngredientsResponse suggestIngredients(AppUser user, SuggestIngredientsRequest request) {
        try {
            log.info("Sending suggest ingredients request for user: {}", user.getEmail());
            String accessToken = getAccessToken(user);

            SuggestIngredientsResponse response = restClient.post()
                    .uri("/suggest-ingredients")
                    .header("Authorization", "Bearer " + accessToken)
                    .body(request)
                    .retrieve()
                    .body(SuggestIngredientsResponse.class);

            if (response == null) {
                throw new RuntimeException("Failed to suggest ingredients: empty response");
            }

            log.info("Received {} ingredient suggestions", response.getIngredients().size());
            return response;
        } catch (IOException e) {
            log.error("Failed to get OAuth token for user: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to get OAuth token for user", e);
        } catch (Exception e) {
            log.error("Error in suggestIngredients: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to suggest ingredients", e);
        }
    }

    public DayMealPlanChatResponse planMealChat(AppUser user, DayMealPlanChatRequest request) {
        try {
            log.info("Sending meal plan chat request for user: {}", user.getEmail());
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

            log.info("Received response with {} suggestions", response.getSuggestions().size());

            // Return the response directly - frontend will lookup full meal details by ID
            return response;
        } catch (IOException e) {
            log.error("Failed to get OAuth token for user: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to get OAuth token for user", e);
        } catch (Exception e) {
            log.error("Error in planMealChat: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate meal plan chat", e);
        }
    }

}