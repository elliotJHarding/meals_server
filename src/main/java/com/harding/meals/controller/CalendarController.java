package com.harding.meals.controller;

import com.harding.meals.dto.calendar.CalendarEventDto;
import com.harding.meals.dto.plan.PlanDto;
import com.harding.meals.entity.user.ActiveCalendar;
import com.harding.meals.entity.user.AppUser;
import com.harding.meals.mapping.CalendarEventMapper;
import com.harding.meals.service.calendar.Calendar;
import com.harding.meals.service.calendar.CalendarService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.List;

@RestController
public class CalendarController {

    private final CalendarService calendarService;
    private final CalendarEventMapper calendarEventMapper;

    public CalendarController(CalendarService calendarService, CalendarEventMapper calendarEventMapper) {
        this.calendarService = calendarService;
        this.calendarEventMapper = calendarEventMapper;
    }

    @GetMapping("calendar/authorize")
    public String authorizeCalendar(@AuthenticationPrincipal AppUser principal) throws IOException {
        return calendarService.getAuthorizationUrl(principal);
    }

    @PostMapping("calendar/link")
    public void linkCalendar(@RequestBody String token, @AuthenticationPrincipal AppUser user) throws IOException {
        calendarService.linkCalendar(token, user);
    }

    @GetMapping("calendar/events/{start}/{end}")
    public List<CalendarEventDto> getEvents(@PathVariable LocalDate start, @PathVariable LocalDate end, @AuthenticationPrincipal AppUser user) throws GeneralSecurityException, IOException {
        return calendarService.findAllEvents(user, start, end).stream()
                .map(calendarEventMapper::toDto)
                .toList();
    }

    @GetMapping("calendar")
    public List<Calendar> getCalendars(@AuthenticationPrincipal AppUser principal) throws IOException {
        return calendarService.findAllCalendars(principal);
    }

    @GetMapping("calendar/authorized")
    public boolean isAuthorized(@AuthenticationPrincipal AppUser principal) {
        return calendarService.isAuthorized(principal);
    }

    @PostMapping("calendar/active")
    public void updateActiveCalendars(@RequestBody List<String> activeCalendarIds, @AuthenticationPrincipal AppUser principal) {
        calendarService.updateActiveCalendars(principal, activeCalendarIds);
    }

}
