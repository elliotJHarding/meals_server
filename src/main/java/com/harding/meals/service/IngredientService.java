package com.harding.meals.service;

import com.harding.meals.entity.meal.Meal;
import com.harding.meals.entity.meal.ingredient.Ingredient;
import com.harding.meals.entity.meal.ingredient.IngredientMetadata;
import com.harding.meals.repository.IngredientMetadataRepository;
import org.springframework.stereotype.Service;

@Service
public class IngredientService {

    IngredientMetadataRepository ingredientMetadataRepository;

    public IngredientService(IngredientMetadataRepository ingredientMetadataRepository) {
        this.ingredientMetadataRepository = ingredientMetadataRepository;
    }

    public IngredientMetadata matchMetadata(Ingredient ingredient) {
        return ingredientMetadataRepository.findByName(ingredient.getName());
    }

    public void enrichWithIngredientMetadata(Meal meal) {
        meal.getIngredients().forEach(ingredient ->
                ingredient.setMetadata(
                       matchMetadata(ingredient)
                )
        );
    }
}
