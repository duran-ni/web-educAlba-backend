package dev.duran.web_educAlba_backend.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.duran.web_educAlba_backend.facade.encrypt.IEncryptFacade;
import dev.duran.web_educAlba_backend.user.core.RoleEntity;
import dev.duran.web_educAlba_backend.user.core.RoleNames;
import dev.duran.web_educAlba_backend.user.core.RoleService;
import dev.duran.web_educAlba_backend.user.core.UserEntity;
import dev.duran.web_educAlba_backend.user.core.UserRepository;

@ExtendWith(MockitoExtension.class)
class DataSeederTest {

    @Mock
    private RoleService roleService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private IEncryptFacade encryptFacade;

    @Test
    void testRun_WhenInitialAdminDoesNotExist_CreatesIt() {
        RoleEntity adminRole = RoleEntity.builder().id(1L).name(RoleNames.ADMIN).build();
        when(roleService.getOrCreateRole(RoleNames.ADMIN)).thenReturn(adminRole);
        when(userRepository.existsByEmail("admin@educalba.com")).thenReturn(false);
        when(encryptFacade.encode("bcrypt", "AdminEducAlba123")).thenReturn("hashed-password");

        DataSeeder dataSeeder = new DataSeeder(roleService, userRepository, encryptFacade);
        dataSeeder.run();

        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void testRun_WhenInitialAdminAlreadyExists_DoesNotCreateItAgain() {
        RoleEntity adminRole = RoleEntity.builder().id(1L).name(RoleNames.ADMIN).build();
        when(roleService.getOrCreateRole(RoleNames.ADMIN)).thenReturn(adminRole);
        when(userRepository.existsByEmail("admin@educalba.com")).thenReturn(true);

        DataSeeder dataSeeder = new DataSeeder(roleService, userRepository, encryptFacade);
        dataSeeder.run();

        verify(userRepository, never()).save(any(UserEntity.class));
    }
}
