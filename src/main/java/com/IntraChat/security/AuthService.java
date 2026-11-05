package com.IntraChat.security;

import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.net.InetAddress;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class AuthService {
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final long Day = 24 * 60 * 60 * 1000L; // 24 hours in milliseconds
    
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int PBKDF2_ITERATIONS = 210_000;
    private static final int SALT_BYTES = 16;
    private static final int HASH_BYTES = 32;
    private static final int MIN_KEY_BYTES = 32; // Minimum key length for HMAC-SHA256

    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder base64Decoder = Base64.getUrlDecoder();

    //Server code. Generatesa random signing key. Generate it once, store it securely and reuses it. If it changes or is lost, every issued token becomes invalid.
    public static byte[] generateSigningKey() {
        byte[] key = new byte[MIN_KEY_BYTES]; // 256 bits
        secureRandom.nextBytes(key);
        return key;
    }

    //Password hashing (storing returned string, never the password itself). Returns a string containing the salt and the hash, separated by a colon.
    public static String hashPassword(String password) throws GeneralSecurityException {
        if(password == null){
            throw new IllegalArgumentException("Password cannot be null");
        }
        byte[] salt = new byte[SALT_BYTES];
        secureRandom.nextBytes(salt);
        byte[] hash = pbkdf2(password.toCharArray(), salt, PBKDF2_ITERATIONS);
        return PBKDF2_ITERATIONS + ":" + base64Encoder.encodeToString(salt) + ":" + base64Encoder.encodeToString(hash);
    }

    //Password verification. Takes the stored string (salt:hash) and the password to verify. Returns true if the password is correct, false otherwise.
    public static boolean verifyPassword(String password, String stored) throws GeneralSecurityException {
        if(password == null || stored == null){
            return false;
        }
        try{
            String[] parts = stored.split(":");
            if (parts.length != 3){
                return false; // Invalid stored format
            }
            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = base64Decoder.decode(parts[1]);
            byte[] expectedHash = base64Decoder.decode(parts[2]);
            byte[] actualHash = pbkdf2(password.toCharArray(), salt, iterations);
            return MessageDigest.isEqual(expectedHash, actualHash); //constant-time comparison to prevent timing attacks
        } catch (GeneralSecurityException | IllegalArgumentException e){
            return false; // Invalid stored format or decoding error
        }
    }
    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations) throws GeneralSecurityException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, HASH_BYTES * 8);
        try{
            return SecretKeyFactory.getInstance(PBKDF2_ALGORITHM).generateSecret(spec).getEncoded();
        }finally{
            spec.clearPassword(); // Clear password from memory
        }
    }

    //Creates a code "payload.signature" that binds a user to an IP address and expires after 24 hours. The payload is signed, not encrypted, so it can be read by anyone, but cannot be tampered with without invalidating the signature. 
    public static String generateCode(byte[] serverKey, String username, InetAddress serverIp) throws GeneralSecurityException{
        requiredValidKey(serverKey);
        if (username == null || serverIp == null){
            throw new IllegalArgumentException("Username and server IP cannot be null");
        }
        
        long expirationTime = System.currentTimeMillis() + Day;
        String payload = base64Encoder.encodeToString(username.getBytes(StandardCharsets.UTF_8))+"|"+serverIp.getHostAddress()+"|"+expirationTime;
        String encodedPayload = base64Encoder.encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        String signature = base64Encoder.encodeToString(hmacSHA256(serverKey, encodedPayload));
        return encodedPayload + "." + signature;
    }

    //Verifies a code. Returns true if the code is valid, false otherwise.
    public static boolean verifyCode(byte[] serverKey, String code, String username, InetAddress serverIp){
        if (code == null || username == null || serverIp == null){
            return false;
        }
        try{
            String[] fields = splitCode(serverKey, code);
            long expirationTime = Long.parseLong(fields[2]);
            return fields[0].equals(serverIp.getHostAddress()) && fields[1].equals(username) && System.currentTimeMillis() < expirationTime;
        } catch (GeneralSecurityException | NumberFormatException e){
            return false;
        }
    }

    //Verifies the signature, then returns username, ip, expirationTime. Throws GeneralSecurityException if the code is invalid or is malformed.
    public static String[] splitCode(byte[] serverKey, String code) throws GeneralSecurityException{
        requiredValidKey(serverKey);
        if (code == null){
            throw new GeneralSecurityException("Code cannot be null");
        }
        try{
            String[] parts = code.split("\\.",-1);
            if (parts.length != 2){
                throw new GeneralSecurityException("Malformed code");
            }
            byte[] expectedSignature = hmacSHA256(serverKey, parts[0]);
            byte[] actualSignature = base64Decoder.decode(parts[1]);
            if (!MessageDigest.isEqual(expectedSignature, actualSignature)){
                throw new GeneralSecurityException("Invalid signature");
            }

            String payload = new String(base64Decoder.decode(parts[0]), StandardCharsets.UTF_8);
            String[] fields = payload.split("\\|",-1);
            if (fields.length != 3){
                throw new GeneralSecurityException("Malformed payload");
            }
            String username = new String(base64Decoder.decode(fields[0]), StandardCharsets.UTF_8);
            return new String[]{username,fields[1], fields[2]}; //username, ip, expirationTime

        } catch (IllegalArgumentException e){
            throw new GeneralSecurityException("Malformed code", e);
        }
    }

    private static void requiredValidKey(byte[] serverKey){
        if (serverKey == null || serverKey.length < MIN_KEY_BYTES){
            throw new IllegalArgumentException("Invalid server key: " + MIN_KEY_BYTES + " bytes required");
        }
    }
    private static byte[] hmacSHA256(byte[] key, String data) throws GeneralSecurityException {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(key, HMAC_ALGORITHM);
        mac.init(keySpec);
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    //Demo

    public static void main(String[] args){
        try{
            String username = "testUser";
            InetAddress serverIp = InetAddress.getByName("10.103.214.107");
            InetAddress wrongIp = InetAddress.getByName("10.103.214.108");
            String password = "testPassword";

            //Password storage
            String storedPassword = hashPassword(password);
            System.out.println("Stored password: " + storedPassword);
            System.out.println("Password verification (correct): " + verifyPassword(password, storedPassword));
            System.out.println("Password verification (incorrect): " + verifyPassword("wrongPassword", storedPassword));

            byte[] serverKey = generateSigningKey();
            String code = generateCode(serverKey, username, serverIp);
            System.out.println("Generated code: " + code);
            System.out.println("Code verification (correct): " + verifyCode(serverKey, code, username, serverIp));
            System.out.println("Code verification (wrong IP): " + verifyCode(serverKey, code, username, wrongIp));
            System.out.println("Code verification (wrong username): " + verifyCode(serverKey, code, "wrongUser", serverIp));
            
            char first = code.charAt(0) == 'A' ? 'B' : 'A'; // Change first character to invalidate the code
            String tamperedCode = first + code.substring(1);
            System.out.println("Tampered code: " + verifyCode(serverKey, tamperedCode, username, serverIp));

            String[] fields = splitCode(serverKey, code);
            System.out.println("Split code: username=" + fields[0] + ", ip=" + fields[1] + ", expirationTime=" + fields[2]);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
