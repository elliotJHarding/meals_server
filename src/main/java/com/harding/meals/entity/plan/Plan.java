package com.harding.meals.entity.plan;

import com.harding.meals.entity.FamilyGroupResource;
import com.harding.meals.entity.meal.Meal;
import com.harding.meals.entity.shopping.ShoppingListItem;
import com.harding.meals.entity.user.AppUser;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.nonNull;

@Entity
@Table(
        uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "date"})}
)
public class Plan implements FamilyGroupResource {
    @Id
    @GeneratedValue
    private Long id;
    LocalDate date;
    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    List<PlanMeal> planMeals;
    @ManyToOne
    AppUser user;
    @OneToMany(mappedBy = "plan", cascade = CascadeType.PERSIST, orphanRemoval = true)
    List<ShoppingListItem> shoppingListItems;
    String note;

    public Plan(LocalDate date, AppUser user) {
        this.date = date;
        this.user = user;
        this.planMeals = Collections.emptyList();
    }

    public Plan() {
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public List<ShoppingListItem> getShoppingListItems() {
        return shoppingListItems;
    }

    public void setShoppingListItems(List<ShoppingListItem> shoppingListItems) {
        this.shoppingListItems = shoppingListItems;
    }

    public Plan withFilteredShoppingListItems() {
        if (nonNull(this.shoppingListItems) && nonNull(this.planMeals)) {  // Fix Bug 4: NPE protection
            List<Long> mealIds = this.planMeals.stream()
                    .map(planMeal -> planMeal.getMeal().getId())
                    .toList();
            this.shoppingListItems = this.shoppingListItems.stream()
                    .filter(item ->
                            nonNull(item.getMeal()) &&
                            mealIds.contains(item.getMeal().getId())
                    )
                    .toList();
        }
        return this;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<PlanMeal> getPlanMeals() {
        return planMeals;
    }

    public void setPlanMeals(List<PlanMeal> planMeals) {
        this.planMeals = planMeals;
    }

    public List<Meal> getMeals() {
        return planMeals != null ? 
            new ArrayList<>(planMeals.stream().map(PlanMeal::getMeal).toList()) : 
            new ArrayList<>();
    }
}
