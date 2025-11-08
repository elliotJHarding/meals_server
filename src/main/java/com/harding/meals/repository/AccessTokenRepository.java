package com.harding.meals.repository;

import com.harding.meals.entity.user.AppUser;
import com.harding.meals.entity.user.token.GoogleOauthToken;
import org.springframework.data.repository.ListCrudRepository;

public interface AccessTokenRepository extends ListCrudRepository<GoogleOauthToken, String> {

    String user(AppUser user);
}
