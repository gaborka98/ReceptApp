package com.company.Forms;

import com.company.MysqlConnector;

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

    private MysqlConnector connector = MysqlConnector.getInstance();

    private Boolean complete = false;

    public PasswordReset() {


        setContentPane(panel1);
        setTitle("ReceptApp Belépés");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);

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

        String newHash = connector.encryptStringSha256(newPassword);

        if (email.isEmpty()) { JOptionPane.showMessageDialog(this, "Adj meg e-mail címet!"); return false; }
        if (newPassword.length == 0) { JOptionPane.showMessageDialog(this, "Adj meg új jelszót!"); return false; }
        if (!email.contains("@") || !email.contains(".")) { JOptionPane.showMessageDialog(this, "Valódi e-mail címet adj meg!"); return false; }
        if (connector.getLoggedInUserByEmail(email) == null) { JOptionPane.showMessageDialog(this, "A felhasználó nem található"); return false; }

        complete = connector.changePasswordByEmail(email, newHash);

        if (complete) {
            JOptionPane.showMessageDialog(this, "Sikeresen megváltoztattad a jelszavad");
            this.dispose();
        }
        return complete;
    }
}
