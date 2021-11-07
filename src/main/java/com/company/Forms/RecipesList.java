package com.company.Forms;

import com.company.MyClass.Filter;
import com.company.MyClass.Recipe;
import com.company.MyClass.User;
import com.company.MysqlConnector;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collection;

public class RecipesList extends JFrame {
    private JPanel panel1;
    private JTable list;
    private JButton filterButton;
    private JButton searchButton;
    private JButton backButton;
    private JButton updateButton;
    private JButton editButton;
    private JButton insertButton;
    private JButton kedvencekhezAdásEltávolításButton;

    private final MainMenu parent;
    private Filter filter;

    private MysqlConnector conn = MysqlConnector.getInstance();

    private String[] columns = {"id", "Név", "Kategória", "Nehézség", "kedvenc"};

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
        TableColumnModel tcm = list.getColumnModel();
        tcm.removeColumn(tcm.getColumn(0));
        updateList(conn.getAllRecipe());

        Dimension dim = new Dimension(600,350);

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
                parent.setVisible(true);
            }
        });
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateList(conn.getAllRecipe());
            }
        });
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String search = JOptionPane.showInputDialog("Add meg a keresni kívánt név töredéket");
                ArrayList<Recipe> recipes = conn.getRecipesBySearch(search);
                updateList(recipes);
            }
        });
        filterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FilterView filterView = new FilterView(RecipesList.this);
                filterView.setVisible(true);
                filterView.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        updateList(conn.getRecipesByFilter(filter, getLoggedIn()));
                    }
                });
            }
        });
        kedvencekhezAdásEltávolításButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int col, row;
                row = list.getSelectedRow();
                col = 4;

                if (row == -1) return;

                String selectedRecipe = list.getModel().getValueAt(row, col).toString();
                String selectedRecipeId = list.getModel().getValueAt(row, 0).toString();

                if ("*".equals(selectedRecipe)) {
                    conn.removeFavorite(getLoggedIn(), selectedRecipeId);
                } else {
                    conn.addFavorite(getLoggedIn(), selectedRecipeId);
                }
                updateList(conn.getAllRecipe());
            }
        });
        insertButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RecipeAdd add = new RecipeAdd(RecipesList.this, "insert", null);
                add.setVisible(true);
                RecipesList.this.setVisible(false);
                updateList(conn.getAllRecipe());
            }
        });
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RecipeAdd edit = new RecipeAdd(RecipesList.this, "edit", (Integer) list.getModel().getValueAt(list.getSelectedRow(), 0));
                edit.setVisible(true);
                RecipesList.this.setVisible(false);
                updateList(conn.getAllRecipe());
            }
        });
    }

    private void updateList(ArrayList<Recipe> recipes) {
        DefaultTableModel model = (DefaultTableModel) list.getModel();
        model.setRowCount(0);

        if (recipes != null && !recipes.isEmpty()) {
            for (Recipe iter : recipes) {

                model.addRow(new Object[]{iter.getId(), iter.getName(), iter.getCategory(), iter.getDifficulty(), (conn.favoriteExist(getLoggedIn(), iter) ? "*" : "") });
            }
        } else {model.addRow(new Object[] {"-1", "Nincs találat", "", "", ""});}
        list.setModel(model);
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }
}
