package mknv.psm.server.util;

import java.util.Random;
import org.springframework.stereotype.Component;

/**
 *
 * @author mknv
 */
@Component
public class SimplePasswordGenerator implements PasswordGenerator {

    private final Random random = new Random();

    @Override
    public String generate(int length, PasswordType passwordType) {
        if (length <= 0 || length > 100) {
            throw new IllegalArgumentException("The length must be between 1 and 100.");
        }
        if (passwordType == null) {
            throw new IllegalArgumentException("The passwordType is null.");
        }
        StringBuilder symbols = new StringBuilder();
        symbols.append("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
        if (passwordType == PasswordType.COMPLEX) {
            symbols.append("_!#$%^&()-+=*");
        }
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomInt = random.nextInt(symbols.length());
            result.append(symbols.charAt(randomInt));
        }
        return result.toString();
    }
}
