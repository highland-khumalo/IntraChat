package com.IntraChat.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author Bonginkosi
 */
public class Server {

    private final int port;
    private ServerSocket serverSocket;
    private boolean isRunning;

    public static int defaultPort = 5000;

    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();

    /**
     * Creates a server instance and assigns
     * the port number used for client connections.
     *
     * @param port The port on which the server listens.
     */
    public Server(int port) {
        this.port = port;
    }

    /**
     * Starts the server and listens for incoming
     * client connections. A new ClientHandler
     * thread is created for each connected client.
     */
    public void start() {
        synchronized (stopLock) {
            isRunning = true;
            isStopped = false;
        }
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("(server) Started on IP: " + getServerIpAddress() + " (port " + port + "). Waiting for clients...");

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("(server) New connection from: " + clientSocket.getRemoteSocketAddress());

                ClientHandler handler = new ClientHandler(clientSocket, this);
                Thread handlerThread = new Thread(handler);
                handlerThread.setDaemon(true);
                handlerThread.start();
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
     *
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
     * Sends a private message to a specific user.
     *
     * @param message The message to send.
     * @param username The recipient's username.
     * @return true if the message was delivered;
     *         otherwise false.
     */
    public boolean sendMessageToUsername(String message, String username) {
        if (username == null) return false;
        for (ClientHandler client : clients) {
            if (client.getUsername().equalsIgnoreCase(username.trim())) {
                client.sendMessage(message);
                return true;
            }
        }
        return false;
    }

    /**
     * Sends the current list of connected users
     * to all active clients.
     */
    public void broadcastUserList() {
        StringBuilder sb = new StringBuilder("(system:users) ");
        for (int i = 0; i < clients.size(); i++) {
            sb.append(clients.get(i).getUsername());
            if (i < clients.size() - 1) {
                sb.append(",");
            }
        }
        String userListMsg = sb.toString();
        for (ClientHandler client : clients) {
            client.sendMessage(userListMsg);
        }
    }

    /**
     * Adds a newly authenticated client to the
     * list of active server connections.
     *
     * @param client The client to be added.
     */
    public void addClient(ClientHandler client) {
        if (client != null && !clients.contains(client)) {
            clients.add(client);
            System.out.println("(server) Client authenticated: " + client.getUsername() + ". Total active: " + clients.size());
        }
    }

    /**
     * Removes a client from the list of active
     * server connections.
     *
     * @param client The client to be removed.
     */
    public void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("(server) Client disconnected. Total active: " + clients.size());
    }

    /**
     * Retrieves all currently connected clients.
     *
     * @return A list of active client handlers.
     */
    public List<ClientHandler> getClients() {
        return clients;
    }

    /**
     * Retrieves the server's local LAN IP address.
     *
     * @return The IP address used by clients to
     *         connect to the server.
     */
    public String getServerIpAddress() {
        return getLocalLanIp();
    }

    /**
     * Determines the server's reachable LAN IPv4 address.
     * The method first attempts to detect the active
     * network interface and then falls back to scanning
     * available interfaces if necessary.
     *
     * @return The detected LAN IPv4 address, or
     *         127.0.0.1 if no valid address is found. 
     */
    public static String getLocalLanIp() {
        // 1. Try finding outbound interface using UDP routing resolution
        try (java.net.DatagramSocket socket = new java.net.DatagramSocket()) {
            socket.connect(java.net.InetAddress.getByName("8.8.8.8"), 10002);
            java.net.InetAddress addr = socket.getLocalAddress();
            if (addr instanceof java.net.Inet4Address && !addr.isLoopbackAddress() && !addr.isAnyLocalAddress()) {
                return addr.getHostAddress();
            }
        } catch (Exception ignored) {
        }

        // 2. Scan network interfaces for active non-loopback site-local IPv4 address
        try {
            java.util.Enumeration<java.net.NetworkInterface> interfaces = java.net.NetworkInterface.getNetworkInterfaces();
            java.net.InetAddress nonSiteLocalCandidate = null;

            while (interfaces != null && interfaces.hasMoreElements()) {
                java.net.NetworkInterface iface = interfaces.nextElement();
                if (!iface.isUp() || iface.isLoopback() || iface.isVirtual()) {
                    continue;
                }

                String name = iface.getName().toLowerCase();
                if (name.contains("docker") || name.contains("veth") || name.contains("virbr") || name.contains("vmnet") || name.contains("vboxnet")) {
                    continue;
                }

                java.util.Enumeration<java.net.InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    java.net.InetAddress addr = addresses.nextElement();
                    if (addr instanceof java.net.Inet4Address && !addr.isLoopbackAddress() && !addr.isAnyLocalAddress()) {
                        if (addr.isSiteLocalAddress()) {
                            return addr.getHostAddress();
                        }
                        if (nonSiteLocalCandidate == null) {
                            nonSiteLocalCandidate = addr;
                        }
                    }
                }
            }

            if (nonSiteLocalCandidate != null) {
                return nonSiteLocalCandidate.getHostAddress();
            }

            // 3. Fallback to getLocalHost()
            java.net.InetAddress localHost = java.net.InetAddress.getLocalHost();
            if (localHost instanceof java.net.Inet4Address && !localHost.isLoopbackAddress() && !localHost.isAnyLocalAddress()) {
                return localHost.getHostAddress();
            }
        } catch (Exception ignored) {
        }

        return "127.0.0.1";
    }

    /**
     * Retrieves all valid IPv4 LAN addresses
     * available on the local machine.
     * Virtual, loop back, and inactive network
     * interfaces are excluded.
     *
     * @return A list containing all detected
     *         LAN IP addresses.
     */
    public static java.util.List<String> getAllLanIps() {
        java.util.List<String> list = new java.util.ArrayList<>();
        try {
            java.util.Enumeration<java.net.NetworkInterface> interfaces = java.net.NetworkInterface.getNetworkInterfaces();
            while (interfaces != null && interfaces.hasMoreElements()) {
                java.net.NetworkInterface iface = interfaces.nextElement();
                if (!iface.isUp() || iface.isLoopback() || iface.isVirtual()) continue;
                String name = iface.getName().toLowerCase();
                if (name.contains("docker") || name.contains("veth") || name.contains("virbr") || name.contains("vmnet") || name.contains("vboxnet")) continue;

                java.util.Enumeration<java.net.InetAddress> addrs = iface.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    java.net.InetAddress addr = addrs.nextElement();
                    if (addr instanceof java.net.Inet4Address && !addr.isLoopbackAddress() && !addr.isAnyLocalAddress()) {
                        String ip = addr.getHostAddress();
                        if (!list.contains(ip)) {
                            list.add(ip);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        if (list.isEmpty()) {
            list.add("127.0.0.1");
        }
        return list;
    }

    /**
     *  Retrieves the port number currently used
     * by the server.
     *
     * @return The server's port number.
     */
    public int getPort() {
        return port;
    }
    /**
     * Checks whether the server is currently running.
     *
     * @return true if the server is active;
     *         otherwise false.
     */
    public boolean isRunning() {
        return isRunning;
    }

    private final Object stopLock = new Object();
    private boolean isStopped = false;

    /**
     * Stops the server and disconnects all connected
     * clients. Network resources are released and
     * the server is marked as no longer running.
     */
    public void stop() {
        synchronized (stopLock) {
            if (isStopped) {
                return;
            }
            isStopped = true;
            isRunning = false;
        }
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("(server error) Error closing server: " + e.getMessage());
        }
        for (ClientHandler client : clients) {
            try {
                client.closeConnection();
            } catch (Exception ignored) {}
        }
        clients.clear();
        System.out.println("(server) Server stopped.");
    }

    /**
     * Generates a connection code containing the
     * server's IP address and port number.
     * The code can be shared with clients to
     * simplify connection setup.
     *
     * @return A formatted connection code for
     *         joining the server
     */
    public String getConnectionCode() {
        return com.IntraChat.security.AuthService.generateConnectionCode(getServerIpAddress(), port);
    }

}
