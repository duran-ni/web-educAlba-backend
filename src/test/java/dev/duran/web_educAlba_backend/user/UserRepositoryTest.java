package dev.duran.web_educAlba_backend.user;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import dev.duran.web_educAlba_backend.TestcontainersConfiguration;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findsUserByEmail() {
        UserEntity user = UserEntity.builder()
            .email("familia@educalba.com")
            .password("hashed-password")
            .build();
        entityManager.persistAndFlush(user);

        Optional<UserEntity> found = userRepository.findByEmail("familia@educalba.com");

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(user.getId());
    }

    @Test
    void returnsEmptyWhenEmailDoesNotExist() {
        Optional<UserEntity> found = userRepository.findByEmail("no-existe@educalba.com");

        assertThat(found).isEmpty();
    }

    @Test
    void existsByEmailReflectsPersistedUsers() {
        UserEntity user = UserEntity.builder()
            .email("admin@educalba.com")
            .password("hashed-password")
            .build();
        entityManager.persistAndFlush(user);

        assertThat(userRepository.existsByEmail("admin@educalba.com")).isTrue();
        assertThat(userRepository.existsByEmail("otro@educalba.com")).isFalse();
    }
}
