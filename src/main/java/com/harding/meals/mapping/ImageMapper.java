package com.harding.meals.mapping;

import com.harding.meals.dto.meal.ImageDto;
import com.harding.meals.entity.meal.Image;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ImageMapper {
    ImageMapper INSTANCE = Mappers.getMapper(ImageMapper.class);

    ImageDto toDto(Image image);

    @Mapping(target = "meal", ignore = true)
    Image toEntity(ImageDto dto);

}
