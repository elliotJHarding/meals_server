package com.harding.meals.service;

import com.harding.meals.entity.FamilyGroupResource;
import com.harding.meals.entity.user.AppUser;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class FamilyResourceService {

    public void validateOwnership(AppUser user, FamilyGroupResource resource) {

        boolean userOwnsMeal = Objects.equals(resource.getUser().getId(), user.getId());

        boolean userInFamilyGroupOwnsMeal =
                user.getFamilyGroup() != null &&
                resource.getUser().getFamilyGroup() != null &&
                        user.getFamilyGroup().getUuid().equals(
                                resource.getUser().getFamilyGroup().getUuid()
                        );

        if (userOwnsMeal || userInFamilyGroupOwnsMeal) {
            return;
        }

        throw new IllegalArgumentException("User does not own this meal");
    }

}
