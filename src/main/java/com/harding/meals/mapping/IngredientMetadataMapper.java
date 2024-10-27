package com.harding.meals.mapping;

import com.harding.meals.dto.meal.ingredient.IngredientMetadataDto;
import com.harding.meals.entity.meal.ingredient.IngredientMetadata;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface IngredientMetadataMapper {
    IngredientMetadataMapper INSTANCE = Mappers.getMapper(IngredientMetadataMapper.class);

    IngredientMetadataDto toDto(IngredientMetadata metadata);

    IngredientMetadata toEntity(IngredientMetadataDto dto);
}
