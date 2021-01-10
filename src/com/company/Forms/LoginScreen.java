package com.company.Forms;

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

public class LoginScreen extends javax.swing.JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton passwordResetButton;
    private JButton loginButton;
    private JPanel panel;
    private JButton registerButton;

    private User loggedIn;

    public MysqlConnector connector = new MysqlConnector();

    Boolean registerComplete = false;

    public User getLoggedIn() {
        return loggedIn;
    }

    public LoginScreen() {
        setLocationRelativeTo(null);
        setContentPane(panel);
        setTitle("ReceptApp Belépés");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();

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

    public String encryptStringSha256(char[] text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(charsToBytes(text));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] charsToBytes(char[] chars){
        Charset charset = Charset.forName("UTF-8");
        ByteBuffer byteBuffer = charset.encode(CharBuffer.wrap(chars));
        return Arrays.copyOf(byteBuffer.array(), byteBuffer.limit());
    }


    public Boolean loginProcess() {
        String username = usernameField.getText();
        String hash = encryptStringSha256(passwordField.getPassword());
        User loggedIn = null;

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Nem adtál meg felhasználónevet!");
            return false;
        }
        if (passwordField.getPassword().length == 0) {
            JOptionPane.showMessageDialog(null, "Nem adtál meg jelszót!");
            return false;
        }

        if (hash.equals(connector.getPasswordHashByUsername(username))) {
            loggedIn = connector.getLoggedInUser(username);
            JOptionPane.showMessageDialog(this, "Sikeres bejelentkezés!");
            this.setVisible(false);
            return loggedIn != null;
        } else {
            JOptionPane.showMessageDialog(this, "Ismeretlen hiba miatt a bejelentkezés nem sikerült!");
            return false;
        }
    }

    public Boolean registerProcess() {
        RegisterScreen register = new RegisterScreen(this);
        register.setVisible(true);

        register.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                registerComplete = register.getComplete();
            }
        });

        return registerComplete;
    }

    public Boolean resetProcess() {
        return true;
    }
}
