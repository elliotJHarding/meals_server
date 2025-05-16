package com.harding.meals.repository;

import com.harding.meals.entity.user.AppUser;
import com.harding.meals.entity.user.FamilyGroup;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AppUserRepository extends CrudRepository<AppUser, Long> {

    AppUser findByUsername(String username);

    AppUser findByEmail(String email);

    List<AppUser> findAllByFamilyGroup(FamilyGroup familyGroup);
}
