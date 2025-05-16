package com.harding.meals.entity.user;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class ActiveCalendar {
    @Id
    @GeneratedValue
    Long id;

    @ManyToOne
    AppUser user;

    String calendarId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public String getCalendarId() {
        return calendarId;
    }

    public void setCalendarId(String calendarId) {
        this.calendarId = calendarId;
    }


    public ActiveCalendar user(AppUser user) {
        this.user = user;
        return this;
    }

    public ActiveCalendar calendarId(String calendarId) {
        this.calendarId = calendarId;
        return this;
    }

}
