package com.harding.meals.service.calendar;

import java.time.LocalDateTime;

public class CalendarEvent {
    private String name;
    private LocalDateTime time;
    private Calendar calendar;
    private String colour;

    public String getName() {
        return name;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public String getColour() {
        return colour;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }

    public CalendarEvent name(String name) {
        this.name = name;
        return this;
    }

    public CalendarEvent time(LocalDateTime time) {
        this.time = time;
        return this;
    }

    public CalendarEvent colour(String colour) {
        this.colour = colour;
        return this;
    }


    public CalendarEvent calendar(Calendar calendar) {
        this.calendar = calendar;
        return this;
    }
}
