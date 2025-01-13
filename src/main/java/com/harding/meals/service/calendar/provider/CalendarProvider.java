package com.harding.meals.service.calendar.provider;

import com.harding.meals.entity.user.AppUser;
import com.harding.meals.service.calendar.Calendar;
import com.harding.meals.service.calendar.CalendarEvent;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public interface CalendarProvider {

    List<Calendar> getCalendars(AppUser principal) throws GeneralSecurityException, IOException;

    List<CalendarEvent> getEvents(Calendar calendar, AppUser principal) throws IOException;

}
