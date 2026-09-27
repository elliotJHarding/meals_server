package com.harding.meals.mapping;

import com.harding.meals.dto.GroceryItemDto;
import com.harding.meals.dto.ReceiptDto;
import com.harding.meals.entity.receipt.GroceryItem;
import com.harding.meals.entity.receipt.Receipt;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReceiptMapper {

    ReceiptDto toDto(Receipt receipt);

    GroceryItemDto toDto(GroceryItem groceryItem);
}
