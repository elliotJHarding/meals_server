package com.harding.meals.mapping;

import com.harding.meals.dto.plan.PlanDto;
import com.harding.meals.entity.plan.Plan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {PlanMealMapper.class, ShoppingListItemMapper.class})
public interface PlanMapper {
    PlanMapper INSTANCE = Mappers.getMapper(PlanMapper.class);

    PlanDto toDto(Plan plan);

    @Mapping(target = "meals", ignore = true)
    Plan toEntity(PlanDto dto);
}

