package com.harding.meals.dto.auth;

import com.harding.meals.dto.DataTransferObject;

public record AppUserDto(
    String name,
    String pictureUrl,
    String familyName,
    String givenName
) implements DataTransferObject {}
