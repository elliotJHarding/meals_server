package com.harding.meals.service.calendar.google;

import com.google.api.client.auth.oauth2.*;
import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.harding.meals.entity.user.AppUser;
import com.harding.meals.service.auth.google.GoogleAuthService;
import com.harding.meals.dto.calendar.Calendar;
import com.harding.meals.dto.calendar.CalendarEvent;
import com.harding.meals.service.calendar.CalendarProvider;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.*;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;

@Component
public class GoogleCalendarProvider implements CalendarProvider {

    private final GoogleAuthService googleAuthService;

    public GoogleCalendarProvider(GoogleAuthService googleAuthService) {
        this.googleAuthService = googleAuthService;
    }

    @Override
    public List<Calendar> getCalendars(AppUser principal) throws IOException {
        com.google.api.services.calendar.Calendar service = googleAuthService.getCalendarService(principal);

        CalendarList calendars = service.calendarList().list().execute();

        return calendars.getItems().stream().map(calendar ->
                new Calendar(
                        calendar.getId(),
                        nonNull(calendar.getSummaryOverride()) ? calendar.getSummaryOverride() : calendar.getSummary(),
                        calendar.getBackgroundColor(),
                        calendar.getForegroundColor(),
                        false
                )
        ).toList();

    }

    @Override
    public List<CalendarEvent> getEvents(AppUser principal, String calendarId, LocalDate from, LocalDate to) {
        try {
            com.google.api.services.calendar.Calendar service = googleAuthService.getCalendarService(principal);

            Events events = service.events().list(calendarId)
                    .setTimeMin(new DateTime(from.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()))
                    .setTimeMax(new DateTime(to.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()))
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();
            List<Event> items = events.getItems();

            Predicate<Event> multiDayEvent = event ->
                    nonNull(event.getStart().getDate()) &&
                    event.getEnd().getDate().getValue() - event.getStart().getDate().getValue() > 24 * 60 * 60 * 1000;


            return Stream.concat(
                    items.stream().map(googleEvent ->
                        new CalendarEvent()
                                .time(Instant.ofEpochMilli(
                                                        nonNull(googleEvent.getStart().getDateTime()) ? googleEvent.getStart().getDateTime().getValue() :
                                                                nonNull(googleEvent.getStart().getDate()) ? googleEvent.getStart().getDate().getValue() :
                                                                        null
                                                )
                                                .atZone(ZoneId.systemDefault())
                                                .toLocalDateTime()
                                )
                                .allDay(nonNull(googleEvent.getStart().getDate()))
                                .name(googleEvent.getSummary())
                    ),
                    items.stream()
                            .filter(multiDayEvent)
                            .flatMap(event -> {
                                LocalDate start = LocalDate.ofInstant(Instant.ofEpochMilli(event.getStart().getDate().getValue()), ZoneId.systemDefault());
                                LocalDate end = LocalDate.ofInstant(Instant.ofEpochMilli(event.getEnd().getDate().getValue()), ZoneId.systemDefault());
                                int daysBetween = Period.between(start, end).getDays();

                                return IntStream.range(1, daysBetween)
                                        .mapToObj(start::plusDays)
                                        .map(date ->
                                                new CalendarEvent()
                                                        .time(date.atStartOfDay())
                                                        .allDay(true)
                                                        .name(event.getSummary())
                                        );
                            })
            ).toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
