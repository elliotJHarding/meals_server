package com.harding.meals.entity.user;

import com.google.api.client.auth.oauth2.StoredCredential;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnTransformer;

import java.io.Serializable;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Entity
public class GoogleOauthToken implements Serializable {
    @Id
    private String id;

    @ColumnTransformer(
            read = """
                    pgp_sym_decrypt(
                        access_token,
                        'calendarTokenEncryptionKey'
                    )
                    """,
            write = """
                    pgp_sym_encrypt(
                        ?,
                        'calendarTokenEncryptionKey'
                    )
                    """
    )
    @Column(columnDefinition = "bytea")
    private String accessToken;

    @ColumnTransformer(
            read = """
                    pgp_sym_decrypt(
                        refresh_token,
                        'calendarTokenEncryptionKey'
                    )
                    """,
            write = """
                    pgp_sym_encrypt(
                        ?,
                        'calendarTokenEncryptionKey'
                    )
                    """
    )
    @Column(columnDefinition = "bytea")
    private String refreshToken;

    private OffsetDateTime created;
    private OffsetDateTime expires;

    @ManyToOne
    private AppUser user;

    public GoogleOauthToken() {
    }

    public GoogleOauthToken(AppUser user, StoredCredential credential) {
        this.id = user.getEmail();
        this.accessToken = credential.getAccessToken();
        this.refreshToken = credential.getRefreshToken();
        this.created = OffsetDateTime.now();
        this.expires = OffsetDateTime.ofInstant(Instant.ofEpochMilli(credential.getExpirationTimeMilliseconds()), ZoneId.systemDefault());
        this.user = user;
    }

    public StoredCredential getStoredCredential() {
        StoredCredential credential = new StoredCredential();
        credential.setAccessToken(this.accessToken);
        credential.setRefreshToken(this.refreshToken);
        credential.setExpirationTimeMilliseconds(expires.toInstant().toEpochMilli());
        return credential;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public OffsetDateTime getCreated() {
        return created;
    }

    public void setCreated(OffsetDateTime created) {
        this.created = created;
    }

    public OffsetDateTime getExpires() {
        return expires;
    }

    public void setExpires(OffsetDateTime expires) {
        this.expires = expires;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }
}
