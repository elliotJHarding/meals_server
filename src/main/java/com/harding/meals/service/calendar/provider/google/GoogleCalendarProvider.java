package com.harding.meals.service.calendar.provider.google;

import com.google.api.client.auth.oauth2.*;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.harding.meals.entity.user.AppUser;
import com.harding.meals.properties.GoogleOauthProperties;
import com.harding.meals.repository.AccessTokenRepository;
import com.harding.meals.service.calendar.Calendar;
import com.harding.meals.service.calendar.CalendarEvent;
import com.harding.meals.service.calendar.provider.CalendarProvider;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.time.*;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;

@Component
public class GoogleCalendarProvider implements CalendarProvider {

    private static String APPLICATION_NAME = "Meal Planner";

    private static final List<String> SCOPES = List.of(
            CalendarScopes.CALENDAR_READONLY
    );
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private final PersistedDataStoreFactory persistedDataStoreFactory;
    private final AccessTokenRepository accessTokenRepository;

    private GoogleClientSecrets clientSecrets;

    GoogleOauthProperties oauthProperties;
    NetHttpTransport httpTransport;
    LocalServerReceiver receiver;

    GoogleAuthorizationCodeFlow flow;

    public GoogleCalendarProvider(GoogleOauthProperties oauthProperties, PersistedDataStoreFactory persistedDataStoreFactory, AccessTokenRepository accessTokenRepository) throws GeneralSecurityException, IOException {
        this.oauthProperties = oauthProperties;
        this.httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        this.receiver = new LocalServerReceiver.Builder()
                .setPort(8080)
                .build();

        clientSecrets = new GoogleClientSecrets();
        GoogleClientSecrets.Details details = new GoogleClientSecrets.Details();
        details.setClientId(oauthProperties.getGoogleClientId());
        details.setClientSecret(oauthProperties.getGoogleClientSecret());
        details.setRedirectUris(List.of(oauthProperties.getRedirectUrl()));
        clientSecrets.setInstalled(details);
        this.persistedDataStoreFactory = persistedDataStoreFactory;

        this.flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(persistedDataStoreFactory)
                .setAccessType("offline")
                .build();
        this.accessTokenRepository = accessTokenRepository;
    }

    private Credential getCredential(AppUser principal) throws IOException {
        Credential credential = flow.loadCredential(principal.getEmail());
        if (nonNull(credential) && credential.getExpiresInSeconds() > 60) {
            return credential;
        }

        if (nonNull(credential) && nonNull(credential.getRefreshToken())) {
            TokenResponse response = refreshAccessToken(principal.getEmail(), credential.getRefreshToken());
            response.setRefreshToken(credential.getRefreshToken());
            Credential refreshedCredential = flow.createAndStoreCredential(response, principal.getEmail());
            return refreshedCredential;
        }

        return null;
    }

    @Override
    public String getAuthorizationUrl(AppUser principal) throws IOException {

        GoogleAuthorizationCodeRequestUrl url = flow.newAuthorizationUrl();

        url.setRedirectUri(oauthProperties.getRedirectUrl());

        return url.toString();
    }

    public TokenResponse getAccessToken(String authorizationCode, String userId) throws IOException {
        GoogleAuthorizationCodeTokenRequest request =
                flow
                        .newTokenRequest(URLDecoder.decode(authorizationCode))
                        .setRedirectUri(oauthProperties.getRedirectUrl())
                        .setClientAuthentication(new ClientParametersAuthentication(oauthProperties.getGoogleClientId(), oauthProperties.getGoogleClientSecret()))
                        .setGrantType("authorization_code");

        return executeTokenRequest(request, userId);
    }

    public void authorize(String authorizationCode, AppUser user) throws IOException {
        TokenResponse response = getAccessToken(authorizationCode, user.getEmail());

        flow.createAndStoreCredential(response, user.getEmail());
    }

    public TokenResponse refreshAccessToken(String userEmail, String refreshToken) throws IOException {
        GoogleRefreshTokenRequest request = new GoogleRefreshTokenRequest(
                httpTransport, flow.getJsonFactory(), refreshToken, oauthProperties.getGoogleClientId(), oauthProperties.getGoogleClientSecret()
        );

        return executeTokenRequest(request, userEmail);
    }

    @Override
    public List<Calendar> getCalendars(AppUser principal) throws IOException {
        com.google.api.services.calendar.Calendar service =
                new com.google.api.services.calendar.Calendar.Builder(httpTransport, JSON_FACTORY, getCredential(principal))
                        .setApplicationName(APPLICATION_NAME)
                        .build();

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
            com.google.api.services.calendar.Calendar service =
                    new com.google.api.services.calendar.Calendar.Builder(httpTransport, JSON_FACTORY, getCredential(principal))
                            .setApplicationName(APPLICATION_NAME)
                            .build();

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

    private TokenResponse executeTokenRequest(TokenRequest request, String userId) throws TokenResponseException {
        try {
            return request.execute();
        } catch (TokenResponseException e) {
            if ("invalid_grant".equals(e.getDetails().getError())) {
                accessTokenRepository.deleteById(userId);
            }
            throw e;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
