package com.harding.meals.service.auth.google;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.harding.meals.properties.GoogleOauthProperties;
import com.harding.meals.repository.AccessTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Google returns a refresh token only on first consent. These tests pin that a
 * later token response without one does not wipe the stored refresh token.
 */
class GoogleAuthServiceTest {

    private static final String USER = "user@test.local";

    private GoogleAuthService service;

    @BeforeEach
    void setUp() throws Exception {
        PersistedDataStoreFactory dataStoreFactory = mock(PersistedDataStoreFactory.class);
        MemoryDataStoreFactory memory = new MemoryDataStoreFactory();
        when(dataStoreFactory.getDataStore(anyString()))
                .thenAnswer(call -> memory.getDataStore(call.getArgument(0)));

        GoogleOauthProperties properties = new GoogleOauthProperties();
        properties.setGoogleClientId("client-id");
        properties.setGoogleClientSecret("client-secret");
        properties.setRedirectUrl("http://localhost/redirect");

        service = new GoogleAuthService(properties, dataStoreFactory, mock(AccessTokenRepository.class));
    }

    @Test
    void storeCredential_keepsStoredRefreshToken_whenNewResponseHasNone() throws Exception {
        service.storeCredential(tokenResponse("first-access", "first-consent-refresh"), USER);

        service.storeCredential(tokenResponse("second-access", null), USER);

        assertEquals("second-access", service.flow.loadCredential(USER).getAccessToken());
        assertEquals("first-consent-refresh", service.flow.loadCredential(USER).getRefreshToken());
    }

    @Test
    void storeCredential_replacesStoredRefreshToken_whenNewResponseHasOne() throws Exception {
        service.storeCredential(tokenResponse("first-access", "old-refresh"), USER);

        service.storeCredential(tokenResponse("second-access", "new-refresh"), USER);

        assertEquals("new-refresh", service.flow.loadCredential(USER).getRefreshToken());
    }

    private static TokenResponse tokenResponse(String accessToken, String refreshToken) {
        return new TokenResponse()
                .setAccessToken(accessToken)
                .setRefreshToken(refreshToken)
                .setExpiresInSeconds(3599L);
    }
}
