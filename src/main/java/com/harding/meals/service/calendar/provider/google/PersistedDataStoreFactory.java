package com.harding.meals.service.calendar.provider.google;

import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.DataStoreFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serializable;

@Component
public class PersistedDataStoreFactory implements DataStoreFactory {

    @Override
    public <V extends Serializable> DataStore<V> getDataStore(String id) throws IOException {
        return null;
    }
}
