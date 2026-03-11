package baustro.fin.ec.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * Utilidad de cifrado AES-256-GCM para el gestor de contraseñas.
 * - Las contraseñas se cifran con AES-256-GCM
 * - El master password se valida con PBKDF2WithHmacSHA256
 * - Nunca se guarda el master password en texto plano
 */
public class CryptoUtil {

    private static final String ALGORITHM      = "AES/GCM/NoPadding";
    private static final String KEY_ALGORITHM  = "AES";
    private static final String KDF_ALGORITHM  = "PBKDF2WithHmacSHA256";
    private static final int    KEY_LENGTH     = 256;
    private static final int    GCM_TAG_LENGTH = 128;
    private static final int    GCM_IV_LENGTH  = 12;
    private static final int    SALT_LENGTH    = 16;
    private static final int    KDF_ITERATIONS = 310_000;

    // Clave de sesión derivada del master password (solo vive en memoria)
    private static SecretKey sessionKey;

    // ----------------------------------------------------------------
    // MASTER PASSWORD
    // ----------------------------------------------------------------

    /** Genera un salt aleatorio en Base64 */
    public static String generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /** Hashea el master password con PBKDF2 para guardar en config */
    public static String hashMasterPassword(String password, String saltB64) throws Exception {
        byte[] salt = Base64.getDecoder().decode(saltB64);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(KDF_ALGORITHM);
        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(), salt, KDF_ITERATIONS, KEY_LENGTH);
        byte[] hash = skf.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(hash);
    }

    /** Verifica si el master password ingresado coincide con el hash guardado */
    public static boolean verifyMasterPassword(String input, String storedHash, String saltB64) {
        try {
            String inputHash = hashMasterPassword(input, saltB64);
            return MessageDigest.isEqual(
                    Base64.getDecoder().decode(inputHash),
                    Base64.getDecoder().decode(storedHash));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Carga el master password en sesión derivando la clave AES.
     * Debe llamarse después de verificar correctamente el master password.
     */
    public static void loadSessionKey(String password, String saltB64) throws Exception {
        byte[] salt = Base64.getDecoder().decode(saltB64);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(KDF_ALGORITHM);
        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(), salt, KDF_ITERATIONS, KEY_LENGTH);
        byte[] keyBytes = skf.generateSecret(spec).getEncoded();
        sessionKey = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
    }

    /** Limpia la clave de sesión de memoria (al cerrar el módulo) */
    public static void clearSession() {
        sessionKey = null;
    }

    public static boolean isSessionActive() {
        return sessionKey != null;
    }

    // ----------------------------------------------------------------
    // CIFRADO / DESCIFRADO
    // ----------------------------------------------------------------

    /** Cifra un texto con AES-256-GCM usando la clave de sesión */
    public static String encrypt(String plaintext) throws Exception {
        if (sessionKey == null) throw new IllegalStateException("Sesión no activa");

        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, sessionKey,
                new GCMParameterSpec(GCM_TAG_LENGTH, iv));

        byte[] ciphertext = cipher.doFinal(plaintext.getBytes("UTF-8"));

        // Formato: iv(12 bytes) + ciphertext → Base64
        byte[] combined = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    /** Descifra un texto cifrado con AES-256-GCM usando la clave de sesión */
    public static String decrypt(String encryptedB64) throws Exception {
        if (sessionKey == null) throw new IllegalStateException("Sesión no activa");

        byte[] combined = Base64.getDecoder().decode(encryptedB64);
        byte[] iv         = new byte[GCM_IV_LENGTH];
        byte[] ciphertext = new byte[combined.length - GCM_IV_LENGTH];
        System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);
        System.arraycopy(combined, GCM_IV_LENGTH, ciphertext, 0, ciphertext.length);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, sessionKey,
                new GCMParameterSpec(GCM_TAG_LENGTH, iv));

        return new String(cipher.doFinal(ciphertext), "UTF-8");
    }
}
