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
            return fields[0].equals(username) && fields[1].equals(serverIp.getHostAddress()) && System.currentTimeMillis() < expirationTime;
        } catch (GeneralSecurityException | NumberFormatException e){
            return false;
        }
    }

    public static final String CODE_PREFIX = "INTRA-";
    private static final String BASE36_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public record ConnectionTarget(String ip, int port) {
        @Override
        public String toString() {
            return ip + ":" + port;
        }
    }

    /**
     * Converts a server IP address and port into a compact, human-friendly Connection Code.
     * Example: "192.168.1.15" and 5000 -> "2334C-LQXZC" (10 alphanumeric chars)
     */
    public static String generateConnectionCode(String ip, int port) {
        if (ip == null || ip.trim().isEmpty() || "localhost".equalsIgnoreCase(ip.trim())) {
            ip = "127.0.0.1";
        }
        ip = ip.trim();
        if (port <= 0 || port > 65535) {
            port = com.IntraChat.network.Server.defaultPort;
        }

        // For standard IPv4 addresses, encode to compact 10-char Base36 (XXXXX-XXXXX)
        String[] octets = ip.split("\\.");
        if (octets.length == 4) {
            try {
                long b1 = Long.parseLong(octets[0].trim());
                long b2 = Long.parseLong(octets[1].trim());
                long b3 = Long.parseLong(octets[2].trim());
                long b4 = Long.parseLong(octets[3].trim());
                if (b1 >= 0 && b1 <= 255 && b2 >= 0 && b2 <= 255 && b3 >= 0 && b3 <= 255 && b4 >= 0 && b4 <= 255) {
                    long val = (b1 << 40) | (b2 << 32) | (b3 << 24) | (b4 << 16) | (port & 0xFFFFL);
                    StringBuilder sb = new StringBuilder();
                    while (val > 0) {
                        sb.insert(0, BASE36_CHARS.charAt((int) (val % 36)));
                        val /= 36;
                    }
                    while (sb.length() < 10) {
                        sb.insert(0, '0');
                    }
                    return sb.substring(0, 5) + "-" + sb.substring(5);
                }
            } catch (Exception ignored) {}
        }

        // Fallback for non-IPv4 hostnames: URL-safe Base64
        String combined = ip + ":" + port;
        return base64Encoder.encodeToString(combined.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Parses a single input (Connection Code, Base64 token, Hex code, or raw IP/IP:port)
     * back into the target IP address and port.
     */
    public static ConnectionTarget parseConnectionCode(String input) {
        if (input == null || input.trim().isEmpty()) {
            return new ConnectionTarget("127.0.0.1", com.IntraChat.network.Server.defaultPort);
        }
        input = input.trim();

        // 1. Remove optional prefixes
        String raw = input;
        if (raw.toUpperCase().startsWith(CODE_PREFIX)) {
            raw = raw.substring(CODE_PREFIX.length()).trim();
        } else if (raw.toUpperCase().startsWith("IC-")) {
            raw = raw.substring(3).trim();
        } else if (raw.toUpperCase().startsWith("CODE-")) {
            raw = raw.substring(5).trim();
        }

        // 2. Check compact Base36 code: "XXXXX-XXXXX" or "XXXXXXXXXX" (case-insensitive)
        String noHyphen = raw.replace("-", "").trim();
        if (noHyphen.length() == 10 && noHyphen.matches("^[0-9a-zA-Z]{10}$")) {
            try {
                long val = 0;
                String upper = noHyphen.toUpperCase();
                for (int i = 0; i < upper.length(); i++) {
                    char c = upper.charAt(i);
                    int digit = BASE36_CHARS.indexOf(c);
                    if (digit == -1) {
                        val = -1;
                        break;
                    }
                    val = val * 36 + digit;
                }
                if (val >= 0 && val < (1L << 48)) {
                    int b1 = (int) ((val >> 40) & 0xFF);
                    int b2 = (int) ((val >> 32) & 0xFF);
                    int b3 = (int) ((val >> 24) & 0xFF);
                    int b4 = (int) ((val >> 16) & 0xFF);
                    int portVal = (int) (val & 0xFFFF);
                    if (portVal > 0 && portVal <= 65535) {
                        return new ConnectionTarget(b1 + "." + b2 + "." + b3 + "." + b4, portVal);
                    }
                }
            } catch (Exception ignored) {}
        }

        // 3. Check 12-char compact Hex format (4 bytes IPv4 + 2 bytes port)
        if (noHyphen.matches("^[0-9a-fA-F]{12}$")) {
            try {
                int b1 = Integer.parseInt(noHyphen.substring(0, 2), 16);
                int b2 = Integer.parseInt(noHyphen.substring(2, 4), 16);
                int b3 = Integer.parseInt(noHyphen.substring(4, 6), 16);
                int b4 = Integer.parseInt(noHyphen.substring(6, 8), 16);
                int portVal = Integer.parseInt(noHyphen.substring(8, 12), 16);
                if (portVal > 0 && portVal <= 65535) {
                    return new ConnectionTarget(b1 + "." + b2 + "." + b3 + "." + b4, portVal);
                }
            } catch (Exception ignored) {}
        }

        // 4. Check 8-char 6-byte Base64 format
        if (raw.length() == 8 && !raw.contains(":")) {
            try {
                byte[] decoded = base64Decoder.decode(raw);
                if (decoded.length == 6) {
                    int b1 = decoded[0] & 0xFF;
                    int b2 = decoded[1] & 0xFF;
                    int b3 = decoded[2] & 0xFF;
                    int b4 = decoded[3] & 0xFF;
                    int portVal = ((decoded[4] & 0xFF) << 8) | (decoded[5] & 0xFF);
                    if (portVal > 0 && portVal <= 65535) {
                        return new ConnectionTarget(b1 + "." + b2 + "." + b3 + "." + b4, portVal);
                    }
                }
            } catch (Exception ignored) {}
        }

        // 5. Try Base64 / Base64URL decode
        try {
            byte[] decodedBytes = null;
            try {
                decodedBytes = base64Decoder.decode(raw);
            } catch (Exception e) {
                try {
                    decodedBytes = Base64.getDecoder().decode(raw);
                } catch (Exception ignored) {}
            }

            if (decodedBytes != null) {
                String decodedStr = new String(decodedBytes, StandardCharsets.UTF_8);
                if (decodedStr.contains(":")) {
                    int lastColon = decodedStr.lastIndexOf(':');
                    String ipPart = decodedStr.substring(0, lastColon).trim();
                    String portPart = decodedStr.substring(lastColon + 1).trim();
                    int portVal = Integer.parseInt(portPart);
                    if (portVal > 0 && portVal <= 65535 && !ipPart.isEmpty()) {
                        if (ipPart.equalsIgnoreCase("localhost")) ipPart = "127.0.0.1";
                        return new ConnectionTarget(ipPart, portVal);
                    }
                } else if (!decodedStr.trim().isEmpty() && decodedStr.matches("^\\d+\\.\\d+\\.\\d+\\.\\d+$")) {
                    return new ConnectionTarget(decodedStr.trim(), com.IntraChat.network.Server.defaultPort);
                }
            }
        } catch (Exception ignored) {}

        // 6. Check signed token: payload.signature
        if (input.contains(".") && input.length() > 20) {
            try {
                String[] dotParts = input.split("\\.");
                if (dotParts.length == 2) {
                    String decodedPayload = new String(base64Decoder.decode(dotParts[0]), StandardCharsets.UTF_8);
                    String[] payloadFields = decodedPayload.split("\\|", -1);
                    if (payloadFields.length >= 2 && !payloadFields[1].isEmpty()) {
                        String ip = payloadFields[1].trim();
                        if (ip.equalsIgnoreCase("localhost")) ip = "127.0.0.1";
                        return new ConnectionTarget(ip, com.IntraChat.network.Server.defaultPort);
                    }
                }
            } catch (Exception ignored) {}
        }

        // 7. Check direct IP:port
        if (input.contains(":")) {
            try {
                int lastColon = input.lastIndexOf(':');
                String ipPart = input.substring(0, lastColon).trim();
                String portPart = input.substring(lastColon + 1).trim();
                int portVal = Integer.parseInt(portPart);
                if (portVal > 0 && portVal <= 65535) {
                    if (ipPart.isEmpty() || ipPart.equalsIgnoreCase("localhost")) ipPart = "127.0.0.1";
                    return new ConnectionTarget(ipPart, portVal);
                }
            } catch (Exception ignored) {}
        }

        // 8. Direct IP or hostname
        String ip = input;
        if (ip.equalsIgnoreCase("localhost")) {
            ip = "127.0.0.1";
        }
        return new ConnectionTarget(ip, com.IntraChat.network.Server.defaultPort);
    }

    public static String extractIpFromCodeOrInput(String input) {
        return parseConnectionCode(input).ip();
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
