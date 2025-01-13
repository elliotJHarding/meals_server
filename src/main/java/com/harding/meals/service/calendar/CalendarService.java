package com.harding.meals.service.calendar;

import com.harding.meals.entity.user.AppUser;
import com.harding.meals.service.calendar.provider.google.GoogleCalendarProvider;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.List;

@Service
public class CalendarService {

    private final GoogleCalendarProvider googleCalendarProvider;

    public CalendarService(GoogleCalendarProvider googleCalendarProvider) {
        this.googleCalendarProvider = googleCalendarProvider;
    }

    public List<CalendarEvent> findAllEvents(AppUser user, LocalDate start, LocalDate end) throws GeneralSecurityException, IOException {
        return googleCalendarProvider.getEvents(null, user);
    }

}
