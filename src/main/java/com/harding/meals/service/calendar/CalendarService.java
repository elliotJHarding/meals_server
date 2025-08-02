package com.harding.meals.service.calendar;

import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.harding.meals.entity.user.ActiveCalendar;
import com.harding.meals.entity.user.GoogleOauthToken;
import com.harding.meals.entity.user.AppUser;
import com.harding.meals.repository.AccessTokenRepository;
import com.harding.meals.repository.ActiveCalendarRepository;
import com.harding.meals.service.calendar.provider.google.GoogleCalendarProvider;
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

    public CalendarService(GoogleCalendarProvider googleCalendarProvider, AccessTokenRepository accessTokenRepository, ActiveCalendarRepository activeCalendarRepository) {
        this.googleCalendarProvider = googleCalendarProvider;
        this.accessTokenRepository = accessTokenRepository;
        this.activeCalendarRepository = activeCalendarRepository;
    }

    public List<Calendar> findAllCalendars(AppUser user) throws IOException {
        List<String> activeCalendarIds = activeCalendarRepository.findByUser(user).stream()
                .map(ActiveCalendar::getCalendarId).toList();
        return googleCalendarProvider.getCalendars(user).stream().map(calendar ->
                new Calendar(
                        calendar.id(),
                        calendar.name(),
                        calendar.colour(),
                        calendar.textColour(),
                        activeCalendarIds.contains(calendar.id())
                )
            ).toList();
    }

    public List<CalendarEvent> findAllEvents(AppUser user, LocalDate from, LocalDate to) throws GeneralSecurityException, IOException {
        List<ActiveCalendar> activeCalendars = activeCalendarRepository.findByUser(user);
        Map<String, String> colourMap = googleCalendarProvider.getCalendars(user).stream()
                .collect(Collectors.toMap(Calendar::id, Calendar::colour));

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
        return googleCalendarProvider.getAuthorizationUrl(user);
    }

    public void linkCalendar(String authCode, AppUser user) throws IOException {
        googleCalendarProvider.authorize(authCode, user);
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
