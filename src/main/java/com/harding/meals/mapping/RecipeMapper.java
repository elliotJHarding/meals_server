package com.harding.meals.mapping;

import com.harding.meals.dto.meal.RecipeDto;
import com.harding.meals.entity.meal.Recipe;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface RecipeMapper {
    RecipeMapper INSTANCE = Mappers.getMapper(RecipeMapper.class);

    RecipeDto toDto(Recipe recipe);

    Recipe toEntity(RecipeDto dto);
}
