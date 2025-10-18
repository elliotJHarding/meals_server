package com.harding.meals.repository;

import com.harding.meals.entity.meal.Effort;
import com.harding.meals.entity.meal.Meal;
import com.harding.meals.entity.user.AppUser;
import com.harding.meals.entity.user.FamilyGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MealRepositoryIntegrationTest {

    @Autowired
    private MealRepository mealRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private FamilyGroupRepository familyGroupRepository;

    private FamilyGroup familyGroup1;
    private FamilyGroup familyGroup2;
    private AppUser user1InFamily1;
    private AppUser user2InFamily1;
    private AppUser userInFamily2;
    private AppUser userWithoutFamily;
    private Meal mealByUser1;
    private Meal mealByUser2;
    private Meal mealByUserInFamily2;
    private Meal mealByUserWithoutFamily;

    @BeforeEach
    void setUp() {
        // Create family groups
        familyGroup1 = new FamilyGroup();
        familyGroup1 = familyGroupRepository.save(familyGroup1);

        familyGroup2 = new FamilyGroup();
        familyGroup2 = familyGroupRepository.save(familyGroup2);

        // Create users in family group 1
        user1InFamily1 = new AppUser();
        user1InFamily1.setUsername("user1@family1.com");
        user1InFamily1.setEmail("user1@family1.com");
        user1InFamily1.setEnabled(true);
        user1InFamily1.setFamilyGroup(familyGroup1);
        user1InFamily1 = appUserRepository.save(user1InFamily1);

        user2InFamily1 = new AppUser();
        user2InFamily1.setUsername("user2@family1.com");
        user2InFamily1.setEmail("user2@family1.com");
        user2InFamily1.setEnabled(true);
        user2InFamily1.setFamilyGroup(familyGroup1);
        user2InFamily1 = appUserRepository.save(user2InFamily1);

        // Create user in family group 2
        userInFamily2 = new AppUser();
        userInFamily2.setUsername("user@family2.com");
        userInFamily2.setEmail("user@family2.com");
        userInFamily2.setEnabled(true);
        userInFamily2.setFamilyGroup(familyGroup2);
        userInFamily2 = appUserRepository.save(userInFamily2);

        // Create user without family group
        userWithoutFamily = new AppUser();
        userWithoutFamily.setUsername("user@nofamily.com");
        userWithoutFamily.setEmail("user@nofamily.com");
        userWithoutFamily.setEnabled(true);
        userWithoutFamily = appUserRepository.save(userWithoutFamily);

        // Create meals for different users
        mealByUser1 = new Meal();
        mealByUser1.setName("Pasta by User 1");
        mealByUser1.setEffort(Effort.LOW);
        mealByUser1.setUser(user1InFamily1);
        mealByUser1 = mealRepository.save(mealByUser1);

        mealByUser2 = new Meal();
        mealByUser2.setName("Pizza by User 2");
        mealByUser2.setEffort(Effort.MEDIUM);
        mealByUser2.setUser(user2InFamily1);
        mealByUser2 = mealRepository.save(mealByUser2);

        mealByUserInFamily2 = new Meal();
        mealByUserInFamily2.setName("Salad by Family 2 User");
        mealByUserInFamily2.setEffort(Effort.LOW);
        mealByUserInFamily2.setUser(userInFamily2);
        mealByUserInFamily2 = mealRepository.save(mealByUserInFamily2);

        mealByUserWithoutFamily = new Meal();
        mealByUserWithoutFamily.setName("Burger by No Family User");
        mealByUserWithoutFamily.setEffort(Effort.HIGH);
        mealByUserWithoutFamily.setUser(userWithoutFamily);
        mealByUserWithoutFamily = mealRepository.save(mealByUserWithoutFamily);
    }

    @Test
    void findByFamilyGroup_shouldReturnMealsFromCurrentUser() {
        // When
        List<Meal> meals = mealRepository.findByFamilyGroup(user1InFamily1);

        // Then
        assertNotNull(meals);
        assertTrue(meals.stream().anyMatch(m -> m.getId().equals(mealByUser1.getId())),
                "Should include meals created by the current user");
    }

    @Test
    void findByFamilyGroup_shouldReturnMealsFromOtherUsersInSameFamilyGroup() {
        // When
        List<Meal> meals = mealRepository.findByFamilyGroup(user1InFamily1);

        // Then
        assertNotNull(meals);
        assertTrue(meals.stream().anyMatch(m -> m.getId().equals(mealByUser2.getId())),
                "Should include meals created by other users in the same family group");
    }

    @Test
    void findByFamilyGroup_shouldNotReturnMealsFromDifferentFamilyGroup() {
        // When
        List<Meal> meals = mealRepository.findByFamilyGroup(user1InFamily1);

        // Then
        assertNotNull(meals);
        assertFalse(meals.stream().anyMatch(m -> m.getId().equals(mealByUserInFamily2.getId())),
                "Should NOT include meals from users in a different family group");
    }

    @Test
    void findByFamilyGroup_shouldNotReturnMealsFromUsersWithoutFamilyGroup() {
        // When
        List<Meal> meals = mealRepository.findByFamilyGroup(user1InFamily1);

        // Then
        assertNotNull(meals);
        assertFalse(meals.stream().anyMatch(m -> m.getId().equals(mealByUserWithoutFamily.getId())),
                "Should NOT include meals from users without a family group");
    }

    @Test
    void findByFamilyGroup_shouldReturnExactlyTwoMealsForFamilyGroup1() {
        // When
        List<Meal> meals = mealRepository.findByFamilyGroup(user1InFamily1);

        // Then
        assertEquals(2, meals.size(),
                "Should return exactly 2 meals (one from user1 and one from user2 in family group 1)");
    }

    @Test
    void findByFamilyGroup_shouldReturnSameMealsForAllUsersInSameFamilyGroup() {
        // When
        List<Meal> mealsForUser1 = mealRepository.findByFamilyGroup(user1InFamily1);
        List<Meal> mealsForUser2 = mealRepository.findByFamilyGroup(user2InFamily1);

        // Then
        assertEquals(mealsForUser1.size(), mealsForUser2.size(),
                "Both users in the same family group should see the same number of meals");

        assertTrue(mealsForUser1.stream().allMatch(m -> mealsForUser2.stream()
                        .anyMatch(m2 -> m2.getId().equals(m.getId()))),
                "Both users should see exactly the same meals");
    }

    @Test
    void findByFamilyGroup_shouldReturnEmptyListForUserWithoutFamilyGroup() {
        // When
        List<Meal> meals = mealRepository.findByFamilyGroup(userWithoutFamily);

        // Then
        assertNotNull(meals);
        assertTrue(meals.isEmpty() || meals.stream().noneMatch(m ->
                m.getId().equals(mealByUser1.getId()) ||
                m.getId().equals(mealByUser2.getId()) ||
                m.getId().equals(mealByUserInFamily2.getId())),
                "User without family group should not see meals from family groups");
    }
}
