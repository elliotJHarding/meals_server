package com.harding.meals.entity.meal.ingredient;

import jakarta.persistence.*;

@Entity
@Table(indexes = {
        @Index(name = "idx_ingredient_metadata_name", columnList = "name")
})
public class IngredientMetadata {

    @Id
    @GeneratedValue
    private Long id;
    private String name;
    @Enumerated(EnumType.STRING)
    private Longevity longevity;

    public IngredientMetadata() {
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Longevity getLongevity() {
        return longevity;
    }

    public void setLongevity(Longevity longevity) {
        this.longevity = longevity;
    }
}
