package com.IntraChat.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author bonginkosi
 */
public class Server {

private final int port;
    private ServerSocket serverSocket;
    private boolean isRunning;
    
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();

    /**
     * Creates a new server instance and assigns
     * the port number used for incoming connections.
     *
     * @param port The port on which the server will listen.
     */
    public Server(int port) {
        this.port = port;
    }
    /**
    * Starts the server and listens for incoming
    * client connections.
    * A new ClientHandler thread is created for
    * each connected client.
    */
    public void start() {
        isRunning = true;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("(server) Started on port " + port + ". Waiting for clients...");

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("(server) New connection from: " + clientSocket.getRemoteSocketAddress());

                ClientHandler handler = new ClientHandler(clientSocket, this);
                clients.add(handler);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            if (isRunning) {
                System.err.println("(server error) " + e.getMessage());
            }
        } finally {
            stop();
        }
    }

    // Send a message to all connected clients (group)

    /**
     * Sends a message to all connected clients.
     * @param message The message to be broadcast.
     * @param sender The client that originated the message.
     */
    public void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    // Send a message to a specific client by username (private message)

    /**
     * Sends a private message to a specific client
     * identified by their username.
     *
     * @param message The message to be sent.
     * @param username The recipient's username
     */
    public void sendMessageToUsername(String message, String username) {
        for (ClientHandler client : clients) {
            if (client.getUsername().equals(username)) {
                client.sendMessage(message);
                break;
            }
        }
    }
    /**
    * Removes a client from the list of active connections.
    *
    * @param client The client to be removed
    */
    public void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("(server) Client disconnected. Total active: " + clients.size());
    }

    /**
     * Stops the server and closes all active
     * client connections and server resources
     */
    public void stop() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            for (ClientHandler client : clients) {
                client.closeConnection();
            }
            clients.clear();
            System.out.println("(server) Server stopped.");
        } catch (IOException e) {
            System.err.println("(server error) Error closing server: " + e.getMessage());
        }
    }

}
