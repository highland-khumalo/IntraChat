package com.IntraChat.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import com.IntraChat.database.DatabaseManager;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final Server server;
    private BufferedReader in;
    private PrintWriter out;
    private String username = "Anonymous";
    private String password = "password";

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("[HANDLER ERROR] Stream setup failed: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            String initialName = in.readLine();
            if (initialName != null && !initialName.trim().isEmpty()) {
                this.username = initialName.trim();
            }
            String initialPassword = in.readLine();
            if (initialPassword != null && !initialPassword.trim().isEmpty()) {
                this.password = initialPassword.trim();
            }

            // Verify or register user in database
            try {
                DatabaseManager.initializeDatabase();
                if (DatabaseManager.userExists(username)) {
                    if (!DatabaseManager.authenticateUser(username, password)) {
                        out.println("(system) Authentication failed: Incorrect credentials.");
                        return;
                    }
                    if (!DatabaseManager.isUserAllowed(username)) {
                        out.println("(system) Account pending: Your account is awaiting administrator approval in Admin Console.");
                        return;
                    }
                } else {
                    boolean firstUser = (DatabaseManager.getUserCount() == 0);
                    DatabaseManager.addUser(username, password, firstUser ? "true" : "false");
                    if (!firstUser) {
                        out.println("(system) Account registered: Your account is pending administrator approval.");
                        return;
                    }
                }
            } catch (Exception e) {
                System.err.println("[HANDLER WARNING] Database auth check: " + e.getMessage());
            }

            out.println("(system) Welcome to IntraChat, " + username + "!");
            server.broadcast("(server says) " + username + " joined the chat!", this);
            server.broadcastUserList();

            String incomingMessage;
            while ((incomingMessage = in.readLine()) != null) {
                if (incomingMessage.equalsIgnoreCase("/quit")) {
                    break;
                }

                if (incomingMessage.startsWith("ROOM:")) {
                    // Group chat: ROOM:<roomName>:<content>
                    int firstColon = incomingMessage.indexOf(':', 5);
                    if (firstColon != -1) {
                        String room = incomingMessage.substring(5, firstColon).trim();
                        String content = incomingMessage.substring(firstColon + 1);
                        try {
                            DatabaseManager.logMessage(username, room, content);
                        } catch (Exception ignored) {}
                        String roomBroadcast = "ROOM:" + room + ":" + username + ":" + content;
                        System.out.println("[ROOM " + room + "] " + username + ": " + content);
                        server.broadcast(roomBroadcast, this);
                    }
                } else if (incomingMessage.startsWith("DM:")) {
                    // Individual direct chat: DM:<recipient>:<content>
                    int firstColon = incomingMessage.indexOf(':', 3);
                    if (firstColon != -1) {
                        String recipient = incomingMessage.substring(3, firstColon).trim();
                        String content = incomingMessage.substring(firstColon + 1);
                        try {
                            DatabaseManager.logMessage(username, recipient, content);
                        } catch (Exception ignored) {}
                        boolean delivered = server.sendMessageToUsername("DM:" + username + ":" + content, recipient);
                        sendMessage("DM_SENT:" + recipient + ":" + content);
                        if (!delivered) {
                            sendMessage("(system) User '" + recipient + "' is offline. Message saved to history.");
                        }
                    }
                } else if (incomingMessage.startsWith("FILE:ROOM:")) {
                    // Group file transfer: FILE:ROOM:<roomName>:<filename>:<base64Data>
                    int rColon = incomingMessage.indexOf(':', 10);
                    if (rColon != -1) {
                        String room = incomingMessage.substring(10, rColon).trim();
                        int fColon = incomingMessage.indexOf(':', rColon + 1);
                        if (fColon != -1) {
                            String filename = incomingMessage.substring(rColon + 1, fColon).trim();
                            String base64Data = incomingMessage.substring(fColon + 1);
                            try {
                                DatabaseManager.logMessage(username, room, "[File: " + filename + "]");
                            } catch (Exception ignored) {}
                            String fileMsg = "FILE:ROOM:" + room + ":" + username + ":" + filename + ":" + base64Data;
                            server.broadcast(fileMsg, this);
                        }
                    }
                } else if (incomingMessage.startsWith("FILE:DM:")) {
                    // Individual file transfer: FILE:DM:<recipient>:<filename>:<base64Data>
                    int rColon = incomingMessage.indexOf(':', 8);
                    if (rColon != -1) {
                        String recipient = incomingMessage.substring(8, rColon).trim();
                        int fColon = incomingMessage.indexOf(':', rColon + 1);
                        if (fColon != -1) {
                            String filename = incomingMessage.substring(rColon + 1, fColon).trim();
                            String base64Data = incomingMessage.substring(fColon + 1);
                            try {
                                DatabaseManager.logMessage(username, recipient, "[File: " + filename + "]");
                            } catch (Exception ignored) {}
                            boolean delivered = server.sendMessageToUsername("FILE:DM:" + username + ":" + filename + ":" + base64Data, recipient);
                            sendMessage("FILE_SENT:DM:" + recipient + ":" + filename);
                            if (!delivered) {
                                sendMessage("(system) User '" + recipient + "' is offline. File could not be delivered.");
                            }
                        }
                    }
                } else {
                    // Default plain broadcast
                    try {
                        DatabaseManager.logMessage(username, "General", incomingMessage);
                    } catch (Exception e) {
                        System.err.println("[HANDLER ERROR] Failed to log message: " + e.getMessage());
                    }

                    String formatted = username + ": " + incomingMessage;
                    System.out.println("[LOG] " + formatted);
                    server.broadcast(formatted, this);
                }
            }
        } catch (IOException e) {
            System.out.println("(handler says) Connection lost with " + username);
        } finally {
            closeConnection();
        }
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public synchronized void closeConnection() {
        server.removeClient(this);
        if (server.isRunning()) {
            server.broadcast("(server says) " + username + " left the chat.", this);
            server.broadcastUserList();
        }
        try {
            if (socket != null && !socket.isClosed()) socket.close();
            if (in != null) in.close();
            if (out != null) out.close();
        } catch (IOException e) {
            System.err.println("(handler says) Error closing socket: " + e.getMessage());
        }
    }

    public String getUsername() {
        return username;
    }
}
