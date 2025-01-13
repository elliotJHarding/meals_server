package com.harding.meals.service.calendar;

import java.time.LocalDateTime;

public class CalendarEvent {
    private String name;
    private LocalDateTime time;
    private Calendar calendar;

    public String getName() {
        return name;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public Calendar getCalendar() {
        return calendar;
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

    public CalendarEvent name(String name) {
        this.name = name;
        return this;
    }

    public CalendarEvent time(LocalDateTime time) {
        this.time = time;
        return this;
    }

    public CalendarEvent calendar(Calendar calendar) {
        this.calendar = calendar;
        return this;
    }
}
