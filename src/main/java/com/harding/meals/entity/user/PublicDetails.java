package com.harding.meals.entity.user;

import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class PublicDetails implements Serializable {
    String name;
    String pictureUrl;
    String locale;
    String familyName;
    String givenName;
    boolean emailVerified;

    public PublicDetails() {}

    public PublicDetails(String name, String pictureUrl, String locale, String familyName, String givenName, boolean emailVerified) {
        this.name = name;
        this.pictureUrl = pictureUrl;
        this.locale = locale;
        this.familyName = familyName;
        this.givenName = givenName;
        this.emailVerified = emailVerified;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
}
