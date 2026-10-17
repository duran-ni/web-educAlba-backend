package dev.duran.web_educAlba_backend.academy.teacher;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/teachers")
public class PublicTeacherController {

    private final TeacherService teacherService;

    public PublicTeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @GetMapping
    public List<TeacherResponse> getAll() {
        return teacherService.getAll();
    }
}
