package dev.duran.web_educAlba_backend.facade.decrypt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DecryptFacadeTest {

    @Mock
    private IDecoder decoder;

    @Test
    void testDecode_WithBase64Type_DelegatesToDecoder() {
        when(decoder.decode("ZW5jb2RlZA==")).thenReturn("decoded");
        DecryptFacade decryptFacade = new DecryptFacade(decoder);

        String result = decryptFacade.decode("base64", "ZW5jb2RlZA==");

        assertThat(result).isEqualTo("decoded");
    }

    @Test
    void testDecode_WithUnsupportedType_ReturnsEmptyStringWithoutCallingDecoder() {
        DecryptFacade decryptFacade = new DecryptFacade(decoder);

        String result = decryptFacade.decode("rot13", "anything");

        assertThat(result).isEmpty();
        verifyNoInteractions(decoder);
    }
}
