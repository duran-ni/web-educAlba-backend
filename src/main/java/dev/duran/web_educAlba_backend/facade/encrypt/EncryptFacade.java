package dev.duran.web_educAlba_backend.facade.encrypt;

public class EncryptFacade implements IEncryptFacade {

    private final IEncrypt encoder;

    public EncryptFacade(IEncrypt encoder) {
        this.encoder = encoder;
    }

    @Override
    public String encode(String type, String data) {
        String dataEncoded = "";

        if (type.equalsIgnoreCase("bcrypt")) {
            dataEncoded = encoder.encode(data);
        }

        return dataEncoded;
    }
}
