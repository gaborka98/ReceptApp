package com.company.Forms;

import com.company.MyClass.User;
import com.company.MysqlConnector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainMenu extends JFrame{
    private JButton listButton;
    private JPanel panel;
    private JButton raktarButton;
    private JButton moderatorButton;
    private JButton logoutButton;

    private User loggedIn;
    private MysqlConnector conn;

    public User getLoggedIn() {
        return loggedIn;
    }

    public MainMenu(User ploggedIn) {
        this.loggedIn = ploggedIn;
        conn = MysqlConnector.getInstance();
        Dimension size = new Dimension(300, 200);

        setLocationRelativeTo(null);
        setContentPane(panel);
        setTitle("ReceptApp Főmenü");
        setMinimumSize(size);
        setPreferredSize(size);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();

        if (loggedIn.getModerator()) {
            moderatorButton.setVisible(true);
            moderatorButton.setEnabled(true);
        } else {
            moderatorButton.setEnabled(false);
            moderatorButton.setVisible(false);
        }

        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logoutProcess();
            }
        });

        moderatorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moderatorProcess();
            }
        });
        raktarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                storageProcess();
            }
        });
    }

    private void storageProcess() {
        StorageMenu storageMenu = new StorageMenu(this);
        storageMenu.setVisible(true);
        this.setVisible(false);
    }

    private void moderatorProcess() {
        ModeratorEditor moderatorEditor = new ModeratorEditor(this);
        moderatorEditor.setVisible(true);
        this.setVisible(false);
    }

    private void logoutProcess() {
        this.dispose();
        LoginScreen login = new LoginScreen();
        login.setVisible(true);
    }
}
