package com.harding.meals.recipe;

import jakarta.persistence.*;

@Entity
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String name;
    @Enumerated(EnumType.STRING)
    private Effort effort;

}
