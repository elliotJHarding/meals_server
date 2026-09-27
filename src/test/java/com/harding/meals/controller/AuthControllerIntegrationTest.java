package com.harding.meals.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.json.webtoken.JsonWebSignature;
import com.harding.meals.base.BaseControllerTest;
import com.harding.meals.service.auth.google.GoogleAuthService;
import com.harding.meals.service.auth.google.VerifyGoogleJwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web auth-code login with the Google calls mocked. The request arrives with no
 * session, so it must go through the public filter chain; the session it creates
 * must then authenticate whoami.
 */
class AuthControllerIntegrationTest extends BaseControllerTest {

    private static final String AUTH_CODE = "web-auth-code";
    private static final String ID_TOKEN = "google-id-token";

    @MockitoBean
    private GoogleAuthService googleAuthService;

    @MockitoBean
    private VerifyGoogleJwtService verifyGoogleJwtService;

    @BeforeEach
    void setUp() {
        super.baseSetUp();
    }

    @Test
    void authCodeLoginCreatesSessionForUnauthenticatedCaller() throws Exception {
        GoogleTokenResponse tokenResponse = new GoogleTokenResponse();
        tokenResponse.setIdToken(ID_TOKEN);
        when(googleAuthService.exchangeAuthCode(AUTH_CODE, "postmessage")).thenReturn(tokenResponse);
        when(verifyGoogleJwtService.verify(eq(ID_TOKEN))).thenReturn(idToken("sub-123", "cook@test.com"));

        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/auth/login/authcode")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"authCode\":\"" + AUTH_CODE + "\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/auth/whoami").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cook"));
    }

    private static GoogleIdToken idToken(String subject, String email) {
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setSubject(subject);
        payload.setEmail(email);
        payload.setEmailVerified(true);
        payload.set("name", "Cook");
        return new GoogleIdToken(new JsonWebSignature.Header(), payload, new byte[0], new byte[0]);
    }
}
