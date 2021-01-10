package com.company.Forms;

import com.company.MyClass.User;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.PreparedStatement;

public class RegisterScreen extends JFrame {
    private JPanel panel;
    private JButton registerButton;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField emailField;

    private Boolean complete;

    private LoginScreen parent;

    public Boolean getComplete() {
        return complete;
    }

    public RegisterScreen(LoginScreen parent) {
        this.parent = parent;
        setLocationRelativeTo(null);
        setContentPane(panel);
        setTitle("ReceptApp Regisztráció");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();

        // Ha entert nyom a jelszo mezőben is belép
        usernameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    registerProcess();
                }
            }
        });

        // Ha entert nyom a jelszo mezőben is belép
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    registerProcess();
                }
            }
        });

        // Ha entert nyom a jelszo mezőben is belép
        emailField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    registerProcess();
                }
            }
        });
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registerProcess();
            }
        });
    }

    private Boolean registerProcess() {
        String username = usernameField.getText();
        String hash = parent.encryptStringSha256(passwordField.getPassword());
        String email = emailField.getText();



        if (username.isEmpty()) { JOptionPane.showMessageDialog(null, "Nem adtál meg felhasználónevet"); return false; }
        if (passwordField.getPassword().length == 0) { JOptionPane.showMessageDialog(null, "Nem adtál meg jelszót"); return false; }
        if (email.isEmpty()) { JOptionPane.showMessageDialog(null, "Nem adtál meg e-mail címet"); return false; }

        if (!email.contains("@") || !email.contains(".")) { JOptionPane.showMessageDialog(null, "Adj meg valódi e-mail címet!"); return false; }

        if (parent.connector.getLoggedInUser(username) != null) {
            JOptionPane.showMessageDialog(null, "A felhasználónév már foglalt!");
            return false;
        }
        if (parent.connector.getLoggedInUserByEmail(email) != null) {
            JOptionPane.showMessageDialog(null, "Az E-mail cím már foglalt!");
            return false;
        }

        User user = new User(username, hash, email, false);

        Boolean temp = parent.connector.addUserToDatabase(user);

        if (temp) {
            JOptionPane.showMessageDialog(null, "Sikeres regisztráció, most már beléphetsz a fiókodba");
            this.complete = true;
            this.dispose();
        }
        return temp;
    }
}
