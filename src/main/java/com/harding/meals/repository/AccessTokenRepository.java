package com.harding.meals.repository;

import com.harding.meals.entity.user.AppUser;
import com.harding.meals.entity.user.token.GoogleOauthToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessTokenRepository extends JpaRepository<GoogleOauthToken, String> {

    String user(AppUser user);
}
