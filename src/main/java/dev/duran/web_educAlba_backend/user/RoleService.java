package dev.duran.web_educAlba_backend.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Transactional
    public RoleEntity getOrCreateRole(String name) {
        return roleRepository.findByName(name)
            .orElseGet(() -> roleRepository.save(RoleEntity.builder().name(name).build()));
    }

}
