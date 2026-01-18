package com.harding.meals.mapping;

import com.harding.meals.dto.IngredientDto;
import com.harding.meals.entity.meal.ingredient.Ingredient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface IngredientMapper {
    IngredientMapper INSTANCE = Mappers.getMapper(IngredientMapper.class);

    IngredientDto toDto(Ingredient ingredient);

    @Mapping(target = "meal", ignore = true)
    Ingredient toEntity(IngredientDto dto);
}
