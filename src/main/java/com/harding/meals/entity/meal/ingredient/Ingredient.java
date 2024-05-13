package com.harding.meals.entity.meal.ingredient;

import com.harding.meals.entity.meal.Meal;
import jakarta.persistence.*;

@Entity
public class Ingredient {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private double amount;
    @ManyToOne
    private Meal meal;
    @ManyToOne
    private Unit unit;
    private long index;

    public Ingredient() {

    }

    public Ingredient(Long id, String name, double amount, Meal meal, Unit unit, long index) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.meal = meal;
        this.unit = unit;
        this.index = index;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Meal getMeal() {
        return meal;
    }

    public void setMeal(Meal meal) {
        this.meal = meal;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }
}
