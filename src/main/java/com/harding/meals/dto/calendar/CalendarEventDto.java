package com.harding.meals.dto.calendar;

import java.time.LocalDateTime;

public record CalendarEventDto (
    String name,
    LocalDateTime time,
    String colour,
    String textColour,
    boolean allDay
) {}
