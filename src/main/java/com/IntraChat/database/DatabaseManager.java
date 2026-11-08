package com.IntraChat.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 *Manages all the database operations for the chat application
 * Handles user accounts, authentication records
 * and message storage using SQLite
 * @author Bonginkosi
 */
public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:chat.db";
    public record Message(String sender, String recipient, String content, String timestamp) {}

    /**
     *Establishes a connection to the SQLite database
     * 
     * @return
     * @throws SQLException
     */
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    /*hgngmhn */

    /**
     *Creates the needed database tables if they do mot already exist.
     * Initializes the users and messages tables during application startup.
     */

    public static void initializeDatabase() {
        // SQL statement used to create the users table.
        String createUsersTable = "CREATE TABLE IF NOT EXISTS users ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " username TEXT NOT NULL UNIQUE,"
                + "password TEXT NOT NULL,"
                + " created_at TEXT DEFAULT CURRENT_TIMESTAMP"
                + ");";
        // SQL statement used to store chat messages
        String createMessagesTable = "CREATE TABLE IF NOT EXISTS messages ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " sender TEXT NOT NULL,"
                + " recipient TEXT NOT NULL,"
                + " content TEXT NOT NULL,"
                + " timestamp TEXT DEFAULT CURRENT_TIMESTAMP"
                + ");";
        //Execute table crreation statements.
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createUsersTable);
            stmt.execute(createMessagesTable);
            System.out.println("(DB) Database tables initialized successfully.");

        } catch (SQLException e) {
            System.err.println("(DB ERROR) Table creation failed: " + e.getMessage());
        }
    }

    // NB: using PreparedStatement to prevents SQL Injection.

    /**
     *Registers a new user in the database
     * 
     * @param username
     * @param password
     */
    public static void addUser(String username, String password) {
        String sql = "INSERT INTO users(username, password) VALUES(?, ?)";

        try (Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
            System.out.println("(DB) User registered: " + username);

        } catch (SQLException e) {
            System.err.println("(DB ERROR) Could not add user: " + e.getMessage());
        }
    }

    /**
     *Stores a chat in the database
     * 
     * @param sender
     * @param recipient
     * @param content
     */
    public static void logMessage(String sender, String recipient, String content) {
        String sql = "INSERT INTO messages(sender, recipient, content) VALUES(?, ?, ?)";

        try (Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, sender);
            pstmt.setString(2, recipient);
            pstmt.setString(3, content);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("(DB ERROR) Could not log message: " + e.getMessage());
        }
    }

    /**
     *Retrieves and displays all stored chat messages from the database
     * Messages are displayed in chronological order based on their ID.
     * 
     */
    public static void displayChatHistory() {
        String sql = "SELECT sender, recipient, content, timestamp FROM messages ORDER BY id ASC";
        //SQL query used to retrieve all chat messages.
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
             //Display chat histoy heading.
            System.out.println("\n--- STORED CHAT HISTORY ---");
            //Process and display each message retrieved from the database.
            while (rs.next()) {
                String sender = rs.getString("sender");
                String recipient = rs.getString("recipient");
                String content = rs.getString("content");
                String timestamp = rs.getString("timestamp");
                //Display database retrieval errors
                System.out.printf("[%s] %s -> %s: %s%n", timestamp, sender, recipient, content);
            }

        } catch (SQLException e) {
            System.err.println("(DB ERROR) Fetch failed: " + e.getMessage());
        }
    }

    /**
     *Authenticates a user by verifying the supplied
     * username and password against the records stored in the database
     * 
     * @param username The username entered by the user
     * @param password The password entered by the user
     * @return true if the credentials are valid 
     * otherwise false
     */
    public static boolean authenticateUser(String username, String password) {
        // SQL query used to find a matching user account
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        // Establish a database connection and prepare the query
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            //Insert the user's credentials into the prepared statement
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            //Execute the query and check whether a matching record record exixts 
            ResultSet rs = pstmt.executeQuery();

            return rs.next();
        
        } 
        //Handle database errors during authentication
        catch (SQLException e) {
            System.err.println("(DB ERROR) Authentication failed: " + e.getMessage());
            return false;
        }
    }

    /**
     *Retrieves all stored chat messages from the database
     * and returns them as a list of Message objects.
     * @return An ArrayList containing the complete chat history.
     */
    public static ArrayList<Message> getChatHistoryArrayList() {
        // SQL query used to retrieve all chat messages in chronological order.
        String sql = "SELECT sender, recipient, content, timestamp FROM messages ORDER BY id ASC";
        // Create a list to store the retrieved messages.
        ArrayList<Message> chatHistory = new ArrayList<>();
        // Connect to the database and execute the query.
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            // Process each record and convert it into a Message object.
            while (rs.next()) {
                String sender = rs.getString("sender");
                String recipient = rs.getString("recipient");
                String content = rs.getString("content");
                String timestamp = rs.getString("timestamp");
                // Add the message to the chat history collection.
                chatHistory.add(new Message(sender, recipient, content, timestamp));
            }

        } // Handle database retrieval errors.
        catch (SQLException e) {
            System.err.println("(DB ERROR) Fetch failed: " + e.getMessage());
        }
        // Return the complete list of chat messages.
        return chatHistory;
    }

    /* 
        Usage:

            initializeDatabase();

            addUser("Alice", "password123");
            addUser("Bob", "password456");


            logMessage("Alice", "Bob", "Hello Bob!");
            logMessage("Bob", "Alice", "Hey Alice, how are you?");

            authenticateUser("Alice", "password123"); //returns true
            authenticateUser("Bob", "password456"); //returns true

            displayChatHistory();
    */
}