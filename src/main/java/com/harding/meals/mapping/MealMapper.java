package com.harding.meals.mapping;

import com.harding.meals.dto.meal.MealDto;
import com.harding.meals.entity.meal.Meal;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {ImageMapper.class})
public interface MealMapper {
    MealMapper INSTANCE = Mappers.getMapper(MealMapper.class);

    MealDto toDto(Meal meal);

    Meal toEntity(MealDto dto);
}

