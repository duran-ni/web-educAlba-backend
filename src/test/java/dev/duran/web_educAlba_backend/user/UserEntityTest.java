package dev.duran.web_educAlba_backend.user;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;

class UserEntityTest {

    @Test
    void buildsUserWithRoles() {
        RoleEntity familiaRole = RoleEntity.builder().name("ALUMNO_FAMILIA").build();

        UserEntity user = UserEntity.builder()
            .email("familia@educalba.com")
            .password("hashed-password")
            .roles(Set.of(familiaRole))
            .build();

        assertThat(user.getEmail()).isEqualTo("familia@educalba.com");
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.getRoles()).contains(familiaRole);
    }
}
