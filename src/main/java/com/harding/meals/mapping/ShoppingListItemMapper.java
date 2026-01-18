package com.harding.meals.mapping;

import com.harding.meals.dto.ShoppingListItemDto;
import com.harding.meals.entity.shopping.ShoppingListItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ShoppingListItemMapper {
    ShoppingListItemMapper INSTANCE = Mappers.getMapper(ShoppingListItemMapper.class);

    ShoppingListItemDto toDto(ShoppingListItem shoppingListItem);

    @Mapping(target = "meal", ignore = true)
    @Mapping(target = "plan", ignore = true)
    @Mapping(target = "amount", ignore = true)
    ShoppingListItem toEntity(ShoppingListItemDto dto);
}
