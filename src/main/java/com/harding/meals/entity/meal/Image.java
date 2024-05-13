package com.harding.meals.entity.meal;

import com.harding.meals.entity.meal.Meal;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity
public class Image {

    @Id
    @GeneratedValue
    private Long id;

    private String url;

    @OneToOne(mappedBy = "image")
    private Meal meal;

    public Image() {

    }

    public Image(Long id, String url, Meal meal) {
        this.id = id;
        this.url = url;
        this.meal = meal;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Meal getMeal() {
        return meal;
    }

    public void setMeal(Meal meal) {
        this.meal = meal;
    }
}
