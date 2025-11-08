package com.harding.meals.repository;

import com.harding.meals.base.BaseIntegrationTest;
import com.harding.meals.entity.user.AppUser;
import com.harding.meals.entity.user.FamilyGroup;
import com.harding.meals.entity.user.PublicDetails;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for AppUserRepository.
 * Tests user queries and family group relationships.
 */
class AppUserRepositoryIntegrationTest extends BaseIntegrationTest {

    @Test
    void saveAppUser_persistsSuccessfully() {
        // Given
        AppUser user = createUser("newuser@example.com", "New User");

        // When
        AppUser savedUser = appUserRepository.save(user);

        // Then
        assertNotNull(savedUser.getId());
        assertEquals("newuser@example.com", savedUser.getEmail());
        assertEquals("New User", savedUser.getPublicDetails().getName());
    }

    @Test
    void findByEmail_retrievesCorrectUser() {
        // Given
        AppUser user = createUser("findme@example.com", "Find Me");
        appUserRepository.save(user);

        // When
        AppUser found = appUserRepository.findByEmail("findme@example.com");

        // Then
        assertNotNull(found);
        assertEquals("findme@example.com", found.getEmail());
        assertEquals("Find Me", found.getPublicDetails().getName());
    }

    @Test
    void findByEmail_returnsNullWhenNotFound() {
        // When
        AppUser found = appUserRepository.findByEmail("nonexistent@example.com");

        // Then
        assertNull(found);
    }

    @Test
    void findByUsername_retrievesCorrectUser() {
        // Given
        AppUser user = createUser("user@example.com", "User Name");
        user.setUsername("testusername");
        appUserRepository.save(user);

        // When
        AppUser found = appUserRepository.findByUsername("testusername");

        // Then
        assertNotNull(found);
        assertEquals("testusername", found.getUsername());
    }

    @Test
    void findAllByFamilyGroup_returnsAllMembersOfFamily() {
        // Given
        FamilyGroup family = new FamilyGroup();
        family = familyGroupRepository.save(family);

        AppUser user1 = createUser("user1@family.com", "User 1");
        user1.setFamilyGroup(family);

        AppUser user2 = createUser("user2@family.com", "User 2");
        user2.setFamilyGroup(family);

        AppUser user3 = createUser("outsider@example.com", "Outsider");
        // No family group

        appUserRepository.saveAll(List.of(user1, user2, user3));

        // When
        List<AppUser> familyMembers = appUserRepository.findAllByFamilyGroup(family);

        // Then
        assertEquals(2, familyMembers.size());
        assertTrue(familyMembers.stream().anyMatch(u -> u.getEmail().equals("user1@family.com")));
        assertTrue(familyMembers.stream().anyMatch(u -> u.getEmail().equals("user2@family.com")));
        assertFalse(familyMembers.stream().anyMatch(u -> u.getEmail().equals("outsider@example.com")));
    }

    @Test
    void findAllByFamilyGroup_returnsEmptyListWhenNoMembers() {
        // Given
        FamilyGroup emptyFamily = new FamilyGroup();
        emptyFamily = familyGroupRepository.save(emptyFamily);

        // When
        List<AppUser> members = appUserRepository.findAllByFamilyGroup(emptyFamily);

        // Then
        assertTrue(members.isEmpty());
    }

    @Test
    void userCanJoinFamilyGroup() {
        // Given
        AppUser user = createUser("user@example.com", "User");
        user = appUserRepository.save(user);

        FamilyGroup family = new FamilyGroup();
        family = familyGroupRepository.save(family);

        // When
        user.setFamilyGroup(family);
        AppUser updatedUser = appUserRepository.save(user);

        // Then
        assertNotNull(updatedUser.getFamilyGroup());
        assertEquals(family.getUuid(), updatedUser.getFamilyGroup().getUuid());

        // Verify family members query
        List<AppUser> members = appUserRepository.findAllByFamilyGroup(family);
        assertEquals(1, members.size());
        assertEquals(user.getId(), members.get(0).getId());
    }

    @Test
    void userCanLeaveFamilyGroup() {
        // Given
        FamilyGroup family = new FamilyGroup();
        family = familyGroupRepository.save(family);

        AppUser user = createUser("user@example.com", "User");
        user.setFamilyGroup(family);
        user = appUserRepository.save(user);

        // When
        user.setFamilyGroup(null);
        AppUser updatedUser = appUserRepository.save(user);

        // Then
        assertNull(updatedUser.getFamilyGroup());

        // Verify user no longer in family members
        List<AppUser> members = appUserRepository.findAllByFamilyGroup(family);
        assertTrue(members.isEmpty());
    }

    @Test
    void multipleUsersWithDifferentUsernames_canExist() {
        // Given
        AppUser user1 = createUser("user1@gmail.com", "User 1");
        user1.setUsername("user1");

        AppUser user2 = createUser("user2@gmail.com", "User 2");
        user2.setUsername("user2");

        // When
        appUserRepository.saveAll(List.of(user1, user2));

        // Then
        List<AppUser> allUsers = StreamSupport
                .stream(appUserRepository.findAll().spliterator(), false)
                .toList();
        assertTrue(allUsers.size() >= 2);
    }

    // Helper method
    private AppUser createUser(String email, String name) {
        AppUser user = new AppUser();
        user.setEmail(email);
        user.setUsername("user_" + email.replace("@", "_").replace(".", "_"));

        PublicDetails publicDetails = new PublicDetails();
        publicDetails.setName(name);
        publicDetails.setEmailVerified(true);
        user.setPublicDetails(publicDetails);

        return user;
    }
}
