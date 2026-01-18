package com.harding.meals.mapping;

import com.harding.meals.dto.MealDto;
import com.harding.meals.entity.meal.Meal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {ImageMapper.class})
public interface MealMapper {
    MealMapper INSTANCE = Mappers.getMapper(MealMapper.class);

    MealDto toDto(Meal meal);

    @Mapping(target = "user", ignore = true)
    Meal toEntity(MealDto dto);
}

