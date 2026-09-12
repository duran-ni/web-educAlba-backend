package dev.duran.web_educAlba_backend.user;

import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

// Adaptador entre nuestra entidad UserEntity y lo que Spring Security necesita
// para autenticar y autorizar (la interfaz UserDetails)
public class SecurityUser implements UserDetails {

    private final UserEntity user;

    public SecurityUser(UserEntity user) {
        this.user = user;
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
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
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Spring Security exige que cada autoridad de rol lleve el prefijo "ROLE_"
        // para que las reglas hasRole(...) de SecurityConfiguration puedan encontrarla
        return user.getRoles().stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
            .collect(Collectors.toSet());
    }

    public UserEntity getUser() {
        return user;
    }
}
