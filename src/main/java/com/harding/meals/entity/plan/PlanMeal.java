package com.harding.meals.entity.plan;

import com.harding.meals.entity.meal.Meal;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(
        uniqueConstraints = {@UniqueConstraint(columnNames = {"plan_id", "meal_id"})}
)
public class PlanMeal {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @ManyToOne
    @JoinColumn(name = "meal_id", nullable = false)
    private Meal meal;

    @ColumnDefault("false")
    private boolean leftovers;

    @Column(nullable = false)
    private Integer requiredServings;

    private String note;

    public PlanMeal() {
    }

    public PlanMeal(Plan plan, Meal meal, Integer requiredServings) {
        this.plan = plan;
        this.meal = meal;
        this.requiredServings = requiredServings;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public Meal getMeal() {
        return meal;
    }

    public void setMeal(Meal meal) {
        this.meal = meal;
    }

    public Integer getRequiredServings() {
        return requiredServings;
    }

    public void setRequiredServings(Integer requiredServings) {
        this.requiredServings = requiredServings;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isLeftovers() {
        return leftovers;
    }

    public void setLeftovers(boolean leftovers) {
        this.leftovers = leftovers;
    }
}