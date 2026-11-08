package com.IntraChat.gui;

import com.IntraChat.database.DatabaseManager;
import com.IntraChat.network.Server;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

/**
 *
 * @author onele
 */
public class Admin extends javax.swing.JFrame {

    private Server serverInstance;
    private String activeFilter = "ALL";
    private int currentPort = Server.defaultPort;
    private javax.swing.JButton acceptBtn;
    private javax.swing.JButton rejectBtn;
    private javax.swing.JButton setPasswordBtn;
    private javax.swing.JButton deleteBtn;
    private javax.swing.JLabel portLabel;
    private javax.swing.JTextField portTF;
    private javax.swing.Timer autoRefreshTimer;

    /**
     * Creates new form Admin
     */
    public Admin() {
        this(Server.defaultPort);
    }

    public Admin(int port) {
        if (!AdminLockManager.acquireLock(port, this)) {
            JOptionPane.showMessageDialog(
                null,
                "Only 1 admin is allowed at a time for port " + port + ".\nAn admin console is already active for this port.",
                "Admin Session Active",
                JOptionPane.WARNING_MESSAGE
            );
            dispose();
            return;
        }
        this.currentPort = port;
        initComponents();
        customInit();
    }

    private void customInit() {
        try {
            DatabaseManager.initializeDatabase();

            if (portTF != null) {
                portTF.setText(String.valueOf(currentPort));
            }

            jButton6.setToolTipText("Click to show ALL users");
            jButton7.setToolTipText("Click to show ACCEPTED users");
            jButton8.setToolTipText("Click to show PENDING users");

            jButton6.addActionListener(e -> { activeFilter = "ALL"; refreshData(); });
            jButton7.addActionListener(e -> { activeFilter = "ACCEPTED"; refreshData(); });
            jButton8.addActionListener(e -> { activeFilter = "PENDING"; refreshData(); });

            javax.swing.JPopupMenu popup = new javax.swing.JPopupMenu();
            javax.swing.JMenuItem itemAccept = new javax.swing.JMenuItem("Accept User (Approve)");
            itemAccept.addActionListener(this::acceptBtnActionPerformed);
            javax.swing.JMenuItem itemReject = new javax.swing.JMenuItem("Set Pending");
            itemReject.addActionListener(this::rejectBtnActionPerformed);
            javax.swing.JMenuItem itemSetPassword = new javax.swing.JMenuItem("Set New Password...");
            itemSetPassword.addActionListener(this::setPasswordBtnActionPerformed);
            javax.swing.JMenuItem itemDelete = new javax.swing.JMenuItem("Delete User");
            itemDelete.addActionListener(this::deleteBtnActionPerformed);

            popup.add(itemAccept);
            popup.add(itemReject);
            popup.add(itemSetPassword);
            popup.addSeparator();
            popup.add(itemDelete);

            jTable1.setComponentPopupMenu(popup);
            jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {
                    int row = jTable1.rowAtPoint(e.getPoint());
                    if (row >= 0 && row < jTable1.getRowCount()) {
                        jTable1.setRowSelectionInterval(row, row);
                    }
                }
            });

            jTextField1.setEditable(false);
            jTextField1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            jTextField1.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    String currentText = jTextField1.getText();
                    if (currentText != null && !currentText.isEmpty() && !"Offline".equalsIgnoreCase(currentText)) {
                        try {
                            java.awt.datatransfer.StringSelection sel = new java.awt.datatransfer.StringSelection(currentText);
                            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
                            JOptionPane.showMessageDialog(Admin.this,
                                "Server Connection Code copied to clipboard:\n\n" + currentText,
                                "Code Copied",
                                JOptionPane.INFORMATION_MESSAGE);
                        } catch (Exception ignored) {}
                    }
                }
            });

            addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    if (autoRefreshTimer != null) {
                        autoRefreshTimer.stop();
                    }
                    AdminLockManager.releaseLock(currentPort);
                    if (serverInstance != null && serverInstance.isRunning()) {
                        serverInstance.stop();
                    }
                }
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    if (autoRefreshTimer != null) {
                        autoRefreshTimer.stop();
                    }
                    AdminLockManager.releaseLock(currentPort);
                }
            });

            autoRefreshTimer = new javax.swing.Timer(2500, evt -> {
                if (isVisible()) {
                    refreshData();
                }
            });
            autoRefreshTimer.start();

            refreshData();
        } catch (Exception e) {
            System.err.println("Admin init error: " + e.getMessage());
        }
    }

    public void refreshData() {
        try {
            int total = DatabaseManager.getUserCount();
            int accepted = DatabaseManager.getAcceptedUserCount();
            int pending = DatabaseManager.getPendingUserCount();

            jButton6.setText(String.valueOf(total));
            jButton7.setText(String.valueOf(accepted));
            jButton8.setText(String.valueOf(pending));

            var allUsers = DatabaseManager.getAllUsers();
            var filtered = new java.util.ArrayList<DatabaseManager.UserRecord>();
            for (var u : allUsers) {
                if ("PENDING".equalsIgnoreCase(activeFilter)) {
                    if ("false".equalsIgnoreCase(u.isAllowed())) filtered.add(u);
                } else if ("ACCEPTED".equalsIgnoreCase(activeFilter)) {
                    if ("true".equalsIgnoreCase(u.isAllowed())) filtered.add(u);
                } else {
                    filtered.add(u);
                }
            }

            String[] columnNames = {"ID", "Username", "Status", "Created At"};
            Object[][] data = new Object[filtered.size()][4];
            for (int i = 0; i < filtered.size(); i++) {
                var u = filtered.get(i);
                data[i][0] = u.id();
                data[i][1] = u.username();
                data[i][2] = "true".equalsIgnoreCase(u.isAllowed()) ? "Active" : "Pending";
                data[i][3] = u.createdAt();
            }

            int selectedRow = jTable1.getSelectedRow();
            Object selectedId = (selectedRow != -1 && selectedRow < jTable1.getRowCount())
                    ? jTable1.getValueAt(selectedRow, 0)
                    : null;

            jTable1.setModel(new javax.swing.table.DefaultTableModel(data, columnNames));

            if (selectedId != null) {
                for (int r = 0; r < jTable1.getRowCount(); r++) {
                    if (selectedId.equals(jTable1.getValueAt(r, 0))) {
                        jTable1.setRowSelectionInterval(r, r);
                        break;
                    }
                }
            }

            label9.setText("USER MANAGEMENT (" + activeFilter + ": " + filtered.size() + ")");

            boolean running = (serverInstance != null && serverInstance.isRunning());
            String lanIp = Server.getLocalLanIp();
            int currentPort = (serverInstance != null && serverInstance.isRunning())
                    ? serverInstance.getPort()
                    : Server.defaultPort;

            if (portTF != null) {
                if (!running) {
                    portTF.setEnabled(true);
                } else {
                    portTF.setText(String.valueOf(currentPort));
                    portTF.setEnabled(false);
                }
            }

            if (running) {
                String code = (serverInstance != null)
                        ? serverInstance.getConnectionCode()
                        : com.IntraChat.security.AuthService.generateConnectionCode(lanIp, currentPort);
                label12.setText("Online | IP: " + lanIp + " | Port: " + currentPort);
                label12.setForeground(new java.awt.Color(0, 204, 0));
                jTextField1.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
                jTextField1.setText(code);
                jTextField1.setForeground(new java.awt.Color(0, 255, 0));
                jTextField1.setToolTipText("Server Connection Code: " + code + " (Click to copy)");
                label8.setText("Connection Code (Click to Copy)");
                jButton9.setText("Stop server");
            } else {
                label12.setText("Offline (Local IP: " + lanIp + ")");
                label12.setForeground(java.awt.Color.RED);
                jTextField1.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 18));
                jTextField1.setText("Offline");
                jTextField1.setForeground(java.awt.Color.RED);
                jTextField1.setToolTipText("Start server to generate connection code");
                label8.setText("Server Status");
                jButton9.setText("Start server");
            }
        } catch (Exception e) {
            System.err.println("Failed to refresh admin data: " + e.getMessage());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();
        jPanel1 = new javax.swing.JPanel();
        label1 = new java.awt.Label();
        label2 = new java.awt.Label();
        jPanel2 = new javax.swing.JPanel();
        label4 = new java.awt.Label();
        jButton6 = new javax.swing.JButton();
        label5 = new java.awt.Label();
        label6 = new java.awt.Label();
        jButton7 = new javax.swing.JButton();
        label7 = new java.awt.Label();
        label8 = new java.awt.Label();
        jTextField1 = new javax.swing.JTextField();
        jButton8 = new javax.swing.JButton();
        label9 = new java.awt.Label();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        label10 = new java.awt.Label();
        label11 = new java.awt.Label();
        jButton9 = new javax.swing.JButton();
        label12 = new java.awt.Label();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        label3 = new java.awt.Label();

        jMenu1.setText("File");
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenuBar1.add(jMenu2);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("IntraChat - Admin Console");

        setBackground(new java.awt.Color(0, 0, 0));
        getContentPane().setBackground(new java.awt.Color(0, 0, 0));

        jPanel1.setBackground(new java.awt.Color(51, 51, 51));

        label1.setFont(new java.awt.Font("Dialog", 2, 24)); // NOI18N
        label1.setForeground(new java.awt.Color(255, 255, 255));
        label1.setText("Welcome, Administrator");

        label2.setForeground(new java.awt.Color(255, 255, 255));
        label2.setText("Manage your community and server efficiently.");

        jPanel2.setBackground(new java.awt.Color(0, 0, 0));

        label4.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        label4.setForeground(new java.awt.Color(255, 153, 0));
        label4.setText("Total Users");

        jButton6.setBackground(new java.awt.Color(0, 0, 0));
        jButton6.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jButton6.setForeground(new java.awt.Color(255, 255, 255));
        jButton6.setText("0");

        label5.setForeground(new java.awt.Color(255, 153, 0));
        label5.setText("Accepted Users");

        label6.setText("label6");

        jButton7.setBackground(new java.awt.Color(0, 0, 0));
        jButton7.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jButton7.setForeground(new java.awt.Color(255, 255, 255));
        jButton7.setText("0");

        label7.setForeground(new java.awt.Color(255, 153, 0));
        label7.setText("Pending Requests");

        label8.setForeground(new java.awt.Color(255, 153, 0));
        label8.setText("Server Status");

        jTextField1.setBackground(new java.awt.Color(0, 0, 0));
        jTextField1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jTextField1.setForeground(new java.awt.Color(0, 255, 0));
        jTextField1.setText("Online");

        jButton8.setBackground(new java.awt.Color(0, 0, 0));
        jButton8.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jButton8.setForeground(new java.awt.Color(255, 255, 255));
        jButton8.setText("0");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(label4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(label5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(label7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(15, 15, 15))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jButton6)
                        .addGap(18, 18, 18)
                        .addComponent(jButton7)
                        .addGap(46, 46, 46)
                        .addComponent(jButton8)
                        .addGap(51, 51, 51)))
                .addGap(106, 106, 106)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(label8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(label6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(label6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton6)
                            .addComponent(jButton7)
                            .addComponent(jButton8)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(label4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(label5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(label7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(22, 22, 22))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(label8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        label9.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        label9.setForeground(new java.awt.Color(255, 255, 255));
        label9.setText("USER MANAGEMENT");

        jTable1.setAutoCreateRowSorter(true);
        jTable1.setBackground(new java.awt.Color(0, 0, 0));
        jTable1.setForeground(new java.awt.Color(255, 153, 0));
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Username", "Status", "Created At"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        acceptBtn = new javax.swing.JButton();
        acceptBtn.setBackground(new java.awt.Color(40, 167, 69));
        acceptBtn.setFont(new java.awt.Font("Segoe UI", 1, 12));
        acceptBtn.setForeground(new java.awt.Color(255, 255, 255));
        acceptBtn.setText("Accept User");
        acceptBtn.addActionListener(this::acceptBtnActionPerformed);

        rejectBtn = new javax.swing.JButton();
        rejectBtn.setBackground(new java.awt.Color(45, 45, 45));
        rejectBtn.setFont(new java.awt.Font("Segoe UI", 1, 12));
        rejectBtn.setForeground(new java.awt.Color(255, 153, 0));
        rejectBtn.setText("Set Pending");
        rejectBtn.addActionListener(this::rejectBtnActionPerformed);

        setPasswordBtn = new javax.swing.JButton();
        setPasswordBtn.setBackground(new java.awt.Color(30, 136, 229));
        setPasswordBtn.setFont(new java.awt.Font("Segoe UI", 1, 12));
        setPasswordBtn.setForeground(new java.awt.Color(255, 255, 255));
        setPasswordBtn.setText("Set Password");
        setPasswordBtn.addActionListener(this::setPasswordBtnActionPerformed);

        deleteBtn = new javax.swing.JButton();
        deleteBtn.setBackground(new java.awt.Color(180, 40, 40));
        deleteBtn.setFont(new java.awt.Font("Segoe UI", 1, 12));
        deleteBtn.setForeground(new java.awt.Color(255, 255, 255));
        deleteBtn.setText("Delete User");
        deleteBtn.addActionListener(this::deleteBtnActionPerformed);

        portLabel = new javax.swing.JLabel();
        portLabel.setFont(new java.awt.Font("Dialog", 1, 16));
        portLabel.setForeground(new java.awt.Color(255, 255, 255));
        portLabel.setText("Port:");

        portTF = new javax.swing.JTextField();
        portTF.setBackground(new java.awt.Color(32, 32, 32));
        portTF.setFont(new java.awt.Font("Segoe UI", 1, 14));
        portTF.setForeground(new java.awt.Color(255, 255, 255));
        portTF.setText(String.valueOf(Server.defaultPort));
        portTF.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 140, 0)));

        label10.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        label10.setForeground(new java.awt.Color(255, 255, 255));
        label10.setText("SERVER CONTROL");

        label11.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        label11.setForeground(new java.awt.Color(255, 255, 255));
        label11.setText("Server Status:");

        jButton9.setBackground(new java.awt.Color(255, 153, 0));
        jButton9.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton9.setText("Stop server");
        jButton9.addActionListener(this::jButton9ActionPerformed);

        label12.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        label12.setForeground(new java.awt.Color(0, 204, 0));
        label12.setText("Online");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(label1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(label2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 376, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(label9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(acceptBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rejectBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(setPasswordBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 70, Short.MAX_VALUE)
                        .addComponent(label10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(178, 178, 178))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(50, 50, 50)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(label11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(label12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(portLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(portTF, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(label1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(label2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(label9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(label10, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(acceptBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(rejectBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(setPasswordBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(deleteBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(label11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(label12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(portLabel)
                            .addComponent(portTF, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(40, Short.MAX_VALUE))
        );

        jButton1.setBackground(new java.awt.Color(255, 153, 0));
        jButton1.setText("Dashboard");
        jButton1.addActionListener(this::jButton1ActionPerformed);

        jButton2.setBackground(new java.awt.Color(0, 0, 0));
        jButton2.setForeground(new java.awt.Color(255, 153, 0));
        jButton2.setText("User Management");
        jButton2.addActionListener(this::jButton2ActionPerformed);

        jButton3.setBackground(new java.awt.Color(0, 0, 0));
        jButton3.setForeground(new java.awt.Color(255, 153, 0));
        jButton3.setText("Server Control");
        jButton3.addActionListener(this::jButton3ActionPerformed);

        jButton4.setBackground(new java.awt.Color(0, 0, 0));
        jButton4.setForeground(new java.awt.Color(255, 153, 0));
        jButton4.setText("Logs");
        jButton4.addActionListener(this::jButton4ActionPerformed);

        jButton5.setBackground(new java.awt.Color(0, 0, 0));
        jButton5.setForeground(new java.awt.Color(255, 153, 0));
        jButton5.setText("About");
        jButton5.addActionListener(this::jButton5ActionPerformed);

        label3.setFont(new java.awt.Font("Dialog", 1, 24)); // NOI18N
        label3.setForeground(new java.awt.Color(255, 153, 0));
        label3.setText("Intrachart");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
                                .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 258, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addComponent(label3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 121, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(label3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(72, 72, 72)
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton3)
                        .addGap(12, 12, 12)
                        .addComponent(jButton4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton5)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        String ip = Server.getLocalLanIp();
        boolean running = (serverInstance != null && serverInstance.isRunning());
        String info = running
            ? "Server is ONLINE!\n\n"
              + "To connect from another device on the same Wi-Fi / LAN:\n"
              + "  • Server IP: " + ip + "\n"
              + "  • Port: " + serverInstance.getPort() + "\n\n"
              + "All detected local IP addresses on this machine:\n  " + String.join("\n  ", Server.getAllLanIps())
            : "Server is currently OFFLINE.\n\nYour machine's local LAN IP is: " + ip + "\nClick 'Start server' to begin hosting.";
        JOptionPane.showMessageDialog(this, info, "Server Network Details", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_jButton3ActionPerformed

    private void acceptBtnActionPerformed(java.awt.event.ActionEvent evt) {
        int selectedRow = jTable1.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user from the table to accept.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = jTable1.convertRowIndexToModel(selectedRow);
        Object idVal = jTable1.getModel().getValueAt(modelRow, 0);
        Object userVal = jTable1.getModel().getValueAt(modelRow, 1);
        if (idVal instanceof Integer userId) {
            DatabaseManager.updateUserAllowed(userId, "true");
            refreshData();
            JOptionPane.showMessageDialog(this, "User '" + userVal + "' is now APPROVED and can log in.", "User Accepted", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void rejectBtnActionPerformed(java.awt.event.ActionEvent evt) {
        int selectedRow = jTable1.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user from the table.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = jTable1.convertRowIndexToModel(selectedRow);
        Object idVal = jTable1.getModel().getValueAt(modelRow, 0);
        Object userVal = jTable1.getModel().getValueAt(modelRow, 1);
        if (idVal instanceof Integer userId) {
            DatabaseManager.updateUserAllowed(userId, "false");
            refreshData();
            JOptionPane.showMessageDialog(this, "User '" + userVal + "' status updated to PENDING.", "Status Updated", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deleteBtnActionPerformed(java.awt.event.ActionEvent evt) {
        int selectedRow = jTable1.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user from the table to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = jTable1.convertRowIndexToModel(selectedRow);
        Object idVal = jTable1.getModel().getValueAt(modelRow, 0);
        Object userVal = jTable1.getModel().getValueAt(modelRow, 1);
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to permanently delete user '" + userVal + "'?",
            "Confirm Delete User",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION && idVal instanceof Integer userId) {
            DatabaseManager.deleteUser(userId);
            refreshData();
            JOptionPane.showMessageDialog(this, "User '" + userVal + "' was permanently deleted.", "User Deleted", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void setPasswordBtnActionPerformed(java.awt.event.ActionEvent evt) {
        int selectedRow = jTable1.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user from the table to set a new password.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = jTable1.convertRowIndexToModel(selectedRow);
        Object idVal = jTable1.getModel().getValueAt(modelRow, 0);
        Object userVal = jTable1.getModel().getValueAt(modelRow, 1);
        String username = String.valueOf(userVal);

        javax.swing.JPasswordField newPassField = new javax.swing.JPasswordField();
        javax.swing.JPasswordField confirmPassField = new javax.swing.JPasswordField();
        Object[] msg = {
            "User: " + username,
            "Enter New Password:", newPassField,
            "Confirm New Password:", confirmPassField
        };

        int opt = JOptionPane.showConfirmDialog(
            this,
            msg,
            "Set New Password for '" + username + "'",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (opt == JOptionPane.OK_OPTION) {
            String newPass = new String(newPassField.getPassword()).trim();
            String confirmPass = new String(confirmPassField.getPassword()).trim();

            if (newPass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Password cannot be empty.", "Invalid Password", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!newPass.equals(confirmPass)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match. Please try again.", "Password Mismatch", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean updated = false;
            if (idVal instanceof Integer userId) {
                updated = DatabaseManager.updateUserPassword(userId, newPass);
            } else {
                updated = DatabaseManager.updateUserPassword(username, newPass);
            }

            if (updated) {
                JOptionPane.showMessageDialog(
                    this,
                    "Password for user '" + username + "' has been successfully updated!\nThe user can now log in with the new password.",
                    "Password Changed",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update password in database.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        if (serverInstance != null && serverInstance.isRunning()) {
            serverInstance.stop();
            if (portTF != null) portTF.setEnabled(true);
            refreshData();
            JOptionPane.showMessageDialog(this, "Server stopped successfully.", "Server Control", JOptionPane.INFORMATION_MESSAGE);
        } else {
            int port = currentPort;
            if (portTF != null) {
                try {
                    port = Integer.parseInt(portTF.getText().trim());
                    if (port < 1024 || port > 65535) {
                        JOptionPane.showMessageDialog(this, "Port must be between 1024 and 65535.", "Invalid Port", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid numeric port (e.g. 5000).", "Invalid Port", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            final int chosenPort = port;
            if (chosenPort != currentPort) {
                if (AdminLockManager.isPortAdminActive(chosenPort)) {
                    JOptionPane.showMessageDialog(
                        this,
                        "Only 1 admin is allowed at a time for port " + chosenPort + ".\nAnother admin session is currently active for this port.",
                        "Port Conflict",
                        JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }
                AdminLockManager.releaseLock(currentPort);
                boolean acquired = AdminLockManager.acquireLock(chosenPort, this);
                if (!acquired) {
                    AdminLockManager.acquireLock(currentPort, this);
                    JOptionPane.showMessageDialog(this, "Could not acquire admin lock for port " + chosenPort, "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                this.currentPort = chosenPort;
            }

            serverInstance = new Server(chosenPort);
            new Thread(() -> serverInstance.start()).start();
            try { Thread.sleep(200); } catch (InterruptedException ignored) {}
            if (portTF != null) portTF.setEnabled(false);
            refreshData();
            String ip = Server.getLocalLanIp();
            String code = serverInstance.getConnectionCode();
            try {
                java.awt.datatransfer.StringSelection sel = new java.awt.datatransfer.StringSelection(code);
                java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
            } catch (Exception ignored) {}
            JOptionPane.showMessageDialog(this,
                "Server started successfully!\n\n"
                + "➤ Server Connection Code:\n"
                + "   " + code + "\n\n"
                + "(Copied to your clipboard automatically!)\n\n"
                + "➤ Direct Address: " + ip + ":" + chosenPort + "\n\n"
                + "Share this Connection Code with users on other devices.\n"
                + "They enter it in the 'code:' field on their login screen to connect directly!",
                "Server Online - Connection Code Ready",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        refreshData();
        JOptionPane.showMessageDialog(this, "Dashboard refreshed.\nActive Server: " + ((serverInstance != null && serverInstance.isRunning()) ? "Yes (Port " + serverInstance.getPort() + ")" : "No"), "Dashboard", JOptionPane.INFORMATION_MESSAGE);
    }

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {
        refreshData();
        JOptionPane.showMessageDialog(this, "User Management console refreshed.\nView registered users in the table.", "User Management", JOptionPane.INFORMATION_MESSAGE);
    }

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            var history = DatabaseManager.getChatHistoryArrayList();
            StringBuilder sb = new StringBuilder();
            for (var m : history) {
                sb.append("[").append(m.timestamp()).append("] ")
                  .append(m.sender()).append(" -> ")
                  .append(m.recipient()).append(": ")
                  .append(m.content()).append("\n");
            }
            if (sb.length() == 0) {
                sb.append("No chat records logged yet.");
            }
            JTextArea ta = new JTextArea(sb.toString(), 18, 45);
            ta.setEditable(false);
            JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Chat Logs", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error fetching logs: " + e.getMessage(), "Logs Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {
        JOptionPane.showMessageDialog(this, "IntraChat Admin Console\nVersion 1.0-SNAPSHOT\nJava 21 Swing Application", "About", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void dispose() {
        if (autoRefreshTimer != null) {
            autoRefreshTimer.stop();
        }
        AdminLockManager.releaseLock(currentPort);
        super.dispose();
    }

    public void start() {
        if (AdminLockManager.getActiveAdmin(currentPort) == this) {
            java.awt.EventQueue.invokeLater(() -> setVisible(true));
        }
    }

    public static Admin openAdminConsole(int port) {
        if (AdminLockManager.isPortAdminActive(port)) {
            Admin existing = AdminLockManager.getActiveAdmin(port);
            if (existing != null) {
                existing.setVisible(true);
                existing.toFront();
                existing.requestFocus();
            }
            JOptionPane.showMessageDialog(
                null,
                "Only 1 admin is allowed at a time for port " + port + ".\nAn admin console is already active for this port.",
                "Admin Access Restricted",
                JOptionPane.WARNING_MESSAGE
            );
            return existing;
        }
        Admin admin = new Admin(port);
        admin.setVisible(true);
        return admin;
    }

    public static void releaseAllLocks() {
        for (int p : new java.util.ArrayList<>(AdminLockManager.activePorts)) {
            AdminLockManager.releaseLock(p);
        }
    }

    public static class AdminLockManager {
        private static final Set<Integer> activePorts = ConcurrentHashMap.newKeySet();
        private static final Map<Integer, Admin> jvmAdmins = new ConcurrentHashMap<>();
        private static final Map<Integer, FileChannel> channels = new ConcurrentHashMap<>();
        private static final Map<Integer, FileLock> locks = new ConcurrentHashMap<>();

        public static synchronized boolean isPortAdminActive(int port) {
            if (activePorts.contains(port)) {
                return true;
            }
            File lockFile = new File(System.getProperty("java.io.tmpdir"), "intrachat_admin_" + port + ".lock");
            try {
                RandomAccessFile raf = new RandomAccessFile(lockFile, "rw");
                FileChannel ch = raf.getChannel();
                FileLock l = ch.tryLock();
                if (l == null) {
                    ch.close();
                    raf.close();
                    return true;
                }
                l.release();
                ch.close();
                raf.close();
                return false;
            } catch (Exception e) {
                return true;
            }
        }

        public static synchronized boolean acquireLock(int port, Admin admin) {
            if (admin != null && jvmAdmins.get(port) == admin) {
                return true;
            }
            if (activePorts.contains(port)) {
                return false;
            }
            File lockFile = new File(System.getProperty("java.io.tmpdir"), "intrachat_admin_" + port + ".lock");
            try {
                RandomAccessFile raf = new RandomAccessFile(lockFile, "rw");
                FileChannel ch = raf.getChannel();
                FileLock l = ch.tryLock();
                if (l == null) {
                    ch.close();
                    raf.close();
                    return false;
                }
                activePorts.add(port);
                if (admin != null) {
                    jvmAdmins.put(port, admin);
                }
                channels.put(port, ch);
                locks.put(port, l);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        public static synchronized void releaseLock(int port) {
            activePorts.remove(port);
            jvmAdmins.remove(port);
            FileLock l = locks.remove(port);
            if (l != null) {
                try { l.release(); } catch (Exception ignored) {}
            }
            FileChannel ch = channels.remove(port);
            if (ch != null) {
                try { ch.close(); } catch (Exception ignored) {}
            }
        }

        public static Admin getActiveAdmin(int port) {
            return jvmAdmins.get(port);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Admin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            Admin.openAdminConsole(Server.defaultPort);
        });
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    private java.awt.Label label1;
    private java.awt.Label label10;
    private java.awt.Label label11;
    private java.awt.Label label12;
    private java.awt.Label label2;
    private java.awt.Label label3;
    private java.awt.Label label4;
    private java.awt.Label label5;
    private java.awt.Label label6;
    private java.awt.Label label7;
    private java.awt.Label label8;
    private java.awt.Label label9;
    // End of variables declaration//GEN-END:variables
}
   
