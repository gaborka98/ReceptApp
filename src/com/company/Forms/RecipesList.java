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
    private JButton editButton;
    private JButton insertButton;

    private MainMenu parent;

    private MysqlConnector conn = MysqlConnector.getInstance();

    private String[] columns = {"id", "Név", "Kategória", "Nehézség"};

    public User getLoggedIn() { return parent.getLoggedIn(); }

    public RecipesList(MainMenu parent) {
        this.parent = parent;
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                parent.setVisible(true);
            }
        });

        list.setModel(new DefaultTableModel(columns,0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        updateList();

        Dimension dim = new Dimension(500,250);

        setContentPane(panel1);
        setPreferredSize(dim);
        setMinimumSize(dim);
        setLocationRelativeTo(null);
        setTitle("Receptek");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();

        if (!parent.getLoggedIn().getModerator()) {
            insertButton.setVisible(false);
            editButton.setVisible(false);
        }

        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int row = list.rowAtPoint(e.getPoint());
                if (e.getClickCount() == 2 && list.getSelectedRow() != -1) {
                    Recipe selectedRercipe = conn.getRecipeById((int)list.getModel().getValueAt(row, 0));
                    if (selectedRercipe == null) {
                        JOptionPane.showMessageDialog(RecipesList.this, "A kiválasztott recept nem talalható");
                        return;
                    }
                    DetailView detailView = new DetailView(selectedRercipe, RecipesList.this);
                    detailView.setVisible(true);
                    RecipesList.this.setVisible(false);
                }
            }
        });
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RecipesList.this.dispose();
                RecipesList.super.setVisible(true);
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
