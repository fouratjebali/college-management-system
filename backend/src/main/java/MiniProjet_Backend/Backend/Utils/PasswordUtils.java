package MiniProjet_Backend.Backend.Utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PasswordUtils {
    
    /**
     * Hash a password using a simple hashing algorithm
     * For production, use bcrypt or argon2
     */
    public static String hashPassword(String password) {
        return Integer.toHexString(password.hashCode());
    }

    /**
     * Verify a password against its hash
     */
    public static boolean verifyPassword(String password, String hash) {
        return hashPassword(password).equals(hash);
    }
}

