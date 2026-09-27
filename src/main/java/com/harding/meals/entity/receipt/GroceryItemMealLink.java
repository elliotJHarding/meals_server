package com.harding.meals.entity.receipt;

import com.harding.meals.entity.meal.Meal;
import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * Evidence that a grocery item was bought for a meal in a given week.
 * Kept as raw purchase history for the anticipation phase even after
 * ingestion has written the meal's ingredient list.
 */
@Entity
@Table(indexes = {
        @Index(name = "idx_gi_meal_link_item", columnList = "grocery_item_id"),
        @Index(name = "idx_gi_meal_link_meal", columnList = "meal_id")
})
public class GroceryItemMealLink {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "grocery_item_id", nullable = false)
    private GroceryItem groceryItem;

    @ManyToOne
    @JoinColumn(name = "meal_id", nullable = false)
    private Meal meal;

    private LocalDate planDate;

    private String confidence;

    public GroceryItemMealLink() {
    }

    public GroceryItemMealLink(GroceryItem groceryItem, Meal meal, LocalDate planDate, String confidence) {
        this.groceryItem = groceryItem;
        this.meal = meal;
        this.planDate = planDate;
        this.confidence = confidence;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public GroceryItem getGroceryItem() {
        return groceryItem;
    }

    public void setGroceryItem(GroceryItem groceryItem) {
        this.groceryItem = groceryItem;
    }

    public Meal getMeal() {
        return meal;
    }

    public void setMeal(Meal meal) {
        this.meal = meal;
    }

    public LocalDate getPlanDate() {
        return planDate;
    }

    public void setPlanDate(LocalDate planDate) {
        this.planDate = planDate;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }
}
