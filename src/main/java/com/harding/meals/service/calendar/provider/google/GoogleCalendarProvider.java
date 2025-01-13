package com.harding.meals.service.calendar.provider.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.harding.meals.entity.user.AppUser;
import com.harding.meals.properties.GoogleOauthProperties;
import com.harding.meals.service.calendar.Calendar;
import com.harding.meals.service.calendar.CalendarEvent;
import com.harding.meals.service.calendar.provider.CalendarProvider;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

@Component
public class GoogleCalendarProvider implements CalendarProvider {

    private static String APPLICATION_NAME = "Meal Planner";

    private static final List<String> SCOPES = List.of(
            CalendarScopes.CALENDAR_READONLY
    );
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    GoogleOauthProperties oauthProperties;
    NetHttpTransport httpTransport;
    LocalServerReceiver receiver;

    public GoogleCalendarProvider(GoogleOauthProperties oauthProperties) throws GeneralSecurityException, IOException {
        this.oauthProperties = oauthProperties;
        this.httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        this.receiver = new LocalServerReceiver.Builder().setPort(8080).build();
    }

    private Credential getCredentials(AppUser principal) throws IOException {
        GoogleClientSecrets clientSecrets = new GoogleClientSecrets();
        GoogleClientSecrets.Details details = new GoogleClientSecrets.Details();
        details.setClientId(oauthProperties.getGoogleClientId());
        details.setClientSecret(oauthProperties.getGoogleClientSecret());
        details.setRedirectUris(List.of(oauthProperties.getRedirectUrl()));
        clientSecrets.setInstalled(details);

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new MemoryDataStoreFactory())
                .setAccessType("offline")
                .build();

        return new AuthorizationCodeInstalledApp(flow, receiver).authorize(principal.getEmail());
    }

    @Override
    public List<Calendar> getCalendars(AppUser principal) throws IOException {

        com.google.api.services.calendar.Calendar service =
                new com.google.api.services.calendar.Calendar.Builder(httpTransport, JSON_FACTORY, getCredentials(principal))
                        .setApplicationName(APPLICATION_NAME)
                        .build();

        // List the next 10 events from the primary calendar.
        DateTime now = new DateTime(System.currentTimeMillis());
        Events events = service.events().list("primary")
                .setMaxResults(10)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        List<Event> items = events.getItems();
        if (items.isEmpty()) {
            System.out.println("No upcoming events found.");
        } else {
            System.out.println("Upcoming events");
            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                if (start == null) {
                    start = event.getStart().getDate();
                }
                System.out.printf("%s (%s)\n", event.getSummary(), start);
            }
        }

        return List.of();
    }

    @Override
    public List<CalendarEvent> getEvents(Calendar calendar, AppUser principal) throws IOException {
        com.google.api.services.calendar.Calendar service =
                new com.google.api.services.calendar.Calendar.Builder(httpTransport, JSON_FACTORY, getCredentials(principal))
                        .setApplicationName(APPLICATION_NAME)
                        .build();

        // List the next 10 events from the primary calendar.
        DateTime now = new DateTime(System.currentTimeMillis());
        Events events = service.events().list("primary")
                .setMaxResults(10)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        List<Event> items = events.getItems();

        return items.stream().map(googleEvent ->
                new CalendarEvent()
                        .time(Instant.ofEpochMilli(googleEvent.getStart().getDateTime().getValue())
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime()
                        )
                        .name(googleEvent.getSummary())
        ).toList();
    }
}
