package mknv.psm.server.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 *
 * @author mknv
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class AesPasswordEncryptorTest {

    @Autowired
    private AesPasswordEncryptor encryptor;

    public AesPasswordEncryptorTest() {
    }

    @Test
    public void encrypt_decrypt() {
        String expected = "password";
        String encrypted = encryptor.encrypt("password");
        String decrypted = encryptor.decrypt(encrypted);
        assertEquals(expected, decrypted);
    }
}
