package com.IntraChat.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import com.IntraChat.security.AuthService;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:chat.db";
    public record Message(String sender, String recipient, String content, String timestamp) {}
    public record UserRecord(int id, String username, String isAllowed, String createdAt) {}

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("(DB WARNING) SQLite JDBC driver not found: " + e.getMessage());
        }
    }

    public static Connection connect() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA journal_mode=WAL;");
            stmt.execute("PRAGMA busy_timeout=5000;");
        } catch (SQLException ignored) {}
        return conn;
    }

    public static void initializeDatabase() {
        String createUsersTable = "CREATE TABLE IF NOT EXISTS users ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " username TEXT NOT NULL UNIQUE,"
                + " password TEXT NOT NULL,"
                + " isAllowed TEXT NOT NULL DEFAULT 'false',"   
                + " created_at TEXT DEFAULT CURRENT_TIMESTAMP"
                + ");";

        String createMessagesTable = "CREATE TABLE IF NOT EXISTS messages ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " sender TEXT NOT NULL,"
                + " recipient TEXT NOT NULL,"
                + " content TEXT NOT NULL,"
                + " timestamp TEXT DEFAULT CURRENT_TIMESTAMP"
                + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createUsersTable);
            stmt.execute(createMessagesTable);

            // Schema migration in case users table existed without isAllowed column
            try {
                stmt.execute("ALTER TABLE users ADD COLUMN isAllowed TEXT NOT NULL DEFAULT 'true'");
            } catch (SQLException ignored) {
                // Column already exists
            }

            System.out.println("(DB) Database tables initialized successfully.");

        } catch (SQLException e) {
            System.err.println("(DB ERROR) Table creation failed: " + e.getMessage());
        }
    }

    public static boolean userExists(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("(DB ERROR) Could not check user: " + e.getMessage());
            return false;
        }
    }

    public static void addUser(String username, String password) {
        addUser(username, password, "false");
    }

    public static void addUser(String username, String password, String isAllowed) {
        String sql = "INSERT INTO users(username, password, isAllowed) VALUES(?, ?, ?)";

        String storedPassword = password;
        try {
            storedPassword = AuthService.hashPassword(password);
        } catch (Exception e) {
            System.err.println("(DB WARNING) Could not hash password, storing fallback: " + e.getMessage());
        }

        try (Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, storedPassword);
            pstmt.setString(3, isAllowed != null ? isAllowed : "true");
            pstmt.executeUpdate();
            System.out.println("(DB) User registered: " + username);

        } catch (SQLException e) {
            System.err.println("(DB ERROR) Could not add user: " + e.getMessage());
        }
    }

    public static boolean authenticateUser(String username, String password) {
        return authenticateUser(username, password, null);
    }

    public static boolean authenticateUser(String username, String password, String requiredIsAllowed) {
        String sql = "SELECT password, isAllowed FROM users WHERE username = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");
                String isAllowed = rs.getString("isAllowed");

                if (requiredIsAllowed != null && !requiredIsAllowed.equalsIgnoreCase(isAllowed)) {
                    return false;
                }

                if (storedPassword == null) {
                    return false;
                }

                // Check hashed password first
                try {
                    if (storedPassword.contains(":") && AuthService.verifyPassword(password, storedPassword)) {
                        return true;
                    }
                } catch (Exception ignored) {}

                // Fallback for legacy plain text passwords
                return storedPassword.equals(password);
            }

            return false;

        } catch (SQLException e) {
            System.err.println("(DB ERROR) Authentication failed: " + e.getMessage());
            return false;
        }
    }

    public static boolean isUserAllowed(String username) {
        String sql = "SELECT isAllowed FROM users WHERE username = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String allowed = rs.getString("isAllowed");
                return "true".equalsIgnoreCase(allowed);
            }
            return false;
        } catch (SQLException e) {
            System.err.println("(DB ERROR) Could not check allowed status: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateUserAllowed(int userId, String isAllowed) {
        String sql = "UPDATE users SET isAllowed = ? WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, isAllowed);
            pstmt.setInt(2, userId);
            int updated = pstmt.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            System.err.println("(DB ERROR) Could not update user status: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateUserAllowed(String username, String isAllowed) {
        String sql = "UPDATE users SET isAllowed = ? WHERE username = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, isAllowed);
            pstmt.setString(2, username);
            int updated = pstmt.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            System.err.println("(DB ERROR) Could not update user status: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            int deleted = pstmt.executeUpdate();
            return deleted > 0;
        } catch (SQLException e) {
            System.err.println("(DB ERROR) Could not delete user: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteUser(String username) {
        String sql = "DELETE FROM users WHERE username = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            int deleted = pstmt.executeUpdate();
            return deleted > 0;
        } catch (SQLException e) {
            System.err.println("(DB ERROR) Could not delete user: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateUserPassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        String storedPassword = newPassword;
        try {
            storedPassword = AuthService.hashPassword(newPassword);
        } catch (Exception e) {
            System.err.println("(DB WARNING) Could not hash password: " + e.getMessage());
        }

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, storedPassword);
            pstmt.setInt(2, userId);
            int updated = pstmt.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            System.err.println("(DB ERROR) Could not update user password: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateUserPassword(String username, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE username = ?";
        String storedPassword = newPassword;
        try {
            storedPassword = AuthService.hashPassword(newPassword);
        } catch (Exception e) {
            System.err.println("(DB WARNING) Could not hash password: " + e.getMessage());
        }

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, storedPassword);
            pstmt.setString(2, username);
            int updated = pstmt.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            System.err.println("(DB ERROR) Could not update user password: " + e.getMessage());
            return false;
        }
    }

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

    public static void displayChatHistory() {
        String sql = "SELECT sender, recipient, content, timestamp FROM messages ORDER BY id ASC";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n--- STORED CHAT HISTORY ---");
            while (rs.next()) {
                String sender = rs.getString("sender");
                String recipient = rs.getString("recipient");
                String content = rs.getString("content");
                String timestamp = rs.getString("timestamp");

                System.out.printf("[%s] %s -> %s: %s%n", timestamp, sender, recipient, content);
            }

        } catch (SQLException e) {
            System.err.println("(DB ERROR) Fetch failed: " + e.getMessage());
        }
    }

    public static ArrayList<Message> getChatHistoryArrayList() {
        String sql = "SELECT sender, recipient, content, timestamp FROM messages ORDER BY id ASC";
        ArrayList<Message> chatHistory = new ArrayList<>();

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String sender = rs.getString("sender");
                String recipient = rs.getString("recipient");
                String content = rs.getString("content");
                String timestamp = rs.getString("timestamp");

                chatHistory.add(new Message(sender, recipient, content, timestamp));
            }

        } catch (SQLException e) {
            System.err.println("(DB ERROR) Fetch failed: " + e.getMessage());
        }

        return chatHistory;
    }

    public static ArrayList<Message> getChatHistoryForRoom(String room) {
        String cleanRoom = (room != null && room.startsWith("#")) ? room.substring(1) : (room != null ? room : "General");
        String sql = "SELECT sender, recipient, content, timestamp FROM messages "
                   + "WHERE recipient = ? OR recipient = ? ORDER BY id ASC";
        ArrayList<Message> history = new ArrayList<>();
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cleanRoom);
            pstmt.setString(2, "#" + cleanRoom);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    history.add(new Message(rs.getString("sender"), rs.getString("recipient"), rs.getString("content"), rs.getString("timestamp")));
                }
            }
        } catch (SQLException e) {
            System.err.println("(DB ERROR) Fetch room history failed: " + e.getMessage());
        }
        return history;
    }

    public static ArrayList<Message> getDirectMessageHistory(String user1, String user2) {
        String sql = "SELECT sender, recipient, content, timestamp FROM messages "
                   + "WHERE (sender = ? AND recipient = ?) OR (sender = ? AND recipient = ?) ORDER BY id ASC";
        ArrayList<Message> history = new ArrayList<>();
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user1);
            pstmt.setString(2, user2);
            pstmt.setString(3, user2);
            pstmt.setString(4, user1);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    history.add(new Message(rs.getString("sender"), rs.getString("recipient"), rs.getString("content"), rs.getString("timestamp")));
                }
            }
        } catch (SQLException e) {
            System.err.println("(DB ERROR) Fetch DM history failed: " + e.getMessage());
        }
        return history;
    }

    public static ArrayList<Message> getAllRoomMessages() {
        String sql = "SELECT sender, recipient, content, timestamp FROM messages "
                   + "WHERE recipient LIKE '#%' OR LOWER(recipient) IN ('general', 'maths', 'elc', 'sdn') "
                   + "ORDER BY id ASC";
        ArrayList<Message> history = new ArrayList<>();
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                history.add(new Message(
                    rs.getString("sender"),
                    rs.getString("recipient"),
                    rs.getString("content"),
                    rs.getString("timestamp")
                ));
            }
        } catch (SQLException e) {
            System.err.println("(DB ERROR) Fetch all room history failed: " + e.getMessage());
        }
        return history;
    }

    public static ArrayList<Message> getAllDirectMessagesForUser(String username) {
        String sql = "SELECT sender, recipient, content, timestamp FROM messages "
                   + "WHERE (LOWER(sender) = LOWER(?) OR LOWER(recipient) = LOWER(?)) "
                   + "  AND NOT (recipient LIKE '#%') "
                   + "  AND LOWER(recipient) NOT IN ('general', 'maths', 'elc', 'sdn') "
                   + "ORDER BY id ASC";
        ArrayList<Message> history = new ArrayList<>();
        if (username == null || username.trim().isEmpty()) {
            return history;
        }
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username.trim());
            pstmt.setString(2, username.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    history.add(new Message(
                        rs.getString("sender"),
                        rs.getString("recipient"),
                        rs.getString("content"),
                        rs.getString("timestamp")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("(DB ERROR) Fetch user direct messages failed: " + e.getMessage());
        }
        return history;
    }

    public static List<UserRecord> getAllUsers() {
        List<UserRecord> users = new ArrayList<>();
        String sql = "SELECT id, username, isAllowed, created_at FROM users ORDER BY id ASC";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(new UserRecord(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("isAllowed"),
                    rs.getString("created_at")
                ));
            }
        } catch (SQLException e) {
            System.err.println("(DB ERROR) Failed to fetch users: " + e.getMessage());
        }
        return users;
    }

    public static int getUserCount() {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException ignored) {}
        return 0;
    }

    public static int getAcceptedUserCount() {
        String sql = "SELECT COUNT(*) FROM users WHERE isAllowed = 'true'";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException ignored) {}
        return 0;
    }

    public static int getPendingUserCount() {
        String sql = "SELECT COUNT(*) FROM users WHERE isAllowed = 'false'";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException ignored) {}
        return 0;
    }
}