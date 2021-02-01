package com.company.Forms;

import com.company.MyClass.Recipe;
import com.company.MyClass.User;
import com.company.MysqlConnector;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class RecipesList extends JFrame {
    private JPanel panel1;
    private JTable list;
    private JButton filterButton;
    private JButton searchButton;
    private JButton backButton;
    private JButton updateButton;

    private MysqlConnector conn = MysqlConnector.getInstance();

    private MainMenu parent;

    private String[] columns = {"id", "Név", "Kategória", "Nehézség"};

    public RecipesList(MainMenu parent) {
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                parent.setVisible(true);
            }
        });

        this.parent = parent;
        list.setModel(new DefaultTableModel(columns,0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        updateList();

        Dimension dim = new Dimension(500,250);

        setLocationRelativeTo(null);
        setContentPane(panel1);
        setPreferredSize(dim);
        setMinimumSize(dim);
        setTitle("Receptek");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();

        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int row = list.rowAtPoint(e.getPoint());
                if (e.getClickCount() == 2 && list.getSelectedRow() != -1) {
                    DetailView detailView = new DetailView(list.getModel().getValueAt(row, 0));
                    detailView.setVisible(true);
                }
            }
        });
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RecipesList.this.dispose();
                parent.setVisible(true);
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
        DefaultTableModel model = (DefaultTableModel) list.getModel();
        model.setRowCount(0);
        TableColumnModel tcm = list.getColumnModel();
        tcm.removeColumn(tcm.getColumn(0));

        ArrayList<Recipe> recipes = conn.getAllRecipe();

        if (!recipes.isEmpty()) {
            for (Recipe iter : recipes) {
                model.addRow(new Object[]{iter.getId(), iter.getName(), iter.getCategory(), iter.getDifficulty()});
            }
        }
        list.setModel(model);
    }
}
