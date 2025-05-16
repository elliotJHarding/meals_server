package com.harding.meals.service.calendar.provider;

import com.harding.meals.entity.user.AppUser;
import com.harding.meals.service.calendar.Calendar;
import com.harding.meals.service.calendar.CalendarEvent;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.List;

public interface CalendarProvider {

    String getAuthorizationUrl(AppUser principal) throws IOException;

    List<Calendar> getCalendars(AppUser principal) throws GeneralSecurityException, IOException;

    List<CalendarEvent> getEvents(AppUser principal, String calendarId, LocalDate from, LocalDate to) throws IOException;

}
