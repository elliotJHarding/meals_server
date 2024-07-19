package com.harding.meals.entity.plan;

import com.harding.meals.entity.meal.Meal;
import com.harding.meals.entity.user.AppUser;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(
        uniqueConstraints = {@UniqueConstraint(columnNames = {"user", "date"})}
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
}
