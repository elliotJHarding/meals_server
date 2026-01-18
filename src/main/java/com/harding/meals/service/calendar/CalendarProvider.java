package com.harding.meals.service.calendar;

import com.harding.meals.dto.Calendar;
import com.harding.meals.dto.CalendarEventDto;
import com.harding.meals.entity.user.AppUser;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.List;

public interface CalendarProvider {

    List<Calendar> getCalendars(AppUser principal) throws GeneralSecurityException, IOException;

    List<CalendarEventDto> getEvents(AppUser principal, String calendarId, LocalDate from, LocalDate to) throws IOException;

}
