package com.harding.meals.entity.shopping;

import com.harding.meals.entity.meal.Meal;
import com.harding.meals.entity.meal.ingredient.Ingredient;
import com.harding.meals.entity.plan.Plan;
import jakarta.persistence.*;

@Entity
public class ShoppingListItem {

    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    private Ingredient ingredient;
    @ManyToOne
    private Meal meal;
    boolean checked;
    @ManyToOne
    private Plan plan;

    public ShoppingListItem(Ingredient ingredient, Meal meal, boolean checked) {
        this.ingredient = ingredient;
        this.meal = meal;
        this.checked = checked;
    }

    public ShoppingListItem() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    public Meal getMeal() {
        return meal;
    }

    public void setMeal(Meal meal) {
        this.meal = meal;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }
}
