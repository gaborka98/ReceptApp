package com.company.Forms;

import com.company.Main;
import com.company.MyClass.User;
import com.company.MysqlConnector;

import javax.swing.*;
import java.awt.event.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

public class LoginScreen extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton passwordResetButton;
    private JButton loginButton;
    private JPanel panel;
    private JButton registerButton;

    private User loggedIn;

    public MysqlConnector connector = MysqlConnector.getInstance();

    public User getLoggedIn() {
        return loggedIn;
    }

    public LoginScreen() {

        setContentPane(panel);
        setTitle("ReceptApp Belépés");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);

        // Ha entert nyom a jelszo mezőben is belép
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    loginProcess();
                }
            }
        });

        // Ha entert nyom a jelszo mezőben is belép
        usernameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    loginProcess();
                }
            }
        });
        passwordResetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetProcess();
            }
        });
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loginProcess();
            }
        });
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registerProcess();
            }
        });
    }

    public Boolean loginProcess() {
        String username = usernameField.getText();
        String hash = connector.encryptStringSha256(passwordField.getPassword());
        User loggedIn = null;

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nem adtál meg felhasználónevet!");
            return false;
        }
        if (passwordField.getPassword().length == 0) {
            JOptionPane.showMessageDialog(this, "Nem adtál meg jelszót!");
            return false;
        }

        String databaseHash = connector.getPasswordHashByUsername(username);
        if (databaseHash.equals("nincs")) {
            JOptionPane.showMessageDialog(this, "Nincs ilyen felhasználó!");
            return false;
        }

        if (hash.equals(databaseHash)) {
            loggedIn = connector.getLoggedInUser(username);
            JOptionPane.showMessageDialog(this, "Sikeres bejelentkezés!");
            this.dispose();
            MainMenu menu = new MainMenu(loggedIn);
            menu.setVisible(true);
            return loggedIn != null;
        } else {
            JOptionPane.showMessageDialog(this, "A jelszó nem egyezik!");
            return false;
        }
    }

    public Boolean registerProcess() {
        RegisterScreen register = new RegisterScreen();
        register.setVisible(true);

        return register.getComplete();
    }

    public Boolean resetProcess() {
        PasswordReset pwReset = new PasswordReset();
        pwReset.setVisible(true);

        return pwReset.getComplete();
    }
}
