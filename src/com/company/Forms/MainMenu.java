package com.company.Forms;

import com.company.MyClass.User;
import com.company.MysqlConnector;

import javax.swing.*;
import java.sql.Connection;

public class MainMenu extends JFrame{
    private JButton listButton;
    private JPanel panel;
    private JButton raktarButton;
    private JButton moderatorButton;
    private JButton logoutButton;

    private User loggedIn;
    private MysqlConnector conn;

    public MainMenu(User ploggedIn) {
        this.loggedIn = ploggedIn;
        conn = new MysqlConnector();

        setLocationRelativeTo(null);
        setContentPane(panel);
        setTitle("ReceptApp Főmenü");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();

        if (loggedIn.getModerator()) {
            moderatorButton.setVisible(true);
            moderatorButton.setEnabled(true);
        } else {
            moderatorButton.setEnabled(false);
            moderatorButton.setVisible(false);
        }
        if (loggedIn.getStorageId() >= 0) {
            raktarButton.setEnabled(true);
        } else {
            raktarButton.setEnabled(false);
        }
    }
}
