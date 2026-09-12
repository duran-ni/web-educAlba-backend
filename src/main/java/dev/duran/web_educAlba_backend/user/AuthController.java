package dev.duran.web_educAlba_backend.user;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/me")
    public UserSummaryResponse me(@AuthenticationPrincipal SecurityUser securityUser) {
        UserEntity user = securityUser.getUser();

        List<String> roleNames = user.getRoles().stream()
            .map(RoleEntity::getName)
            .toList();

        return new UserSummaryResponse(user.getId(), user.getEmail(), roleNames);
    }
}
