package dev.duran.web_educAlba_backend.academy.student;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.duran.web_educAlba_backend.user.auth.SecurityUser;

// Dashboard del usuario (familia/alumno): la ruta /api/dashboard/** ya
// esta restringida al rol FAMILY en SecurityConfiguration, asi que aqui
// no hace falta ninguna comprobacion de permisos adicional
@RestController
@RequestMapping("/api/dashboard")
public class UserDashboardController {

    private final UserDashboardService userDashboardService;

    public UserDashboardController(UserDashboardService userDashboardService) {
        this.userDashboardService = userDashboardService;
    }

    @GetMapping("/me")
    public StudentResponse getMyProfile(@AuthenticationPrincipal SecurityUser securityUser) {
        return userDashboardService.getMyProfile(securityUser.getUser().getId());
    }

    @GetMapping("/my-workshops")
    public List<MyWorkshopResponse> getMyWorkshops(@AuthenticationPrincipal SecurityUser securityUser) {
        return userDashboardService.getMyWorkshops(securityUser.getUser().getId());
    }
}
