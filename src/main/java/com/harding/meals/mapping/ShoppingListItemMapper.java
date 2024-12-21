package com.harding.meals.mapping;

import com.harding.meals.dto.plan.ShoppingListItemDto;
import com.harding.meals.entity.shopping.ShoppingListItem;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ShoppingListItemMapper {
    ShoppingListItemMapper INSTANCE = Mappers.getMapper(ShoppingListItemMapper.class);

    ShoppingListItemDto toDto(ShoppingListItem shoppingListItem);

    ShoppingListItem toEntity(ShoppingListItemDto dto);
}
