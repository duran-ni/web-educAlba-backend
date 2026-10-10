package dev.duran.web_educAlba_backend.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Test
    void returnsExistingRoleWhenItAlreadyExists() {
        RoleEntity existingRole = RoleEntity.builder().id(1L).name(RoleNames.ADMIN).build();
        when(roleRepository.findByName(RoleNames.ADMIN)).thenReturn(Optional.of(existingRole));

        RoleService roleService = new RoleService(roleRepository);
        RoleEntity result = roleService.getOrCreateRole(RoleNames.ADMIN);

        assertThat(result).isEqualTo(existingRole);
    }

    @Test
    void createsRoleWhenItDoesNotExist() {
        when(roleRepository.findByName(RoleNames.FAMILY)).thenReturn(Optional.empty());
        when(roleRepository.save(org.mockito.ArgumentMatchers.any(RoleEntity.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        RoleService roleService = new RoleService(roleRepository);
        RoleEntity result = roleService.getOrCreateRole(RoleNames.FAMILY);

        assertThat(result.getName()).isEqualTo(RoleNames.FAMILY);
        verify(roleRepository).save(org.mockito.ArgumentMatchers.any(RoleEntity.class));
    }
}
