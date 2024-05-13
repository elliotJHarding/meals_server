package com.harding.meals.entity.meal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Recipe {

    @Id
    @GeneratedValue
    private Long id;

    private String url;
    private String title;
    @ManyToOne
    private Image image;

    public Recipe(Long id, String url, String title, Image image) {
        this.id = id;
        this.url = url;
        this.title = title;
        this.image = image;
    }

    public Recipe() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }
}
