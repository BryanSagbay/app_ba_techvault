package baustro.fin.ec.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * Cifrado AES-256 para el gestor de contraseñas.
 * La clave maestra se define aquí — puedes cambiarla antes de compilar.
 */
public class CryptoUtil {

    // ⚠️  CAMBIA ESTO antes de usar en producción
    private static final String MASTER_KEY = "OpsManager@Secure#2024!BancoBaustro";
    private static final String SALT = "OpsMgrSalt8291";
    private static final byte[] IV = "OpsManager16ByteIV".substring(0, 16).getBytes();

    private static SecretKey getKey() throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(MASTER_KEY.toCharArray(), SALT.getBytes(), 65536, 256);
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    public static String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, getKey(), new IvParameterSpec(IV));
            byte[] encrypted = cipher.doFinal(plainText.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            e.printStackTrace();
            return plainText;
        }
    }

    public static String decrypt(String encryptedText) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, getKey(), new IvParameterSpec(IV));
            byte[] decoded = Base64.getDecoder().decode(encryptedText);
            return new String(cipher.doFinal(decoded), "UTF-8");
        } catch (Exception e) {
            return "[Error al descifrar]";
        }
    }
}
