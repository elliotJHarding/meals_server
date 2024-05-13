package com.harding.meals.mapping;

import com.harding.meals.dto.meal.ImageDto;
import com.harding.meals.entity.meal.Image;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ImageMapper {
    ImageMapper INSTANCE = Mappers.getMapper(ImageMapper.class);

    ImageDto toDto(Image image);

    Image toEntity(Image dto);

}
