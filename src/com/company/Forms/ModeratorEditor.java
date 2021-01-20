package com.company.Forms;

import com.company.MyClass.User;
import com.company.MysqlConnector;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ModeratorEditor extends JFrame {
    private JPanel panel1;
    private JButton addModeratorButton;
    private JButton deleteSelectedButton;
    private JTable table1;
    private JButton backButton;
    private JButton updateButton;
    String [] columns = {"Felhasználónév", "E-mail cím"};

    private static final MysqlConnector conn = MysqlConnector.getInstance();

    private MainMenu parent;

    public ModeratorEditor(MainMenu parent) {
        this.parent = parent;

        table1.setModel(new DefaultTableModel(columns,0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        updateList();

        setLocationRelativeTo(null);
        setContentPane(panel1);
        setTitle("Moderátorok szerkesztése");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();

        addModeratorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        deleteSelectedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                backProcess();
            }
        });
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateList();
            }
        });
    }

    private void updateList() {
        DefaultTableModel model = (DefaultTableModel) table1.getModel();
        model.setRowCount(0);

        for (User iter : conn.getAllModerator()) {
            model.addRow(new Object[]{iter.getUsername(), iter.getEmail()});
        }
        table1.setModel(model);
    }

    private void backProcess() {
        parent.setVisible(true);
        this.dispose();
    }
}
