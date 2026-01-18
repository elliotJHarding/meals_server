package com.harding.meals.service.calendar;

import com.harding.meals.dto.Calendar;
import com.harding.meals.dto.CalendarEventDto;
import com.harding.meals.entity.user.ActiveCalendar;
import com.harding.meals.entity.user.AppUser;
import com.harding.meals.repository.AccessTokenRepository;
import com.harding.meals.repository.ActiveCalendarRepository;
import com.harding.meals.service.auth.google.GoogleAuthService;
import com.harding.meals.service.calendar.google.GoogleCalendarProvider;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CalendarService {

    private final GoogleCalendarProvider googleCalendarProvider;
    private final AccessTokenRepository accessTokenRepository;
    private final ActiveCalendarRepository activeCalendarRepository;
    private final GoogleAuthService googleAuthService;

    public CalendarService(GoogleCalendarProvider googleCalendarProvider, AccessTokenRepository accessTokenRepository, ActiveCalendarRepository activeCalendarRepository, GoogleAuthService googleAuthService) {
        this.googleCalendarProvider = googleCalendarProvider;
        this.accessTokenRepository = accessTokenRepository;
        this.activeCalendarRepository = activeCalendarRepository;
        this.googleAuthService = googleAuthService;
    }

    public List<Calendar> findAllCalendars(AppUser user) throws IOException {
        List<String> activeCalendarIds = activeCalendarRepository.findByUser(user).stream()
                .map(ActiveCalendar::getCalendarId).toList();
        return googleCalendarProvider.getCalendars(user).stream().map(calendar ->
                new Calendar()
                        .id(calendar.getId())
                        .name(calendar.getName())
                        .colour(calendar.getColour())
                        .textColour(calendar.getTextColour())
                        .active(activeCalendarIds.contains(calendar.getId()))
        ).toList();
    }

    public List<CalendarEventDto> findAllEvents(AppUser user, LocalDate from, LocalDate to) throws GeneralSecurityException, IOException {
        List<ActiveCalendar> activeCalendars = activeCalendarRepository.findByUser(user);

        List<Calendar> calendars = googleCalendarProvider.getCalendars(user);

        List<String> calendarIds = calendars.stream()
                .map(Calendar::getId)
                .toList();

        activeCalendarRepository.deleteAll(
                activeCalendars.stream()
                        .filter(activeCalendar -> !calendarIds.contains(activeCalendar.getCalendarId()))
                        .toList()
        );

        Map<String, String> colourMap = calendars.stream()
                .collect(Collectors.toMap(Calendar::getId, Calendar::getColour));

        return activeCalendars.stream()
                .flatMap(activeCalendar ->
                        googleCalendarProvider.getEvents(
                                user, activeCalendar.getCalendarId(),
                                from, to
                        ).stream()
                                .map(calendarEvent -> calendarEvent.colour(colourMap.get(activeCalendar.getCalendarId())))
                ).toList();
    }

    public String getAuthorizationUrl(AppUser user) throws IOException {
        return googleAuthService.getAuthorizationUrl(user);
    }

    public void linkCalendar(String authCode, AppUser user) throws IOException {
        googleAuthService.authorize(authCode, user);
    }

    public boolean isAuthorized(AppUser user) {
        return accessTokenRepository.existsById(user.getEmail());
    }

    public void updateActiveCalendars(AppUser user, List<String> calendarIds) {
        List<ActiveCalendar> activeCalendars = activeCalendarRepository.findByUser(user);
        List<String> activeCalendarIds = activeCalendars.stream()
                        .map(ActiveCalendar::getCalendarId).toList();

        activeCalendarRepository.deleteAll(
                activeCalendars.stream()
                        .filter(activeCalendar ->
                                !calendarIds.contains(activeCalendar.getCalendarId())
                        )
                        .toList()
        );

        activeCalendarRepository.saveAll(
                calendarIds.stream()
                        .filter(calendarId ->
                                !activeCalendarIds.contains(calendarId)
                        )
                        .map(calendarId ->
                            new ActiveCalendar()
                                    .user(user)
                                    .calendarId(calendarId)
                        )
                        .toList()
        );
    }

}
