package com.harding.meals.service.auth;

import com.harding.meals.entity.user.AppUser;
import com.harding.meals.repository.AccessTokenRepository;
import com.harding.meals.repository.AppUserRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class AppUserDetailsService implements UserDetailsService {

    AppUserRepository userRepository;

    AccessTokenRepository tokenRepository;

    AppUserDetailsService(AppUserRepository appUserRepository) {
        this.userRepository = appUserRepository;
    }

    @Override
    public AppUser loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username);
    }

    public void createUser(AppUser user) {
        userRepository.save(user);
    }

}
