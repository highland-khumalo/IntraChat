package com.IntraChat.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client {
    private final String serverIp;
    private final int serverPort;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean isConnected;
    private java.util.function.Consumer<String> messageListener;
    private String lastErrorMessage;
    private final java.util.Queue<String> pendingQueue = new java.util.concurrent.ConcurrentLinkedQueue<>();

    public Client(String serverIp, int serverPort) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
    }

    public synchronized void setMessageListener(java.util.function.Consumer<String> listener) {
        this.messageListener = listener;
        if (listener != null) {
            String pending;
            while ((pending = pendingQueue.poll()) != null) {
                try {
                    listener.accept(pending);
                } catch (Exception e) {
                    System.err.println("(client error) Error dispatching pending message: " + e.getMessage());
                }
            }
        }
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public boolean connect(String username, String password) {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(serverIp, serverPort), 5000);
            socket.setSoTimeout(5000);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println(username);
            out.println(password != null ? password : "");

            String response = in.readLine();
            if (response == null) {
                lastErrorMessage = "Server closed connection during authentication.";
                disconnect();
                return false;
            }

            if (response.startsWith("(system) Welcome")) {
                socket.setSoTimeout(0);
                isConnected = true;
                lastErrorMessage = null;
                pendingQueue.offer(response);

                Thread listenerThread = new Thread(this::listenForMessages);
                listenerThread.setDaemon(true);
                listenerThread.start();

                return true;
            } else {
                lastErrorMessage = response;
                disconnect();
                return false;
            }
        } catch (IOException e) {
            lastErrorMessage = e.getMessage();
            disconnect();
            return false;
        }
    }

    private void listenForMessages() {
        try {
            String incoming;
            while (isConnected && (incoming = in.readLine()) != null) {
                System.out.println(incoming);
                synchronized (this) {
                    if (messageListener != null) {
                        messageListener.accept(incoming);
                    } else {
                        pendingQueue.offer(incoming);
                    }
                }
            }
        } catch (IOException e) {
            if (isConnected) {
                System.out.println("(client) Disconnected from server.");
                synchronized (this) {
                    if (messageListener != null) {
                        messageListener.accept("(system) Disconnected from server.");
                    } else {
                        pendingQueue.offer("(system) Disconnected from server.");
                    }
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

    public synchronized void disconnect() {
        if (!isConnected && (socket == null || socket.isClosed())) {
            return;
        }
        isConnected = false;
        try {
            if (socket != null && !socket.isClosed()) socket.close();
            if (in != null) in.close();
            if (out != null) out.close();
        } catch (IOException e) {
            System.err.println("(error) Error closing connection: " + e.getMessage());
        }
    }
}
