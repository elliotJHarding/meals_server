package com.harding.meals.dto.auth;

import java.util.List;
import java.util.UUID;

public record FamilyGroupDto(
    UUID uuid,
    List<AppUserDto> users
) {}
