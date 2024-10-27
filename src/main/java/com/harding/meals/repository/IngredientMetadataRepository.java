package com.harding.meals.repository;

import com.harding.meals.entity.meal.ingredient.IngredientMetadata;
import org.springframework.data.repository.CrudRepository;

public interface IngredientMetadataRepository extends CrudRepository<IngredientMetadata, Long> {

    public IngredientMetadata findByName(String name);

}
