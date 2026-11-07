package com.IntraChat.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.Base64;
import java.util.Scanner;

public class FileHandling {

    public static String encodeFileToBase64(File file) throws IOException {
        byte[] bytes = Files.readAllBytes(file.toPath());
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static File saveBase64ToFile(String base64Data, File targetFile) throws IOException {
        byte[] bytes = Base64.getDecoder().decode(base64Data);
        if (targetFile.getParentFile() != null && !targetFile.getParentFile().exists()) {
            targetFile.getParentFile().mkdirs();
        }
        try (FileOutputStream fos = new FileOutputStream(targetFile)) {
            fos.write(bytes);
        }
        return targetFile;
    }

    public static String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[] { "B", "KB", "MB", "GB" };
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        if (digitGroups >= units.length) digitGroups = units.length - 1;
        return new DecimalFormat("#,##0.#").format(bytes / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static File getDefaultDownloadsDirectory() {
        String userHome = System.getProperty("user.home");
        File downloads = new File(userHome, "Downloads");
        if (downloads.exists() && downloads.isDirectory()) {
            return downloads;
        }
        File chatDownloads = new File(userHome, "IntraChat_Downloads");
        if (!chatDownloads.exists()) {
            chatDownloads.mkdirs();
        }
        return chatDownloads;
    }

    // Legacy login method preserved for backward compatibility
    public static boolean login(String username, String password, String code) {
        try {
            File file = new File("user_data.txt");
            if (!file.exists()) {
                return false; // User data file does not exist
            }
            
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] userData = line.split(",");
                if (userData.length >= 3 && userData[0].equals(username) && userData[1].equals(password) && userData[2].equals(code)) {
                    scanner.close();
                    return true; // Login successful
                }
            }
            scanner.close();
        } catch (IOException e) {
            System.out.println("An error occurred while reading user data.");
            e.printStackTrace();
        }
        return false; // Login failed
    }
}