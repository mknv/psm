package mknv.psm.server.util;

/**
 *
 * @author mknv
 */
public interface PasswordEncryptor {

    String encrypt(String rawPassword);

    String decrypt(String encryptedPassword);
}
