package com.elvarg.util;

import com.password4j.Hash;
import com.password4j.PBKDF2Function;
import com.password4j.Password;
import com.password4j.types.Hmac;

import java.util.Base64;

public class PasswordUtil {

    // OWASP-recommended work factor for PBKDF2-HMAC-SHA512 (>= 210,000 iterations).
    private static PBKDF2Function pbkdf2 = PBKDF2Function.getInstance(Hmac.SHA512, 210000, 512);

    /**
     * Determines whether a stored value is in the salted-hash format produced by
     * {@link #generatePasswordHashWithSalt(String)} (base64 salt ":" base64 hash),
     * as opposed to a legacy plaintext password.
     */
    public static boolean isHashed(String value) {
        if (value == null) {
            return false;
        }
        String[] parts = value.split(":");
        if (parts.length != 2) {
            return false;
        }
        try {
            Base64.getDecoder().decode(parts[0]);
            Base64.getDecoder().decode(parts[1]);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static String generatePasswordHashWithSalt(String password) {
        Hash hash = Password.hash(password).addRandomSalt().with(pbkdf2);

        return toBase64(hash.getSalt()) + ":" + toBase64(hash.getResult());
    }

    public static Boolean passwordsMatch(String plainTextPassword, String passwordHashWithSalt) {
        var parts = passwordHashWithSalt.split(":");

        var salt = fromBase64(parts[0]);
        var passwordHash = fromBase64(parts[1]);

        return Password.check(plainTextPassword, passwordHash).addSalt(salt).with(pbkdf2);
    }

    private static String toBase64(String s) {
        return Base64.getEncoder().encodeToString(s.getBytes());
    }

    private static String fromBase64(String s) {
        byte[] decodedBytes = Base64.getDecoder().decode(s);
        return new String(decodedBytes);
    }
}
