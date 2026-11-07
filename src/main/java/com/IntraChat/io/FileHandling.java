package com.IntraChat.io;

import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.nio.file.*; 


public class FileHandling {
    //If user in user data file exists, then login is successful, else login fails and register the new user by adding the user data to the file.
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
    
    public static void main(String[] args) {

        //Entering new user data and saving it to a file
        /*Scanner input = new Scanner(System.in);
        
        System.out.println("Enter your username:");
        String username = input.nextLine();
        System.out.println("Enter your password:");
        String password = input.nextLine();
        System.out.println("Enter your code:");
        String code = input.nextLine();

        try {
            File file = new File("user_data.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            
            FileWriter writer = new FileWriter(file, true);
            writer.write(username + "," + password + "," + code + "\n");
            writer.close();
            
            System.out.println("User data saved successfully.");
        } catch (IOException e) {
            System.out.println("An error occurred while saving user data.");
            e.printStackTrace();
        }
        input.close();
    }*/

    
}