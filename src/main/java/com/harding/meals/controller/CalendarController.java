package com.harding.meals.controller;

import com.harding.meals.dto.calendar.CalendarEventDto;
import com.harding.meals.entity.user.AppUser;
import com.harding.meals.mapping.CalendarEventMapper;
import com.harding.meals.service.calendar.CalendarService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@RestController
public class CalendarController {

    private final CalendarService calendarService;
    private final CalendarEventMapper calendarEventMapper;

    public CalendarController(CalendarService calendarService, CalendarEventMapper calendarEventMapper) {
        this.calendarService = calendarService;
        this.calendarEventMapper = calendarEventMapper;
    }

    @GetMapping("calendar/events")
    public List<CalendarEventDto> getEvents(@AuthenticationPrincipal AppUser principal) throws GeneralSecurityException, IOException {
        return calendarService.findAllEvents(principal, null, null).stream()
                .map(calendarEventMapper::toDto)
                .toList();
    }

}
