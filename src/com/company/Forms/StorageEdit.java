package com.company.Forms;

import com.company.MyClass.Ingredient;
import com.company.MyClass.Recipe;
import com.company.MyClass.User;
import com.company.MysqlConnector;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

public class StorageEdit extends JFrame {
    private StorageMenu parent;
    private JPanel panel1;
    private JButton addButton;
    private JButton visszaButton;
    private JButton removeButton;
    private JTable table1;

    private final MysqlConnector conn = MysqlConnector.getInstance();
    private final String[] columns = {"id", "Név", "Egység", "Mértékegység"};

    public User getLoggedIn() { return parent.getLoggedIn(); }

    public StorageEdit(StorageMenu parent) {
        this.parent = parent;

        table1.setModel(new DefaultTableModel(columns,0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        TableColumnModel tcm = table1.getColumnModel();
        tcm.removeColumn(tcm.getColumn(0));
        updateList(conn.getAllStorageIngredientByStorageId(getLoggedIn().getStorageId()));

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                parent.setVisible(true);
            }
        });

        Dimension dim = new Dimension(500, 250);

        setContentPane(panel1);
        setPreferredSize(dim);
        setMinimumSize(dim);
        setLocationRelativeTo(null);
        setTitle("Raktár szerkesztése");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();

        visszaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                StorageEdit.this.dispose();
                parent.setVisible(true);
            }
        });

        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelectedIngredients();
            }
        });
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                IngAdd add = new IngAdd(StorageEdit.this);
                add.setVisible(true);
            }
        });
    }

    private void deleteSelectedIngredients() {
        int col, row;
        row = table1.getSelectedRow();
        col = 0;

        if (row == -1) return;

        String name = table1.getModel().getValueAt(row, 1).toString();
        String selectedId = table1.getModel().getValueAt(row, col).toString();
        Object[] options = {"Igen", "Nem"};
        int result = JOptionPane.showOptionDialog(this, "Biztos eltávolítod a raktárodból a(z) " + name + " nevű hozzávalót?", "Hozzávaló eltávolítása", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (result == JOptionPane.YES_OPTION) {
            conn.deleteIngredientsById(Integer.parseInt(selectedId));
        }
        updateList(conn.getAllStorageIngredientByStorageId(getLoggedIn().getStorageId()));
    }

    public void addIngredient(Ingredient add) {
        conn.addIngredientToStorage(getLoggedIn().getStorageId(), add);
        getLoggedIn().setStorage(conn.getAllStorageIngredientByStorageId(getLoggedIn().getStorageId()));
        updateList(conn.getAllStorageIngredientByStorageId(getLoggedIn().getStorageId()));
    }

    private void updateList(ArrayList<Ingredient> ings) {
        DefaultTableModel model = (DefaultTableModel) table1.getModel();
        model.setRowCount(0);

        if (ings != null && !ings.isEmpty()) {
            for (Ingredient iter : ings) {
                model.addRow(new Object[]{iter.getId(), iter.getName(), iter.getFancyMeasure(), iter.getUnit() });
            }
        } else {model.addRow(new Object[] {"-1", "Nincs találat", "", "", ""});}
        table1.setModel(model);
    }
}
