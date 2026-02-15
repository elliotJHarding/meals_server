package com.harding.meals.service.auth.google;

import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.DataStoreFactory;
import com.harding.meals.entity.user.AppUser;
import com.harding.meals.entity.user.token.GoogleOauthToken;
import com.harding.meals.repository.AccessTokenRepository;
import com.harding.meals.repository.AppUserRepository;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

public class PersistedDataStore implements DataStore<StoredCredential> {
    private String id;

    private final AccessTokenRepository tokenRepository;
    private final AppUserRepository appUserRepository;

    public PersistedDataStore(String id, AccessTokenRepository tokenRepository, AppUserRepository appUserRepository) {
        this.id = id;
        this.tokenRepository = tokenRepository;
        this.appUserRepository = appUserRepository;
    }

    @Override
    public DataStoreFactory getDataStoreFactory() {
        return null;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int size() throws IOException {
        return (int) tokenRepository.count();
    }

    @Override
    public boolean isEmpty() throws IOException {
        return tokenRepository.count() == 0;
    }

    @Override
    public boolean containsKey(String key) throws IOException {
        return tokenRepository.existsById(key);
    }

    @Override
    public boolean containsValue(StoredCredential value) throws IOException {
        return false;
    }

    @Override
    public Set<String> keySet() throws IOException {
        return tokenRepository.findAll().stream()
                .map(GoogleOauthToken::getId)
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<StoredCredential> values() throws IOException {
        return tokenRepository.findAll().stream().map(GoogleOauthToken::getStoredCredential).toList();
    }

    @Override
    public StoredCredential get(String key) throws IOException {
        GoogleOauthToken token = tokenRepository.findById(key).orElse(null);
        return nonNull(token) ? token.getStoredCredential() : null;
    }

    @Override
    public DataStore<StoredCredential> set(String key, StoredCredential value) {
        AppUser user = appUserRepository.findByEmail(key);
        GoogleOauthToken token = tokenRepository.findById(key).orElse(null);
        if (token != null) {
            token.setAccessToken(value.getAccessToken());
            token.setRefreshToken(value.getRefreshToken());
            token.setCreated(OffsetDateTime.now());
            token.setExpires(OffsetDateTime.ofInstant(
                    Instant.ofEpochMilli(value.getExpirationTimeMilliseconds()),
                    ZoneId.systemDefault()));
        } else {
            token = new GoogleOauthToken(user, value);
        }
        tokenRepository.save(token);
        return this;
    }

    @Override
    public DataStore<StoredCredential> clear() throws IOException {
        tokenRepository.deleteAll();
        return this;
    }

    @Override
    public DataStore<StoredCredential> delete(String key) throws IOException {
        tokenRepository.deleteById(key);
        return this;
    }
}
