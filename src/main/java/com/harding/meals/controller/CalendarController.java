package com.harding.meals.controller;

import com.harding.meals.dto.CalendarEventDto;
import com.harding.meals.entity.user.AppUser;

import com.harding.meals.dto.Calendar;
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

    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
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
