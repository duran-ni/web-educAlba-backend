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
class RoleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void findsRoleByName() {
        RoleEntity role = RoleEntity.builder().name(RoleNames.ADMIN).build();
        entityManager.persistAndFlush(role);

        Optional<RoleEntity> found = roleRepository.findByName(RoleNames.ADMIN);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(role.getId());
    }

    @Test
    void returnsEmptyWhenRoleNameDoesNotExist() {
        assertThat(roleRepository.findByName(RoleNames.FAMILY)).isEmpty();
    }
}
