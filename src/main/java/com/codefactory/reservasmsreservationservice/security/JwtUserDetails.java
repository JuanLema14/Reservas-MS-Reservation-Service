package com.codefactory.reservasmsreservationservice.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;

/**
 * UserDetails personalizado que incluye el userId (UUID) además del email.
 * El userId se extrae del JWT y se usa como identificador principal.
 */
public class JwtUserDetails implements UserDetails {

    private final String userId;
    private final String email;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean accountNonExpired;
    private final boolean accountNonLocked;
    private final boolean credentialsNonExpired;
    private final boolean enabled;

    public JwtUserDetails(String userId, String email, String password,
                          Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.accountNonExpired = true;
        this.accountNonLocked = true;
        this.credentialsNonExpired = true;
        this.enabled = true;
    }

    /**
     * Returns the userId (UUID) as the primary identifier.
     * This is what should be used for business logic, not the email.
     */
    @Override
    public String getUsername() {
        return userId;
    }

    /**
     * Returns the email for display purposes only.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns the userId (UUID) for business logic.
     */
    public String getUserId() {
        return userId;
    }

    public UUID getUserIdAsUUID() {
        return UUID.fromString(userId);
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}