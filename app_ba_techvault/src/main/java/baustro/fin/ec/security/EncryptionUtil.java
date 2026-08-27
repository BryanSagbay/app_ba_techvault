package baustro.fin.ec.security;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

public class EncryptionUtil {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int KEY_LENGTH = 256;
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH = 128;

    // Clave derivada de la clave maestra con PBKDF2
    private static SecretKey derivedKey;
    private static String masterPassword;

    public static void setMasterPassword(String password) {
        masterPassword = password;
        derivedKey = null; // reset
    }

    public static boolean hasMasterPassword() {
        return masterPassword != null && !masterPassword.isEmpty();
    }

    private static SecretKey getDerivedKey() throws Exception {
        if (derivedKey == null) {
            byte[] salt = "TechOpsManagerSalt2024".getBytes(StandardCharsets.UTF_8);
            PBEKeySpec spec = new PBEKeySpec(
                    masterPassword.toCharArray(), salt, 65536, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] keyBytes = factory.generateSecret(spec).getEncoded();
            derivedKey = new SecretKeySpec(keyBytes, "AES");
        }
        return derivedKey;
    }

    public static String encrypt(String plainText) throws Exception {
        SecretKey key = getDerivedKey();
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH, iv));
        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        // Prepend IV to encrypted data
        byte[] combined = new byte[IV_LENGTH + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, IV_LENGTH);
        System.arraycopy(encrypted, 0, combined, IV_LENGTH, encrypted.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    public static String decrypt(String encryptedText) throws Exception {
        SecretKey key = getDerivedKey();
        byte[] combined = Base64.getDecoder().decode(encryptedText);

        byte[] iv = Arrays.copyOfRange(combined, 0, IV_LENGTH);
        byte[] encrypted = Arrays.copyOfRange(combined, IV_LENGTH, combined.length);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH, iv));
        return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
    }

    public static void clearMasterPassword() {
        masterPassword = null;
        derivedKey = null;
    }
}
