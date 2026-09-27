package com.harding.meals.service.auth.google;

import com.google.api.client.auth.oauth2.*;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.harding.meals.entity.user.AppUser;
import com.harding.meals.properties.GoogleOauthProperties;
import com.harding.meals.repository.AccessTokenRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.util.List;

import static java.util.Objects.nonNull;

@Component
public class GoogleAuthService {

    private static String APPLICATION_NAME = "Meal Planner";

    private static final List<String> SCOPES = List.of(
            CalendarScopes.CALENDAR_READONLY,
            "https://www.googleapis.com/auth/cloud-platform",
            "https://www.googleapis.com/auth/generative-language.retriever"
    );
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private final PersistedDataStoreFactory persistedDataStoreFactory;
    private final AccessTokenRepository accessTokenRepository;

    private GoogleClientSecrets clientSecrets;

    GoogleOauthProperties oauthProperties;
    NetHttpTransport httpTransport;
    LocalServerReceiver receiver;

    GoogleAuthorizationCodeFlow flow;

    public GoogleAuthService(GoogleOauthProperties oauthProperties, PersistedDataStoreFactory persistedDataStoreFactory, AccessTokenRepository accessTokenRepository) throws GeneralSecurityException, IOException {
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

    public Credential getCredential(AppUser principal) throws IOException {
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

    public String getAuthorizationUrl(AppUser principal) throws IOException {

        GoogleAuthorizationCodeRequestUrl url = flow.newAuthorizationUrl();

        url.setRedirectUri(oauthProperties.getRedirectUrl());

        return url.toString();
    }

    public TokenResponse getAccessToken(String authorizationCode, String userId) throws IOException {
        return getAccessToken(authorizationCode, userId, oauthProperties.getRedirectUrl());
    }

    public TokenResponse getAccessToken(String authorizationCode, String userId, String redirectUri) throws IOException {
        GoogleAuthorizationCodeTokenRequest request =
                flow
                        .newTokenRequest(URLDecoder.decode(authorizationCode))
                        .setRedirectUri(redirectUri)
                        .setClientAuthentication(new ClientParametersAuthentication(oauthProperties.getGoogleClientId(), oauthProperties.getGoogleClientSecret()))
                        .setGrantType("authorization_code");

        return executeTokenRequest(request, userId);
    }

    public void authorize(String authorizationCode, AppUser user) throws IOException {
        TokenResponse response = getAccessToken(authorizationCode, user.getEmail());

        flow.createAndStoreCredential(response, user.getEmail());
    }

    public void authorizeMobile(String authorizationCode, AppUser user) throws IOException {
        TokenResponse response = getAccessToken(authorizationCode, user.getEmail(), "");

        flow.createAndStoreCredential(response, user.getEmail());
    }

    /**
     * Exchanges an auth-code-flow serverAuthCode for the full Google token
     * response. An auth code is single-use, so the caller exchanges once and
     * reuses the returned response for both identity ({@code getIdToken()}) and
     * the offline credential ({@link #storeCredential}). {@code redirectUri} is
     * "postmessage" for the GIS popup auth-code flow (web) — it must match the
     * value the client obtained the code with.
     */
    public GoogleTokenResponse exchangeAuthCode(String authorizationCode, String redirectUri) throws IOException {
        return flow
                .newTokenRequest(URLDecoder.decode(authorizationCode))
                .setRedirectUri(redirectUri)
                .setClientAuthentication(new ClientParametersAuthentication(oauthProperties.getGoogleClientId(), oauthProperties.getGoogleClientSecret()))
                .setGrantType("authorization_code")
                .execute();
    }

    /**
     * Stores an offline credential from an already-exchanged token response,
     * keyed by user email. Used by the web auth-code login so the single code
     * exchange is not repeated.
     */
    public void storeCredential(TokenResponse response, String userId) throws IOException {
        flow.createAndStoreCredential(response, userId);
    }

    public TokenResponse refreshAccessToken(String userEmail, String refreshToken) throws IOException {
        GoogleRefreshTokenRequest request = new GoogleRefreshTokenRequest(
                httpTransport, flow.getJsonFactory(), refreshToken, oauthProperties.getGoogleClientId(), oauthProperties.getGoogleClientSecret()
        );

        return executeTokenRequest(request, userEmail);
    }

    public Calendar getCalendarService(AppUser principal) throws IOException {
        return new com.google.api.services.calendar.Calendar.Builder(httpTransport, JSON_FACTORY, getCredential(principal))
                .setApplicationName(APPLICATION_NAME)
                .build();
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
