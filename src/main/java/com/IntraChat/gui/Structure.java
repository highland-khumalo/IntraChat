/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.IntraChat.gui;

import com.IntraChat.network.Server;

public class Structure extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Chat.class.getName());

    /**
     * Creates new form Chat
     */
    public Structure() {
        initComponents();
        setResizable(false);
        setTitle("Login");
        pack();
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
        forgotpassword = new javax.swing.JButton();
        username = new javax.swing.JLabel();
        code = new javax.swing.JLabel();
        password = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        startSeverBtn = new javax.swing.JButton();
        logo = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        Rootpanel.setBackground(new java.awt.Color(18, 18, 18));
        Rootpanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel3.setBackground(new java.awt.Color(255, 140, 0));
        jPanel3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel3.setForeground(new java.awt.Color(204, 102, 0));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        codeTF.setBackground(new java.awt.Color(255, 140, 0));
        codeTF.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(0, 0, 0)));
        codeTF.addActionListener(this::codeTFActionPerformed);
        jPanel3.add(codeTF, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 250, 120, -1));

        passwordF.setBackground(new java.awt.Color(255, 140, 0));
        passwordF.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(0, 0, 0)));
        passwordF.addActionListener(this::passwordFActionPerformed);
        jPanel3.add(passwordF, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 180, 190, -1));

        usernameTF.setBackground(new java.awt.Color(255, 140, 0));
        usernameTF.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(0, 0, 0)));
        jPanel3.add(usernameTF, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 100, 200, -1));

        loginBtn.setBackground(new java.awt.Color(0, 0, 0));
        loginBtn.setFont(new java.awt.Font("Segoe UI Black", 3, 12)); // NOI18N
        loginBtn.setForeground(new java.awt.Color(255, 140, 0));
        loginBtn.setText("login");
        loginBtn.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel3.add(loginBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 290, 130, 40));

        forgotpassword.setBackground(new java.awt.Color(255, 140, 0));
        forgotpassword.setText("forgot password");
        forgotpassword.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(0, 0, 0)));
        jPanel3.add(forgotpassword, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 360, 150, -1));

        username.setText("username");
        jPanel3.add(username, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 80, 78, -1));

        code.setText("code");
        jPanel3.add(code, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 220, 37, -1));

        password.setText("password");
        jPanel3.add(password, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 150, 71, -1));

        jLabel5.setFont(new java.awt.Font("Sylfaen", 3, 18)); // NOI18N
        jLabel5.setText("LOGIN :");
        jPanel3.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(88, 21, 130, 40));

        startSeverBtn.setBackground(new java.awt.Color(0, 0, 0));
        startSeverBtn.setFont(new java.awt.Font("Segoe UI Black", 3, 12)); // NOI18N
        startSeverBtn.setForeground(new java.awt.Color(255, 140, 0));
        startSeverBtn.setText("Start Server");
        startSeverBtn.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        startSeverBtn.addActionListener(this::startSeverBtnActionPerformed);
        jPanel3.add(startSeverBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 290, 120, 40));

        Rootpanel.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 0, 340, 420));
        Rootpanel.add(logo, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 180, -1, -1));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/logo.login.png"))); // NOI18N
        Rootpanel.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 420, -1));

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
        // TODO add your handling code here:
    }//GEN-LAST:event_codeTFActionPerformed

    private void passwordFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_passwordFActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_passwordFActionPerformed

    private void startSeverBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startSeverBtnActionPerformed
        Server server = new Server(Server.defaultPort);
        new Thread(() -> {
            server.start();
        }).start();
        String ipAddress = server.getServerIpAddress();
        int ipPort = server.getPort();

        javax.swing.JOptionPane.showMessageDialog(
                this,
                "Sever Started at " + ipAddress + ":" + ipPort,
                "Notification",
                javax.swing.JOptionPane.INFORMATION_MESSAGE
        );
    }//GEN-LAST:event_startSeverBtnActionPerformed

    public void start() {
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
        java.awt.EventQueue.invokeLater(() -> setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel Rootpanel;
    private javax.swing.JLabel code;
    private javax.swing.JTextField codeTF;
    private javax.swing.JButton forgotpassword;
    private javax.swing.JLabel jLabel1;
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
