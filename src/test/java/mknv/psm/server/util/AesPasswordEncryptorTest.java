package mknv.psm.server.util;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import mknv.psm.server.PsmServer;

/**
 *
 * @author mknv
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = PsmServer.class)
@ActiveProfiles("test")
public class AesPasswordEncryptorTest {

    @Autowired
    private AesPasswordEncryptor encryptor;
    private final String testString = "password1";

    public AesPasswordEncryptorTest() {
    }
    
    @Test
    public void should_load_properties_from_configuration_file(){
        assertNotNull(encryptor.getKey());
        assertNotNull(encryptor.getSalt());
    }

    @Test
    public void encrypt_decrypt_OK() {
        System.out.println("ENCRYPT DECRYPT OK");
        String encoded = encryptor.encrypt(testString);
        String decoded = encryptor.decrypt(encoded);
        assertEquals(testString, decoded);
    }
}
