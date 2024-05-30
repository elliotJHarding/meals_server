package com.harding.meals.entity.user;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Entity
public class AppUser implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String username;
    @Column(nullable = false)
    private boolean enabled;
    private String email;

    private PublicDetails publicDetails;

    public AppUser() {
        this.enabled = true;
    }

    public AppUser(GoogleIdToken idToken) {
        GoogleIdToken.Payload payload = idToken.getPayload();

        this.setEmail(payload.getEmail());
        this.setUsername(payload.getSubject());

        this.setPublicDetails(new PublicDetails(
                (String) payload.get("name"),
                (String) payload.get("picture"),
                (String) payload.get("locale"),
                (String) payload.get("family_name"),
                (String) payload.get("given_name"),
                payload.getEmailVerified()
        ));
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public PublicDetails getPublicDetails() {
        return publicDetails;
    }

    public void setPublicDetails(PublicDetails publicDetails) {
        this.publicDetails = publicDetails;
    }
}
