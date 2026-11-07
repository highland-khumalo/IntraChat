package com.IntraChat.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private final String serverIp;
    private final int serverPort;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean isConnected;
    private java.util.function.Consumer<String> messageListener;

    public Client(String serverIp, int serverPort) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
    }

    public void setMessageListener(java.util.function.Consumer<String> listener) {
        this.messageListener = listener;
    }

    public boolean connect(String username, String password) { //Note: add the Code parameter and convert it back to IP
        try {
            socket = new Socket(serverIp, serverPort);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            isConnected = true;

            out.println(username);
            out.println(password != null ? password : "");

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
                System.out.println(incoming);
                if (messageListener != null) {
                    messageListener.accept(incoming);
                }
            }
        } catch (IOException e) {
            if (isConnected) {
                System.out.println("(client) Disconnected from server.");
                if (messageListener != null) {
                    messageListener.accept("(system) Disconnected from server.");
                }
            }
        } finally {
            disconnect();
        }
    }
    public void sendMessage(String message) {
        if (out != null && isConnected) {
            out.println(message);
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    public String getServerIp() {
        return serverIp;
    }

    public int getServerPort() {
        return serverPort;
    }

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
