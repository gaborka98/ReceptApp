package com.company.Forms;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class PasswordReset extends JFrame {
    private JTextField emailTextfield;
    private JPanel panel1;
    private JButton passwordResetButton;
    private JPasswordField passwrodField;

    private LoginScreen parent;

    private Boolean complete = false;

    public PasswordReset(LoginScreen parent) {
        this.parent = parent;

        setLocationRelativeTo(null);
        setContentPane(panel1);
        setTitle("ReceptApp Belépés");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();

        passwordResetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reset();
            }
        });

        emailTextfield.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    reset();
                }
            }
        });

        passwrodField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    reset();
                }
            }
        });
    }

    public Boolean getComplete() {
        return complete;
    }

    private Boolean reset() {
        String email = emailTextfield.getText();
        char[] newPassword = passwrodField.getPassword();

        String newHash = parent.encryptStringSha256(newPassword);

        if (email.isEmpty()) { JOptionPane.showMessageDialog(null, "Adj meg e-mail címet!"); return false; }
        if (newPassword.length == 0) { JOptionPane.showMessageDialog(null, "Adj meg új jelszót!"); return false; }
        if (!email.contains("@") || !email.contains(".")) { JOptionPane.showMessageDialog(null, "Valódi e-mail címet adj meg!"); return false; }
        if (parent.connector.getLoggedInUserByEmail(email) == null) { JOptionPane.showMessageDialog(null, "A felhasználó nem található"); return false; }

        complete = parent.connector.changePasswordByEmail(email, newHash);

        if (complete) {
            JOptionPane.showMessageDialog(null, "Sikeresen megváltoztattad a jelszavad");
            this.dispose();
        }
        return complete;
    }
}
