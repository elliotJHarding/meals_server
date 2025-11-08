package com.harding.meals.util;

import com.harding.meals.entity.user.AppUser;
import com.harding.meals.entity.user.FamilyGroup;
import com.harding.meals.entity.user.PublicDetails;

/**
 * Factory for creating test entities with correct structure
 */
public class TestEntityFactory {

    private static long counter = 1000L;

    public static AppUser createUser(String email, String name) {
        AppUser user = new AppUser();
        user.setEmail(email);
        user.setUsername("user_" + (counter++));

        PublicDetails publicDetails = new PublicDetails();
        publicDetails.setName(name);
        publicDetails.setEmailVerified(true);
        user.setPublicDetails(publicDetails);

        return user;
    }

    public static AppUser createUser(String email, String name, FamilyGroup familyGroup) {
        AppUser user = createUser(email, name);
        user.setFamilyGroup(familyGroup);
        return user;
    }

    public static FamilyGroup createFamilyGroup() {
        return new FamilyGroup();
    }
}
