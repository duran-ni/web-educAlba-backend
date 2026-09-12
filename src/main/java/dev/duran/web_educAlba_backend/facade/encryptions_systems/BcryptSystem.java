package dev.duran.web_educAlba_backend.facade.encryptions_systems;

import org.springframework.security.crypto.password.PasswordEncoder;

import dev.duran.web_educAlba_backend.facade.encrypt.IEncrypt;

public class BcryptSystem implements IEncrypt {

    private final PasswordEncoder encoder;

    public BcryptSystem(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public String encode(String data) {
        return encoder.encode(data);
    }
}
