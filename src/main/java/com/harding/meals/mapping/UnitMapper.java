package com.harding.meals.mapping;

import com.harding.meals.dto.meal.ingredient.UnitDto;
import com.harding.meals.entity.meal.ingredient.Unit;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UnitMapper {
    UnitMapper INSTANCE = Mappers.getMapper(UnitMapper.class);

    UnitDto toDto(Unit ingredient);

    Unit toEntity(UnitDto dto);
}
