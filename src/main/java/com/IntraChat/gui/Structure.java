/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.IntraChat.gui;

public class Structure extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Chat.class.getName());

    /**
     * Creates new form Chat
     */
    public Structure() {
        initComponents();
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
        jButton2 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
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
        usernameTF.addActionListener(this::usernameTFActionPerformed);
        jPanel3.add(usernameTF, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 100, 200, -1));

        loginBtn.setBackground(new java.awt.Color(0, 0, 0));
        loginBtn.setFont(new java.awt.Font("Segoe UI Black", 3, 12)); // NOI18N
        loginBtn.setForeground(new java.awt.Color(255, 140, 0));
        loginBtn.setText("login");
        loginBtn.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel3.add(loginBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 290, 130, 40));

        forgotpassword.setBackground(new java.awt.Color(255, 140, 0));
        forgotpassword.setForeground(new java.awt.Color(255, 255, 255));
        forgotpassword.setText("forgot password");
        forgotpassword.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(0, 0, 0)));
        jPanel3.add(forgotpassword, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 360, 150, -1));

        username.setFont(new java.awt.Font("Segoe UI Black", 0, 12)); // NOI18N
        username.setText("username:");
        jPanel3.add(username, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 80, 78, -1));

        code.setFont(new java.awt.Font("Segoe UI Black", 0, 12)); // NOI18N
        code.setText("code:");
        jPanel3.add(code, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 220, 37, -1));

        password.setFont(new java.awt.Font("Segoe UI Black", 0, 12)); // NOI18N
        password.setText("password:");
        jPanel3.add(password, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 150, 71, -1));

        jLabel5.setFont(new java.awt.Font("Viner Hand ITC", 1, 24)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Welcome");
        jPanel3.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 20, 120, 30));

        jButton2.setBackground(new java.awt.Color(0, 0, 0));
        jButton2.setFont(new java.awt.Font("Segoe UI Black", 3, 12)); // NOI18N
        jButton2.setForeground(new java.awt.Color(255, 140, 0));
        jButton2.setText("Start Server");
        jButton2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButton2.addActionListener(this::jButton2ActionPerformed);
        jPanel3.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 290, 120, 40));

        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Login to continue your conversation 👥");
        jPanel3.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 40, 220, -1));

        jLabel3.setFont(new java.awt.Font("Viner Hand ITC", 1, 24)); // NOI18N
        jLabel3.setText("Back!");
        jPanel3.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 20, -1, 30));

        Rootpanel.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 0, 340, 420));
        Rootpanel.add(logo, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 180, -1, -1));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/logo.login.png"))); // NOI18N
        Rootpanel.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 420, -1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(Rootpanel, javax.swing.GroupLayout.PREFERRED_SIZE, 760, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(Rootpanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void codeTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_codeTFActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_codeTFActionPerformed

    private void passwordFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_passwordFActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_passwordFActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton2ActionPerformed

    private void usernameTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usernameTFActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_usernameTFActionPerformed

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
    private javax.swing.JButton forgotpassword;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JButton loginBtn;
    private javax.swing.JLabel logo;
    private javax.swing.JLabel password;
    private javax.swing.JPasswordField passwordF;
    private javax.swing.JLabel username;
    private javax.swing.JTextField usernameTF;
    // End of variables declaration//GEN-END:variables

