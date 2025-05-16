package com.harding.meals.repository;

import com.harding.meals.entity.user.GoogleOauthToken;
import org.springframework.data.repository.ListCrudRepository;

public interface AccessTokenRepository extends ListCrudRepository<GoogleOauthToken, String> {

}
