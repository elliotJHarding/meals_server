package com.harding.meals.repository;

import com.harding.meals.entity.shopping.ShoppingListItem;
import org.springframework.data.repository.CrudRepository;

public interface ShoppingListItemRepository extends CrudRepository<ShoppingListItem, Long> {
}
