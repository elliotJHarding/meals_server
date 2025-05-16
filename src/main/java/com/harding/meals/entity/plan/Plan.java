package com.harding.meals.entity.plan;

import com.harding.meals.entity.meal.Meal;
import com.harding.meals.entity.shopping.ShoppingListItem;
import com.harding.meals.entity.user.AppUser;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.nonNull;

@Entity
@Table(
        uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "date"})}
)
public class Plan {
    @Id
    @GeneratedValue
    private Long id;
    LocalDate date;
    @ManyToOne
    Meal dinner;
    @ManyToOne
    AppUser user;
    @OneToMany(mappedBy = "plan", cascade = CascadeType.PERSIST)
    List<ShoppingListItem> shoppingListItems;

    String note;

    public Plan(LocalDate date, Meal dinner, AppUser user) {
        this.date = date;
        this.dinner = dinner;
        this.user = user;
    }

    public Plan(LocalDate date, AppUser user) {
        this.date = date;
        this.dinner = null;
        this.user = user;
    }

    public Plan() {

    }

    public Meal getDinner() {
        return dinner;
    }

    public void setDinner(Meal dinner) {
        this.dinner = dinner;
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
        if (nonNull(this.shoppingListItems)) {
            this.shoppingListItems = this.shoppingListItems.stream()
                    .filter(item ->
                            nonNull(item.getMeal()) &&
                            Objects.equals(item.getMeal().getId(), this.dinner.getId()))
                    .toList();
        }
        return this;
    }
}
