package com.harding.meals.mapping;

import com.harding.meals.dto.plan.PlanMealDto;
import com.harding.meals.entity.plan.PlanMeal;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {MealMapper.class})
public interface PlanMealMapper {
    PlanMealMapper INSTANCE = Mappers.getMapper(PlanMealMapper.class);

    PlanMealDto toDto(PlanMeal planMeal);

    PlanMeal toEntity(PlanMealDto dto);
}