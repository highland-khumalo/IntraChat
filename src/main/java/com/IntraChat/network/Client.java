package com.IntraChat.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author Bonginkosi
 */
public class Client {
private final String serverIp;
    private final int serverPort;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean isConnected;

    /**
     * Creates a new client instance and stores the
     * server connection details.
     * @param serverIp The IP address of the server
     * @param serverPort The port number used for communication
     */
    public Client(String serverIp, int serverPort) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
    }

    /**
     *Establishes a connection to the server and
     * initializes the communication streams.
     * If the connection is successful, a background
     * thread is started to listen for incoming messages.
     * @param username The username used to identify the client
     * @param password The password used for authentication
     * @return true if the connection is successful:
     * otherwise false
     */
    public boolean connect(String username, String password) { //Note: add the Code parameter and convert it back to IP
        try {
            socket = new Socket(serverIp, serverPort);
            socket.setSoTimeout(1000);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            isConnected = true;

            out.println(username);

            new Thread(this::listenForMessages).start();

            return true;
        } catch (IOException e) {
            System.err.println("(error) Could not connect to server: " + e.getMessage());
            return false;
        }
    }

    private void listenForMessages() {
        try {
            String incoming;
            while (isConnected && (incoming = in.readLine()) != null) {
                // Note: Remember to handle the gui aspect when complete
                System.out.println(incoming);
            }
        } catch (IOException e) {
            if (isConnected) {
                System.out.println("(client) Disconnected from server.");
            }
        } finally {
            disconnect();
        }
    }

    /**
     * Sends a message to the connected server.
     * The message is transmitted only if the client
     * is currently connected and the output stream
     * is available.
     * @param message
     */
    public void sendMessage(String message) {
        if (out != null && isConnected) {
            out.println(message);
        }
    }

    /**
     * Closes the connection to the server and releases
     * all associated network resources.
     * The socket and communication streams are safely closed.
     */
    public void disconnect() {
        isConnected = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("(error) Error closing connection: " + e.getMessage());
        }
    }
}
