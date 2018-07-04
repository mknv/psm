package mknv.psm.server.util;

/**
 *
 * @author mknv
 */
public interface PasswordGenerator {
    
    String generate(int length, PasswordType passwordType);
}
