package com.harding.meals.repository;

import com.harding.meals.entity.user.FamilyGroup;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface FamilyGroupRepository extends CrudRepository<FamilyGroup, UUID> {

}
