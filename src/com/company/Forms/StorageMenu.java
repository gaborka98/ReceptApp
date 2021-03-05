package com.company.Forms;

import com.company.MyClass.User;
import com.company.MysqlConnector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class StorageMenu extends JFrame {
    private static final MysqlConnector conn = MysqlConnector.getInstance();
    private MainMenu parent;

    public User getLoggedIn() {
        return loggedIn;
    }

    private User loggedIn;

    private JButton createButton;
    private JPanel panel1;
    private JButton deleteButton;
    private JButton editButton;
    private JButton visszaButton;


    public StorageMenu(MainMenu parent) {
        this.parent = parent;
        this.loggedIn = parent.getLoggedIn();

        Dimension dim = new Dimension(300,200);

        setContentPane(panel1);
        setPreferredSize(dim);
        setMinimumSize(dim);
        setLocationRelativeTo(null);
        setTitle("Raktár szerkesztése");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                parent.setVisible(true);
            }
        });

        if (loggedIn.getStorageId() == 0) {
            deleteButton.setEnabled(false);
            editButton.setEnabled(false);
        } else { createButton.setVisible(false); }

        visszaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                StorageMenu.this.dispose();
            }
        });
        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (conn.createStorageByUser(loggedIn)) {
                    editButton.setEnabled(true);
                    deleteButton.setEnabled(true);
                    createButton.setVisible(false);
                } else {
                    JOptionPane.showMessageDialog(StorageMenu.this, "A folyamat közben valamilyen hiba lépett fel!");
                }
            }
        });
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object[] options = new Object[]{"Igen", "Nem"};
                int choice = JOptionPane.showOptionDialog(StorageMenu.this, "Biztos eltávolítod a raktárad?", "Raktár törlése", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                if (choice == JOptionPane.YES_OPTION) {
                    if (conn.deleteStorageByUser(loggedIn)) {
                        JOptionPane.showMessageDialog(StorageMenu.this, "Sikeresen törölted a raktárad.");
                        StorageMenu.this.createButton.setVisible(true);
                        StorageMenu.this.editButton.setEnabled(false);
                        StorageMenu.this.deleteButton.setEnabled(false);
                    } else {
                        JOptionPane.showMessageDialog(StorageMenu.this, "Folyamat során ismeretlen hiba lépett fel!");
                    }
                }
            }
        });
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                StorageEdit se = new StorageEdit(StorageMenu.this);
                se.setVisible(true);
                StorageMenu.this.setVisible(false);
            }
        });
    }
}
