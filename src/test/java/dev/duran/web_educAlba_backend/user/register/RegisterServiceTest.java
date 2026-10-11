package dev.duran.web_educAlba_backend.user.register;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Base64;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.duran.web_educAlba_backend.academy.student.EducationalStage;
import dev.duran.web_educAlba_backend.academy.student.StudentRepository;
import dev.duran.web_educAlba_backend.facade.decrypt.IDecryptFacade;
import dev.duran.web_educAlba_backend.facade.encrypt.IEncryptFacade;
import dev.duran.web_educAlba_backend.user.core.RoleNames;
import dev.duran.web_educAlba_backend.user.core.RoleRepository;
import dev.duran.web_educAlba_backend.user.core.UserRepository;

@ExtendWith(MockitoExtension.class)
class RegisterServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private IDecryptFacade decryptFacade;

    @Mock
    private IEncryptFacade encryptFacade;

    @Test
    void testRegister_WhenFamilyRoleIsMissing_ThrowsIllegalStateException() {
        String encodedPassword = Base64.getEncoder().encodeToString("ClaveSegura123".getBytes());
        RegisterRequest request = new RegisterRequest("Lucía", "García", "lucia@educalba.com", encodedPassword,
                "Clases de Refuerzo", EducationalStage.PRIMARIA);

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(decryptFacade.decode("base64", encodedPassword)).thenReturn("ClaveSegura123");
        when(roleRepository.findByName(RoleNames.FAMILY)).thenReturn(Optional.empty());

        RegisterService registerService = new RegisterService(userRepository, roleRepository, studentRepository,
                decryptFacade, encryptFacade);

        assertThatThrownBy(() -> registerService.register(request))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining(RoleNames.FAMILY);
    }
}
