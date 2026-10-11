package dev.duran.web_educAlba_backend.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import dev.duran.web_educAlba_backend.academy.student.StudentNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    void testHandleRuntimeException_WithoutResponseStatus_ReturnsInternalServerError() {
        when(request.getRequestURI()).thenReturn("/api/admin/workshops");
        RuntimeException unexpectedException = new RuntimeException("Unexpected failure");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleRuntimeException(unexpectedException, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().message()).isEqualTo("Unexpected failure");
    }

    @Test
    void testHandleRuntimeException_WithResponseStatus_UsesItsOwnStatus() {
        when(request.getRequestURI()).thenReturn("/api/admin/students/999");
        RuntimeException notFoundException = new StudentNotFoundException(999L);

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleRuntimeException(notFoundException, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
