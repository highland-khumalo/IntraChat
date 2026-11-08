/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.IntraChat.gui;

import com.IntraChat.network.Client;
import com.IntraChat.network.Server;
import javax.swing.JOptionPane;

/**
 *
 * @author Liyabona
 */
public class Structure extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Chat.class.getName());
    private static Server runningServer;

    /**
     *  Creates the login window and initializes
     * all graphical user interface components
     */
    public Structure() {
        initComponents();
        customInit();
    }

    private void customInit() {
        String localIp = Server.getLocalLanIp();
        jLabel2.setText("Login to chat 👥 (Enter Connection Code below)");
        code.setText("Connection Code:");
        codeTF.setToolTipText("Enter Server Connection Code (e.g. INTRA-...) or leave blank for localhost");
    }
        
    private javax.swing.ImageIcon getSafeIcon(String resourcePath) {
        java.net.URL url = getClass().getResource(resourcePath);
        if (url == null && resourcePath.startsWith("/resources/")) {
            url = getClass().getResource(resourcePath.substring("/resources".length()));
        }
        if (url == null && !resourcePath.startsWith("/resources/")) {
            url = getClass().getResource("/resources" + resourcePath);
        }
        return url != null ? new javax.swing.ImageIcon(url) : null;
    }

    /**
     * Displays the login window on the
     * Swing Event Dispatch Thread.
     */
    public void start() {
        java.awt.EventQueue.invokeLater(() -> setVisible(true));
    }



    //End of variables declaration
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        Rootpanel = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        codeTF = new javax.swing.JTextField();
        passwordF = new javax.swing.JPasswordField();
        usernameTF = new javax.swing.JTextField();
        loginBtn = new javax.swing.JButton();
        username = new javax.swing.JLabel();
        code = new javax.swing.JLabel();
        password = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        startSeverBtn = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        logo = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        Rootpanel.setBackground(new java.awt.Color(18, 18, 18));
        Rootpanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel3.setBackground(new java.awt.Color(32, 32, 32));
        jPanel3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel3.setForeground(new java.awt.Color(204, 102, 0));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        codeTF.setBackground(new java.awt.Color(32, 32, 32));
        codeTF.setForeground(new java.awt.Color(255, 255, 255));
        codeTF.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(255, 255, 255)));
        codeTF.addActionListener(this::codeTFActionPerformed);
        jPanel3.add(codeTF, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 250, 120, -1));

        passwordF.setBackground(new java.awt.Color(32, 32, 32));
        passwordF.setForeground(new java.awt.Color(255, 255, 255));
        passwordF.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(255, 255, 255)));
        passwordF.addActionListener(this::passwordFActionPerformed);
        jPanel3.add(passwordF, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 180, 190, -1));

        usernameTF.setBackground(new java.awt.Color(32, 32, 32));
        usernameTF.setForeground(new java.awt.Color(255, 255, 255));
        usernameTF.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(255, 255, 255)));
        usernameTF.addActionListener(this::usernameTFActionPerformed);
        jPanel3.add(usernameTF, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 100, 200, -1));

        loginBtn.setBackground(new java.awt.Color(255, 140, 0));
        loginBtn.setFont(new java.awt.Font("Segoe UI Black", 3, 12)); // NOI18N
        loginBtn.setForeground(new java.awt.Color(255, 255, 255));
        loginBtn.setText("Enter");
        loginBtn.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        loginBtn.addActionListener(this::loginBtnActionPerformed);
        jPanel3.add(loginBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 290, 130, 40));

        username.setFont(new java.awt.Font("Segoe UI Black", 0, 12)); // NOI18N
        username.setForeground(new java.awt.Color(255, 255, 255));
        username.setText("username:");
        jPanel3.add(username, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 80, 78, -1));

        code.setFont(new java.awt.Font("Segoe UI Black", 0, 12)); // NOI18N
        code.setForeground(new java.awt.Color(255, 255, 255));
        code.setText("code:");
        jPanel3.add(code, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 220, 80, -1));

        password.setFont(new java.awt.Font("Segoe UI Black", 0, 12)); // NOI18N
        password.setForeground(new java.awt.Color(255, 255, 255));
        password.setText("password:");
        jPanel3.add(password, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 150, 71, -1));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Welcome Back");
        jPanel3.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 20, 250, 30));

        startSeverBtn.setBackground(new java.awt.Color(255, 140, 0));
        startSeverBtn.setFont(new java.awt.Font("Segoe UI Black", 3, 12)); // NOI18N
        startSeverBtn.setForeground(new java.awt.Color(255, 255, 255));
        startSeverBtn.setText("Start Server");
        startSeverBtn.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        startSeverBtn.addActionListener(this::startSeverBtnActionPerformedl);
        jPanel3.add(startSeverBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 290, 120, 40));

        jLabel2.setForeground(new java.awt.Color(255, 140, 0));
        jLabel2.setText("Login to continue your conversation 👥");
        jPanel3.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 50, 280, 20));

        Rootpanel.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 0, 340, 420));
        Rootpanel.add(logo, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 180, -1, -1));
        Rootpanel.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 420, -1));

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/logo.login.png"))); // NOI18N
        jLabel3.setText("jLabel3");
        Rootpanel.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(Rootpanel, javax.swing.GroupLayout.PREFERRED_SIZE, 760, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(Rootpanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void codeTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_codeTFActionPerformed
        loginBtnActionPerformed(evt);
    }//GEN-LAST:event_codeTFActionPerformed

    private void passwordFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_passwordFActionPerformed
        loginBtnActionPerformed(evt);
    }//GEN-LAST:event_passwordFActionPerformed

    private void startSeverBtnActionPerformedl(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startSeverBtnActionPerformedl
        Admin.openAdminConsole(Server.defaultPort);
        this.dispose();
    }//GEN-LAST:event_startSeverBtnActionPerformedl

    private void usernameTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usernameTFActionPerformed
        passwordF.requestFocusInWindow();
    }//GEN-LAST:event_usernameTFActionPerformed

    private void loginBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loginBtnActionPerformed
        String username = usernameTF.getText().trim();
        String password = new String(passwordF.getPassword()).trim();
        String code = codeTF.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Please enter both a username and password.",
                "Login Error",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        com.IntraChat.security.AuthService.ConnectionTarget target = com.IntraChat.security.AuthService.parseConnectionCode(code);
        final String finalServerIp = target.ip();
        final int finalServerPort = target.port();

        loginBtn.setEnabled(false);
        loginBtn.setText("Connecting...");

        new Thread(() -> {
            Client client = new Client(finalServerIp, finalServerPort);
            boolean connected = client.connect(username, password);

            javax.swing.SwingUtilities.invokeLater(() -> {
                loginBtn.setEnabled(true);
                loginBtn.setText("login");

                if (connected) {
                    Chat chatFrame = new Chat(client, username);
                    chatFrame.setVisible(true);
                    this.dispose();
                } else {
                    String err = client.getLastErrorMessage();
                    if (err != null && err.contains("Account pending")) {
                        JOptionPane.showMessageDialog(
                            this,
                            "Your account '" + username + "' is pending administrator approval.\n"
                            + "Please ask the server administrator to accept your account in the Admin Console.",
                            "Account Pending Approval",
                            JOptionPane.WARNING_MESSAGE
                        );
                    } else if (err != null && err.contains("Account registered")) {
                        JOptionPane.showMessageDialog(
                            this,
                            "Registration requested on the server!\n\nYour account '" + username + "' is pending administrator approval.\n"
                            + "The server administrator must accept your account in the Admin Console before you can log in.",
                            "Registration Submitted",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    } else if (err != null && err.contains("Authentication failed")) {
                        JOptionPane.showMessageDialog(
                            this,
                            "Incorrect password for user '" + username + "'.",
                            "Authentication Failed",
                            JOptionPane.ERROR_MESSAGE
                        );
                    } else {
                        JOptionPane.showMessageDialog(
                            this,
                            "Could not connect to server at " + finalServerIp + ":" + finalServerPort + ".\n\n"
                            + (err != null && !err.isBlank() ? "Details: " + err + "\n\n" : "")
                            + "Tips for connecting from another device:\n"
                            + " 1. Make sure both devices are on the same Wi-Fi / Local Network.\n"
                            + " 2. Ensure the host has clicked 'Start Server' on their device.\n"
                            + " 3. Verify you entered the host's LAN Connection Code.\n"
                            + " 4. Check that the host's firewall allows incoming connections on port " + finalServerPort + ".",
                            "Connection Failed",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            });
        }).start();
    }//GEN-LAST:event_loginBtnActionPerformed

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
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new Structure().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel Rootpanel;
    private javax.swing.JLabel code;
    private javax.swing.JTextField codeTF;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JButton loginBtn;
    private javax.swing.JLabel logo;
    private javax.swing.JLabel password;
    private javax.swing.JPasswordField passwordF;
    private javax.swing.JButton startSeverBtn;
    private javax.swing.JLabel username;
    private javax.swing.JTextField usernameTF;
    // End of variables declaration//GEN-END:variables
}

