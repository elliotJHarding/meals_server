package com.harding.meals.service.auth.google;

import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.DataStoreFactory;
import com.harding.meals.repository.AccessTokenRepository;
import com.harding.meals.repository.AppUserRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serializable;

@Component
public class PersistedDataStoreFactory implements DataStoreFactory {

    private final AccessTokenRepository accessTokenRepository;
    private final AppUserRepository appUserRepository;

    public PersistedDataStoreFactory(AccessTokenRepository accessTokenRepository, AppUserRepository appUserRepository) {
        this.accessTokenRepository = accessTokenRepository;
        this.appUserRepository = appUserRepository;
    }

    @Override
    public <V extends Serializable> DataStore<V> getDataStore(String id) throws IOException {
        return (DataStore<V>) new PersistedDataStore(id, accessTokenRepository, appUserRepository);
    }
}
