package com.harding.meals.mapping;

import com.harding.meals.dto.PlanMealDto;
import com.harding.meals.entity.plan.PlanMeal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {MealMapper.class})
public interface PlanMealMapper {
    PlanMealMapper INSTANCE = Mappers.getMapper(PlanMealMapper.class);

    PlanMealDto toDto(PlanMeal planMeal);

    @Mapping(target = "plan", ignore = true)
    PlanMeal toEntity(PlanMealDto dto);
}