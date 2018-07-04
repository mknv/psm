package mknv.psm.server.util;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;

/**
 *
 * @author Mikhail Konovalov
 */
@Component
@ConfigurationProperties(prefix = "password.encryptor")
public class AesPasswordEncryptor implements PasswordEncryptor {

    private String key;
    private String salt;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    @Override
    public String encrypt(String rawPassword) {
        TextEncryptor encryptor = Encryptors.delux(key, salt);
        String result = encryptor.encrypt(rawPassword);
        return result;
    }

    @Override
    public String decrypt(String encryptedPassword) {
        TextEncryptor decryptor = Encryptors.delux(key, salt);
        return decryptor.decrypt(encryptedPassword);
    }
}
