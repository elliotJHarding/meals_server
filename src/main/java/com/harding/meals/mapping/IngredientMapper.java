package com.harding.meals.mapping;

import com.harding.meals.dto.meal.ingredient.IngredientDto;
import com.harding.meals.entity.meal.ingredient.Ingredient;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface IngredientMapper {
    IngredientMapper INSTANCE = Mappers.getMapper(IngredientMapper.class);

    IngredientDto toDto(Ingredient ingredient);

    Ingredient toEntity(IngredientDto dto);
}
