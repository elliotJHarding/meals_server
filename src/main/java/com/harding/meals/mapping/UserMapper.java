package com.harding.meals.mapping;

import com.harding.meals.dto.auth.AppUserDto;
import com.harding.meals.entity.user.PublicDetails;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    AppUserDto toDto(PublicDetails userDetails);

}