package com.harding.meals.entity.meal;

import com.harding.meals.entity.user.AppUser;
import com.harding.meals.entity.meal.ingredient.Ingredient;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;

import java.util.Set;

@Entity
public class Meal {

    @Id
    @GeneratedValue
    private Long id;
    private String name;
    @Enumerated(EnumType.STRING)
    private Effort effort;
    @ManyToOne
    private AppUser user;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn
    private Image image;

    @Nullable
    @OneToOne(cascade = CascadeType.ALL)
    private Recipe recipe;

    @Nullable
    private String description;
    @Nullable
    private Integer serves;
    @Nullable
    private Integer prepTimeMinutes;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "meal", orphanRemoval = true)
    private Set<Ingredient> ingredients;

    @ManyToMany
    private Set<MealTag> tags;

    public Meal(Long id, String name, Effort effort, Image image, @Nullable String description, @Nullable Integer serves, @Nullable Integer prepTimeMinutes, Set<Ingredient> ingredients) {
        this.id = id;
        this.name = name;
        this.effort = effort;
        this.image = image;
        this.description = description;
        this.serves = serves;
        this.prepTimeMinutes = prepTimeMinutes;
        this.ingredients = ingredients;
    }

    public Meal() {

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

    public Effort getEffort() {
        return effort;
    }

    public void setEffort(Effort effort) {
        this.effort = effort;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }


    @Nullable
    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(@Nullable Recipe recipe) {
        this.recipe = recipe;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    @Nullable
    public Integer getServes() {
        return serves;
    }

    public void setServes(@Nullable Integer serves) {
        this.serves = serves;
    }

    @Nullable
    public Integer getPrepTimeMinutes() {
        return prepTimeMinutes;
    }

    public void setPrepTimeMinutes(@Nullable Integer prepTimeMinutes) {
        this.prepTimeMinutes = prepTimeMinutes;
    }

    public Set<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(Set<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public Set<MealTag> getTags() {
        return tags;
    }

    public void setTags(Set<MealTag> tags) {
        this.tags = tags;
    }
}
