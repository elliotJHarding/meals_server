package com.harding.meals.mapping;

import com.harding.meals.dto.IngredientMetadataDto;
import com.harding.meals.entity.meal.ingredient.IngredientMetadata;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface IngredientMetadataMapper {
    IngredientMetadataMapper INSTANCE = Mappers.getMapper(IngredientMetadataMapper.class);

    IngredientMetadataDto toDto(IngredientMetadata metadata);

    @Mapping(target = "name", ignore = true)
    IngredientMetadata toEntity(IngredientMetadataDto dto);
}
