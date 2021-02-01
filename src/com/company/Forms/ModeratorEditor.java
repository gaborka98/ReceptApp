package com.company.Forms;

import com.company.MyClass.User;
import com.company.MysqlConnector;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

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

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                parent.setVisible(true);
            }
        });

        Dimension dim = new Dimension(500,250);

        setLocationRelativeTo(null);
        setContentPane(panel1);
        setPreferredSize(dim);
        setMinimumSize(dim);
        setTitle("Moderátorok szerkesztése");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                parent.setVisible(true);
            }
        });

        addModeratorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addModeratorProcess();
            }
        });

        deleteSelectedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelectedModerator();
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

    private void deleteSelectedModerator() {
        int col, row;
        row = table1.getSelectedRow();
        col = 0;
        String username = table1.getValueAt(row, col).toString();
        Object[] options = {"Igen", "Nem"};
        int result = JOptionPane.showOptionDialog(this, "Biztos eltávolítod a moderátorok közül " + username + " nevű felhasználót?", "Moderátor eltávolítása", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (result == JOptionPane.YES_OPTION) {
            if (conn.updatePlayerModeratorByUsername(username, false)) {
                JOptionPane.showMessageDialog(this, "Sikeresen eltávolítottad a felhasználót a moderátorok közül.");
                updateList();
            } else {
                JOptionPane.showMessageDialog(this, "Ismeretlen hiba lépett fel a művelet során!");
            }
        }
    }

    private void addModeratorProcess() {
        String username = JOptionPane.showInputDialog(this, "Kérem a felhasználó nevét");
        if (!username.isEmpty()) {
            User updatedUser = conn.getLoggedInUser(username);
            if (updatedUser != null) {
                if (!updatedUser.getModerator()) {
                    if (conn.updatePlayerModeratorByUsername(username, true)) {
                        updateList();
                    } else {
                        JOptionPane.showMessageDialog(this, "Ismeretlen hiba lépett fel a művelet során");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Ez a felhasználó már rendelkezik moderátori joggal!");
                }
            } else {
                JOptionPane.showMessageDialog(this, "A megadott felhasználó nem létezik!");
            }
        }
    }

    private void updateList() {
        DefaultTableModel model = (DefaultTableModel) table1.getModel();
        model.setRowCount(0);

        ArrayList<User> moderators = conn.getAllModerator();

        if (!moderators.isEmpty()) {
            for (User iter : conn.getAllModerator()) {
                model.addRow(new Object[]{iter.getUsername(), iter.getEmail()});
            }
        }
        table1.setModel(model);
    }

    private void backProcess() {
        parent.setVisible(true);
        this.dispose();
    }
}
