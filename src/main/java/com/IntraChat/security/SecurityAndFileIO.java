package main.java.com.IntraChat.security;

import java.nio.charset.StandardCharsets;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.net.InetAddress;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

public class SecurityAndFileIO {
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final long Day = 24 * 60 * 60 * 1000L; // 24 hours in milliseconds
    
    //Generate: encrypt "username|ip| password" using AES and return the encrypted string
    public static String encrypt(String username, InetAddress ip, String password) throws GeneralSecurityException {
        String combined = username + ":" + ip.getHostAddress() + ":" + password;
        byte[] keyBytes = Arrays.copyOf(combined.getBytes(StandardCharsets.UTF_8), 16); // AES key size is 16 bytes
        SecretKey secretKey = new javax.crypto.spec.SecretKeySpec(keyBytes, "AES");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(combined.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }
    //Verify: decrypt the encrypted string and check if it matches "username|ip|password"
    public static boolean verify(String encrypted, String username, InetAddress ip, String password) throws GeneralSecurityException {
        byte[] keyBytes = Arrays.copyOf((username + ":" + ip.getHostAddress() + ":" + password).getBytes(StandardCharsets.UTF_8), 16);
        SecretKey secretKey = new javax.crypto.spec.SecretKeySpec(keyBytes, "AES");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encrypted));
        String decryptedString = new String(decryptedBytes, StandardCharsets.UTF_8);

        return decryptedString.equals(username + ":" + ip.getHostAddress() + ":" + password);
    }
    //Split the encrypted string into username, ip, and password
    public static String[] splitEncrypted(String encrypted) throws GeneralSecurityException {
        byte[] decodedBytes = Base64.getDecoder().decode(encrypted);
        String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);
        return decodedString.split(":");
    }
    //Demonstrate the usage of the encrypt and verify methods
    public static void main(String[] args) {
        try {
            String username = "testUser";
            InetAddress ip = InetAddress.getByName("10.103.214.107");
            String password = "testPassword";
            String encrypted = encrypt(username, ip, password);
            boolean isValid = verify(encrypted, username, ip, password);
            System.out.println("Encrypted: " + encrypted);
            System.out.println("Is Valid: " + isValid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
