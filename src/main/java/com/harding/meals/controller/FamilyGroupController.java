package com.harding.meals.controller;

import com.harding.meals.dto.AppUserDto;
import com.harding.meals.dto.FamilyGroupDto;
import com.harding.meals.entity.user.AppUser;
import com.harding.meals.entity.user.FamilyGroup;
import com.harding.meals.mapping.UserMapper;
import com.harding.meals.repository.AppUserRepository;
import com.harding.meals.repository.FamilyGroupRepository;
import org.apache.catalina.User;
import org.apache.coyote.BadRequestException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerErrorException;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static java.util.Objects.nonNull;

@RestController
public class FamilyGroupController {

    private final AppUserRepository appUserRepository;
    private final UserMapper userMapper;
    private final FamilyGroupRepository familyGroupRepository;

    public FamilyGroupController(AppUserRepository appUserRepository, UserMapper userMapper, FamilyGroupRepository familyGroupRepository) {
        this.appUserRepository = appUserRepository;
        this.userMapper = userMapper;
        this.familyGroupRepository = familyGroupRepository;
    }

    @GetMapping("/familyGroup")
    FamilyGroupDto getFamilyGroup(@AuthenticationPrincipal AppUser user) {

        AppUser appUser = appUserRepository.findByUsername(user.getUsername());

        return new FamilyGroupDto()
                .uuid(nonNull(appUser.getFamilyGroup()) ? appUser.getFamilyGroup().getUuid() : null)
                .users(appUserRepository.findAllByFamilyGroup(appUser.getFamilyGroup()).stream()
                        .filter(u -> !u.getUsername().equals(appUser.getUsername()))
                        .map(u -> userMapper.toDto(u.getPublicDetails()))
                        .toList()
                );
    }

    @PostMapping("/familyGroup")
    UUID createFamilyGroup(@AuthenticationPrincipal AppUser user) {

        AppUser appUser = appUserRepository.findByUsername(user.getUsername());

        if (nonNull(appUser.getFamilyGroup())) {
            throw new IllegalStateException("User is already part of a family group");
        }

        FamilyGroup familyGroup = familyGroupRepository.save(new FamilyGroup());
        user.setFamilyGroup(familyGroup);

        appUserRepository.save(user);

        return familyGroup.getUuid();
    }

    @PostMapping("familyGroup/{uuid}")
    void joinFamilyGroup(@PathVariable String uuid, @AuthenticationPrincipal AppUser user) throws BadRequestException {
        if (nonNull(uuid)) {
            
        } else {
            throw new BadRequestException();
        }
    }

}
