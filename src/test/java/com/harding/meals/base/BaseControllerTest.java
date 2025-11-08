package com.harding.meals.base;

import com.harding.meals.entity.user.AppUser;
import com.harding.meals.entity.user.FamilyGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * Base class for controller integration tests.
 * Extends BaseIntegrationTest and adds:
 * - MockMvc for HTTP request testing
 * - Helper methods for authenticated requests
 * - JSON serialization utilities
 */
public abstract class BaseControllerTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    protected MockMvc mockMvc;

    @Override
    public void baseSetUp() {
        super.baseSetUp();
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    /**
     * Create a GET request with authentication
     */
    protected MockHttpServletRequestBuilder authenticatedGet(String url, AppUser user) {
        return get(url)
                .with(SecurityMockMvcRequestPostProcessors.user(user))
                .contentType(MediaType.APPLICATION_JSON);
    }

    /**
     * Create a POST request with authentication
     */
    protected MockHttpServletRequestBuilder authenticatedPost(String url, AppUser user) {
        return post(url)
                .with(SecurityMockMvcRequestPostProcessors.user(user))
                .contentType(MediaType.APPLICATION_JSON);
    }

    /**
     * Create a PUT request with authentication
     */
    protected MockHttpServletRequestBuilder authenticatedPut(String url, AppUser user) {
        return put(url)
                .with(SecurityMockMvcRequestPostProcessors.user(user))
                .contentType(MediaType.APPLICATION_JSON);
    }

    /**
     * Create a DELETE request with authentication
     */
    protected MockHttpServletRequestBuilder authenticatedDelete(String url, AppUser user) {
        return delete(url)
                .with(SecurityMockMvcRequestPostProcessors.user(user))
                .contentType(MediaType.APPLICATION_JSON);
    }

    /**
     * Convert object to JSON string
     */
    protected String toJson(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }

    /**
     * Convert JSON string to object
     */
    protected <T> T fromJson(String json, Class<T> clazz) throws Exception {
        return objectMapper.readValue(json, clazz);
    }

    /**
     * Create a test user and persist it
     */
    protected AppUser createTestUser(String email, String name) {
        AppUser user = new AppUser();
        user.setEmail(email);
        user.setUsername("user_" + email.replace("@", "_").replace(".", "_"));

        com.harding.meals.entity.user.PublicDetails publicDetails = new com.harding.meals.entity.user.PublicDetails();
        publicDetails.setName(name);
        publicDetails.setEmailVerified(true);
        user.setPublicDetails(publicDetails);

        return appUserRepository.save(user);
    }

    /**
     * Create a test user with family group and persist it
     */
    protected AppUser createTestUserWithFamily(String email, String name, FamilyGroup familyGroup) {
        AppUser user = createTestUser(email, name);
        user.setFamilyGroup(familyGroup);
        return appUserRepository.save(user);
    }

    /**
     * Create a test family group and persist it
     */
    protected FamilyGroup createTestFamilyGroup(String displayName) {
        FamilyGroup familyGroup = new FamilyGroup();
        // FamilyGroup doesn't have a name field, UUID is auto-generated
        return familyGroupRepository.save(familyGroup);
    }
}
