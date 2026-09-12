package dev.duran.web_educAlba_backend.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyRegisteredException(request.email());
        }

        RoleEntity familyRole = roleRepository.findByName(RoleNames.FAMILY)
            .orElseThrow(() -> new IllegalStateException("El rol " + RoleNames.FAMILY + " no existe en la base de datos"));

        UserEntity user = UserEntity.builder()
            .email(request.email())
            .password(passwordEncoder.encode(request.password()))
            .roles(java.util.Set.of(familyRole))
            .build();

        UserEntity savedUser = userRepository.save(user);

        return new RegisterResponse(savedUser.getId(), savedUser.getEmail());
    }
}
