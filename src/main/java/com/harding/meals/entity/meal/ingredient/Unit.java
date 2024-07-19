package com.harding.meals.entity.meal.ingredient;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Unit {

    @Id
    @GeneratedValue
    Long id;

    String code;

    String shortStem;
    boolean shortSpace;
    boolean shortPluralise;

    String longStem;
    boolean longSpace;
    boolean longPluralise;

    public Unit() {

    }

    public Unit(Long id, String code, String shortStem, boolean shortSpace, boolean shortPluralise, String longStem, boolean longSpace, boolean longPluralise) {
        this.id = id;
        this.code = code;
        this.shortStem = shortStem;
        this.shortSpace = shortSpace;
        this.shortPluralise = shortPluralise;
        this.longStem = longStem;
        this.longSpace = longSpace;
        this.longPluralise = longPluralise;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getShortStem() {
        return shortStem;
    }

    public void setShortStem(String shortStem) {
        this.shortStem = shortStem;
    }

    public boolean isShortSpace() {
        return shortSpace;
    }

    public void setShortSpace(boolean shortSpace) {
        this.shortSpace = shortSpace;
    }

    public boolean isShortPluralise() {
        return shortPluralise;
    }

    public void setShortPluralise(boolean shortPluralise) {
        this.shortPluralise = shortPluralise;
    }

    public String getLongStem() {
        return longStem;
    }

    public void setLongStem(String longStem) {
        this.longStem = longStem;
    }

    public boolean isLongSpace() {
        return longSpace;
    }

    public void setLongSpace(boolean longSpace) {
        this.longSpace = longSpace;
    }

    public boolean isLongPluralise() {
        return longPluralise;
    }

    public void setLongPluralise(boolean longPluralise) {
        this.longPluralise = longPluralise;
    }
}
