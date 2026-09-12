package dev.duran.web_educAlba_backend.facade.encryptions_systems;

import java.util.Base64;
import dev.duran.web_educAlba_backend.facade.decrypt.IDecoder;

public class Base64System implements IDecoder {
    @Override
    public String decode(String data) {
        byte[] decodedBytes = Base64.getDecoder().decode(data);
        return new String(decodedBytes);
    }
}
