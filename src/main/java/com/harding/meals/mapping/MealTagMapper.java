package com.harding.meals.mapping;

import com.harding.meals.dto.meal.MealTagDto;
import com.harding.meals.entity.meal.MealTag;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface MealTagMapper {
    MealTagMapper INSTANCE = Mappers.getMapper(MealTagMapper.class);

    MealTagDto toDto(MealTag meal);

    MealTag toEntity(MealTagDto dto);
}
