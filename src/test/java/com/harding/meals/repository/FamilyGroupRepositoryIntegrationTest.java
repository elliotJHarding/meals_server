package com.harding.meals.repository;

import com.harding.meals.base.BaseIntegrationTest;
import com.harding.meals.entity.user.FamilyGroup;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for FamilyGroupRepository.
 * Tests basic CRUD operations for family groups.
 */
class FamilyGroupRepositoryIntegrationTest extends BaseIntegrationTest {

    @Test
    void saveFamilyGroup_persistsSuccessfully() {
        // Given
        FamilyGroup familyGroup = new FamilyGroup();

        // When
        FamilyGroup savedGroup = familyGroupRepository.save(familyGroup);

        // Then
        assertNotNull(savedGroup.getUuid());
    }

    @Test
    void findById_retrievesFamilyGroup() {
        // Given
        FamilyGroup familyGroup = new FamilyGroup();
        familyGroup = familyGroupRepository.save(familyGroup);

        // When
        Optional<FamilyGroup> retrieved = familyGroupRepository.findById(familyGroup.getUuid());

        // Then
        assertTrue(retrieved.isPresent());
        assertEquals(familyGroup.getUuid(), retrieved.get().getUuid());
    }

    @Test
    void deleteFamilyGroup_removesFromDatabase() {
        // Given
        FamilyGroup familyGroup = new FamilyGroup();
        familyGroup = familyGroupRepository.save(familyGroup);
        String uuid = familyGroup.getUuid().toString();

        // When
        familyGroupRepository.deleteById(familyGroup.getUuid());

        // Then
        assertFalse(familyGroupRepository.findById(familyGroup.getUuid()).isPresent());
    }

    @Test
    void findAll_returnsAllFamilyGroups() {
        // Given
        FamilyGroup family1 = new FamilyGroup();
        FamilyGroup family2 = new FamilyGroup();

        familyGroupRepository.saveAll(List.of(family1, family2));

        // When
        List<FamilyGroup> allGroups = StreamSupport
                .stream(familyGroupRepository.findAll().spliterator(), false)
                .toList();

        // Then
        assertTrue(allGroups.size() >= 2);
    }

    @Test
    void familyGroupUuid_isUniqueAndGenerated() {
        // Given
        FamilyGroup family1 = new FamilyGroup();
        FamilyGroup family2 = new FamilyGroup();

        // When
        family1 = familyGroupRepository.save(family1);
        family2 = familyGroupRepository.save(family2);

        // Then
        assertNotNull(family1.getUuid());
        assertNotNull(family2.getUuid());
        assertNotEquals(family1.getUuid(), family2.getUuid());
    }
}
