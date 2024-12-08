package com.harding.meals.entity.meal;

import com.harding.meals.entity.user.AppUser;
import jakarta.persistence.*;

@Entity
public class MealTag {
    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @ManyToOne
    private AppUser user;

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

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }
}
