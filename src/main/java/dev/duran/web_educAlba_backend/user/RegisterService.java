package dev.duran.web_educAlba_backend.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.duran.web_educAlba_backend.facade.decrypt.IDecryptFacade;
import dev.duran.web_educAlba_backend.facade.encrypt.IEncryptFacade;

@Service
public class RegisterService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final IDecryptFacade decryptFacade;
    private final IEncryptFacade encryptFacade;

    public RegisterService(UserRepository userRepository, RoleRepository roleRepository, IDecryptFacade decryptFacade,
            IEncryptFacade encryptFacade) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.decryptFacade = decryptFacade;
        this.encryptFacade = encryptFacade;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyRegisteredException(request.email());
        }

        String decodedPassword = decryptFacade.decode("base64", request.password());

        if (decodedPassword.length() < 8) {
            throw new InvalidPasswordException("La contraseña debe tener al menos 8 caracteres");
        }

        RoleEntity familyRole = roleRepository.findByName(RoleNames.FAMILY)
                .orElseThrow(() -> new IllegalStateException(
                        "El rol " + RoleNames.FAMILY + " no existe en la base de datos"));

        UserEntity user = UserEntity.builder()
                .email(request.email())
                .password(encryptFacade.encode("bcrypt", decodedPassword))
                .roles(java.util.Set.of(familyRole))
                .build();

        UserEntity savedUser = userRepository.save(user);

        return new RegisterResponse(savedUser.getId(), savedUser.getEmail());
    }
}
