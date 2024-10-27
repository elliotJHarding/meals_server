package com.harding.meals.entity.shopping;

import com.harding.meals.entity.meal.ingredient.Ingredient;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.time.LocalDate;

public class ShoppingListItem {

    @Id
    @GeneratedValue
    private Long id;
    private LocalDate date;
    @ManyToOne
    private Ingredient ingredient;
}
