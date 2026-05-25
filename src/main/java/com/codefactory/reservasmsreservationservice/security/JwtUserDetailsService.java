package com.codefactory.reservasmsreservationservice.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

/**
 * UserDetailsService para el microservicio de reservas.
 * No tiene base de datos de usuarios propia - extrae authorities del JWT.
 */
@Service
public class JwtUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Since Reservation Service doesn't have a user database, we create a JwtUserDetails
        // The actual userId (UUID) is extracted from JWT claims and passed as username
        // The authorities are also extracted from JWT claims in JwtAuthenticationFilter
        return new JwtUserDetails(
                username,  // This is actually the userId (UUID)
                username,  // Use same as email for display (will be overridden when full details available)
                "",        // No password needed for JWT validation
                Collections.emptyList()
        );
    }

    /**
     * Create a JwtUserDetails with all fields populated.
     * Called from JwtAuthenticationFilter after extracting claims from JWT.
     */
    public JwtUserDetails createJwtUserDetails(String userId, String email,
                                               Collection<? extends GrantedAuthority> authorities) {
        return new JwtUserDetails(userId, email, "", authorities);
    }
}