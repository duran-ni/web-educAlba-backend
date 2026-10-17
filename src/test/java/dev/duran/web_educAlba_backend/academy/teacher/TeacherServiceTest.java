package dev.duran.web_educAlba_backend.academy.teacher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;

@ExtendWith(MockitoExtension.class)
class TeacherServiceTest {

    @Mock
    TeacherRepository teacherRepository;

    @Mock
    Cloudinary cloudinary;

    TeacherService teacherService;

    // No es un @Mock de Mockito: lo creamos a mano para poder encadenarlo
    // con "when(cloudinary.uploader()).thenReturn(uploader)", igual que
    // hace GalleryPhotoControllerTest con su doble de Cloudinary
    Uploader uploader;

    @Captor
    ArgumentCaptor<Teacher> teacherCaptor;

    @BeforeEach
    void setUp() {
        teacherService = new TeacherService(teacherRepository, cloudinary);
    }

    private Teacher buildTeacher(Long id) {
        return Teacher.builder()
                .id(id)
                .name("Laura Gómez")
                .specialty("Matemáticas")
                .build();
    }

    private Teacher buildTeacherWithPhoto(Long id) {
        return Teacher.builder()
                .id(id)
                .name("Laura Gómez")
                .specialty("Matemáticas")
                .photoUrl("https://res.cloudinary.com/demo/old.jpg")
                .photoPublicId("old-public-id")
                .build();
    }

    private MultipartFile buildImage() {
        return new MockMultipartFile("image", "photo.jpg", "image/jpeg", "fake-image-content".getBytes());
    }

    @Test
    @DisplayName("getAll returns every teacher mapped to a response")
    void getAll_ReturnsAllTeachersMapped() {
        Teacher teacher = buildTeacher(1L);
        when(teacherRepository.findAll()).thenReturn(List.of(teacher));

        List<TeacherResponse> result = teacherService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).name()).isEqualTo("Laura Gómez");
        assertThat(result.get(0).specialty()).isEqualTo("Matemáticas");
    }

    @Test
    @DisplayName("getAll returns an empty list when there are no teachers")
    void getAll_ReturnsEmptyList_WhenNoTeachersExist() {
        when(teacherRepository.findAll()).thenReturn(List.of());

        List<TeacherResponse> result = teacherService.getAll();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getById returns the matching teacher")
    void getById_ReturnsMatchingTeacher() {
        Teacher teacher = buildTeacher(1L);
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));

        TeacherResponse result = teacherService.getById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.specialty()).isEqualTo("Matemáticas");
    }

    @Test
    @DisplayName("getById throws TeacherNotFoundException when the teacher does not exist")
    void getById_ThrowsWhenTeacherDoesNotExist() {
        when(teacherRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teacherService.getById(1L))
                .isInstanceOf(TeacherNotFoundException.class);
    }

    @Test
    @DisplayName("create saves a new teacher built from the request and returns it mapped")
    void create_SavesAndReturnsNewTeacher() {
        TeacherRequest request = new TeacherRequest("Marcos Ruiz", "Educación Física");
        Teacher savedTeacher = Teacher.builder()
                .id(5L)
                .name("Marcos Ruiz")
                .specialty("Educación Física")
                .build();
        when(teacherRepository.save(teacherCaptor.capture())).thenReturn(savedTeacher);

        TeacherResponse result = teacherService.create(request);

        assertThat(teacherCaptor.getValue().getId()).isNull();
        assertThat(teacherCaptor.getValue().getName()).isEqualTo("Marcos Ruiz");
        assertThat(teacherCaptor.getValue().getSpecialty()).isEqualTo("Educación Física");
        assertThat(result.id()).isEqualTo(5L);
        assertThat(result.name()).isEqualTo("Marcos Ruiz");
    }

    @Test
    @DisplayName("update overwrites the existing teacher's fields and saves it")
    void update_OverwritesFieldsAndSaves() {
        Teacher existingTeacher = buildTeacher(1L);
        TeacherRequest request = new TeacherRequest("Laura Gómez", "Música");
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(existingTeacher));
        when(teacherRepository.save(existingTeacher)).thenReturn(existingTeacher);

        TeacherResponse result = teacherService.update(1L, request);

        assertThat(existingTeacher.getSpecialty()).isEqualTo("Música");
        assertThat(result.specialty()).isEqualTo("Música");
        verify(teacherRepository).save(existingTeacher);
    }

    @Test
    @DisplayName("update throws TeacherNotFoundException when the teacher does not exist")
    void update_ThrowsWhenTeacherDoesNotExist() {
        TeacherRequest request = new TeacherRequest("No importa", "No importa");
        when(teacherRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teacherService.update(1L, request))
                .isInstanceOf(TeacherNotFoundException.class);
    }

    @Test
    @DisplayName("uploadPhoto uploads the image to Cloudinary and saves the teacher with its photo data")
    void uploadPhoto_UploadsAndSavesPhotoData() throws IOException {
        Teacher teacher = buildTeacher(1L);
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(cloudinary.uploader()).thenReturn(uploader = mock(Uploader.class));
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(Map.of(
                "secure_url", "https://res.cloudinary.com/demo/new.jpg",
                "public_id", "new-public-id"));
        when(teacherRepository.save(teacherCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        TeacherResponse result = teacherService.uploadPhoto(1L, buildImage());

        assertThat(teacherCaptor.getValue().getPhotoUrl()).isEqualTo("https://res.cloudinary.com/demo/new.jpg");
        assertThat(teacherCaptor.getValue().getPhotoPublicId()).isEqualTo("new-public-id");
        assertThat(result.photoUrl()).isEqualTo("https://res.cloudinary.com/demo/new.jpg");
    }

    @Test
    @DisplayName("uploadPhoto deletes the previous photo from Cloudinary when the teacher already had one")
    void uploadPhoto_DeletesPreviousPhoto_WhenTeacherAlreadyHadOne() throws IOException {
        Teacher teacher = buildTeacherWithPhoto(1L);
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(cloudinary.uploader()).thenReturn(uploader = mock(Uploader.class));
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(Map.of(
                "secure_url", "https://res.cloudinary.com/demo/new.jpg",
                "public_id", "new-public-id"));
        when(teacherRepository.save(any(Teacher.class))).thenAnswer(invocation -> invocation.getArgument(0));

        teacherService.uploadPhoto(1L, buildImage());

        verify(uploader).destroy(eq("old-public-id"), anyMap());
    }

    @Test
    @DisplayName("uploadPhoto throws TeacherNotFoundException when the teacher does not exist")
    void uploadPhoto_ThrowsWhenTeacherDoesNotExist() {
        when(teacherRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teacherService.uploadPhoto(1L, buildImage()))
                .isInstanceOf(TeacherNotFoundException.class);
    }

    @Test
    @DisplayName("uploadPhoto throws TeacherUploadException when Cloudinary fails")
    void uploadPhoto_ThrowsWhenCloudinaryFails() throws IOException {
        Teacher teacher = buildTeacher(1L);
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(cloudinary.uploader()).thenReturn(uploader = mock(Uploader.class));
        when(uploader.upload(any(byte[].class), anyMap())).thenThrow(new IOException("Cloudinary no responde"));

        assertThatThrownBy(() -> teacherService.uploadPhoto(1L, buildImage()))
                .isInstanceOf(TeacherUploadException.class);
    }

    @Test
    @DisplayName("delete removes the teacher when it exists and has no photo")
    void delete_RemovesExistingTeacher() {
        Teacher teacher = buildTeacher(1L);
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));

        teacherService.delete(1L);

        verify(teacherRepository).delete(teacher);
    }

    @Test
    @DisplayName("delete also removes the teacher's photo from Cloudinary when it had one")
    void delete_RemovesPhotoFromCloudinary_WhenTeacherHadOne() throws IOException {
        Teacher teacher = buildTeacherWithPhoto(1L);
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(cloudinary.uploader()).thenReturn(uploader = mock(Uploader.class));

        teacherService.delete(1L);

        verify(uploader).destroy(eq("old-public-id"), anyMap());
        verify(teacherRepository).delete(teacher);
    }

    @Test
    @DisplayName("delete throws TeacherUploadException when Cloudinary fails to remove the photo")
    void delete_ThrowsWhenCloudinaryFailsToRemovePhoto() throws IOException {
        Teacher teacher = buildTeacherWithPhoto(1L);
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(cloudinary.uploader()).thenReturn(uploader = mock(Uploader.class));
        when(uploader.destroy(anyString(), anyMap())).thenThrow(new IOException("Cloudinary no responde"));

        assertThatThrownBy(() -> teacherService.delete(1L))
                .isInstanceOf(TeacherUploadException.class);

        verify(teacherRepository, never()).delete(any(Teacher.class));
    }

    @Test
    @DisplayName("delete throws TeacherNotFoundException when the teacher does not exist")
    void delete_ThrowsWhenTeacherDoesNotExist() {
        when(teacherRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teacherService.delete(1L))
                .isInstanceOf(TeacherNotFoundException.class);

        verify(teacherRepository, never()).delete(any(Teacher.class));
    }
}
