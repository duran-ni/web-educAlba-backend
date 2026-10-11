package dev.duran.web_educAlba_backend.facade.encrypt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EncryptFacadeTest {

    @Mock
    private IEncrypt encoder;

    @Test
    void testEncode_WithBcryptType_DelegatesToEncoder() {
        when(encoder.encode("plain-password")).thenReturn("hashed-password");
        EncryptFacade encryptFacade = new EncryptFacade(encoder);

        String result = encryptFacade.encode("bcrypt", "plain-password");

        assertThat(result).isEqualTo("hashed-password");
    }

    @Test
    void testEncode_WithUnsupportedType_ReturnsEmptyStringWithoutCallingEncoder() {
        EncryptFacade encryptFacade = new EncryptFacade(encoder);

        String result = encryptFacade.encode("md5", "plain-password");

        assertThat(result).isEmpty();
        verifyNoInteractions(encoder);
    }
}
