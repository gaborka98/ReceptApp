package com.company.Forms;

import com.company.MyClass.Ingredient;
import com.company.MyClass.Recipe;
import com.company.MyClass.User;
import com.company.MysqlConnector;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class DetailView extends JFrame {

    private JTextPane descriptionText;
    private JButton visszaButton;
    private JButton craftButton;
    private JLabel recipeTitle;
    private JPanel panel;
    private JTable ingTable;
    private JPanel imgPanel;

    private Recipe recipe;
    private RecipesList parent;

    private MysqlConnector conn = MysqlConnector.getInstance();
    private String[] columns = {"id", "Név", "Mennyiség", "Raktáradban", "Maradék"};

    private User getLoggedIn() {return parent.getLoggedIn();}

    public DetailView(Recipe recipe, RecipesList parent) {
        this.recipe = recipe;
        this.parent = parent;

        descriptionText.setEditable(false);

        ingTable.setModel(new DefaultTableModel(columns,0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        TableColumnModel tcm = ingTable.getColumnModel();
        tcm.removeColumn(tcm.getColumn(0));
        updateList();

        Dimension dim = new Dimension(500,700);

        setContentPane(panel);
        setPreferredSize(dim);
        setMinimumSize(dim);
        setLocationRelativeTo(null);
        setTitle("Recept részletei");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                DetailView.this.dispose();
                parent.setVisible(true);
            }
        });

        if(parent.getLoggedIn().getStorageId() == -1) {
            craftButton.setEnabled(false);
            craftButton.setToolTipText("A recept elkészítéséhez létre kell hozz egy raktárat!");
        }

        recipeTitle.setText(recipe.getName());
        descriptionText.setText(recipe.getDescription());

        if (recipe.getImg() != null) {
            imgPanel.add(new JLabel(new ImageIcon(recipe.getImg().getScaledInstance(380, 214, Image.SCALE_SMOOTH))));
        } else {
            imgPanel.setVisible(false);
        }

        visszaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DetailView.this.dispose();
                parent.setVisible(true);
            }
        });
        craftButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                making();
            }
        });
    }

    private void making() {
        Boolean makeable = true;
        ArrayList<Ingredient> storage = getLoggedIn().getStorage();
        ArrayList<Ingredient> recipe = this.recipe.getIngredients();

        for (int i = 0; i < ingTable.getRowCount(); i++) {
            if (ingTable.getModel().getValueAt(i,4).toString().contains("-")) {
                makeable = false;
            }
        }

        for (Ingredient recipeIter : recipe) {
            for (Ingredient storageIter : storage) {
                if (recipeIter.getName().equals(storageIter.getName())) {
                    if (recipeIter.getMeasure() > storageIter.getMeasure()) {
                        makeable = false;
                    }
                }
            }
        }

        if (makeable) {
            Object[] options = {"Igen", "Nem"};
            int result = JOptionPane.showOptionDialog(this, "Biztos elkészíted a(z) " + recipeTitle.getText() + " nevű receptet?", "Recept elkészítése", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

            if (result == JOptionPane.YES_OPTION) {
                conn.makeRecipe(this.recipe.getId(), getLoggedIn().getStorageId(), getLoggedIn().getId());
                getLoggedIn().setStorage(conn.getAllStorageIngredientByStorageId(getLoggedIn().getStorageId()));
                JOptionPane.showMessageDialog(this, "A recept elkeszítése sikeresen megtörtént, az alapanyagokat levontuk a raktárkészletből");
            }
        } else {
            JOptionPane.showMessageDialog(DetailView.this, "A recept elkészítése nem lehetséges, hiányzó alapanyagok miatt!");
        }
    }

    private void updateList() {
        DefaultTableModel model = (DefaultTableModel) ingTable.getModel();
        model.setRowCount(0);

        ArrayList<Ingredient> storage = getLoggedIn().getStorage();
        ArrayList<Ingredient> recipe = this.recipe.getIngredients();

        if (recipe != null && storage != null) {
            for (Ingredient recIter : recipe) {
                Boolean alreadyhas = false;
                String userStorage = "";
                String different = "";
                for (Ingredient storIter : storage) {
                    if (recIter.getName().equals(storIter.getName())) {
                        different = getFancyMeasure(recIter.getGroup(), (storIter.getMeasure() - recIter.getMeasure())) + " " + generateMeasure(recIter.getGroup(), recIter.getUnit(), (storIter.getMeasure() - recIter.getMeasure()));
                        userStorage = storIter.getFancyMeasure() + " " + generateMeasure(storIter.getGroup(), storIter.getUnit(), storIter.getMeasure());
                        model.addRow(new Object[]{recIter.getId(), recIter.getName(), recIter.getFancyMeasure() + " " + recIter.getUnit(), userStorage,  different});
                        alreadyhas = true;
                    }
                    different = "-" + recIter.getFancyMeasure() + recIter.getUnit();
                    userStorage = " - ";
                }
                if (!alreadyhas) {
                    model.addRow(new Object[]{recIter.getId(), recIter.getName(), recIter.getFancyMeasure() + " " + recIter.getUnit(), userStorage, different});
                }
            }
        } else {model.addRow(new Object[] {"-1", "Nincs találat", "", ""});}

        ingTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String diff = (String) table.getModel().getValueAt(row, 4);

                if (diff != null && !diff.contains("-")) {
                    c.setBackground(Color.GREEN);
                } else {
                    c.setBackground(Color.RED);
                }
                c.setForeground(Color.BLACK);

                return c;
            }
        });

        ingTable.setModel(model);
    }

    private double getFancyMeasure(int group, double measure) {
        if (group == 1) {
            if (measure >= 1000) {
                return (measure / 1000.0);
            } else if (measure >= 10) {
                return (measure / 10.0);
            } else return measure;
        } else if (group == 2) {
            if (measure >= 1000) {
                return (measure / 1000.0);
            } else if (measure >= 10) {
                return (measure / 10.0);
            } else return measure;
        } else {
            return measure;
        }
    }

    private String generateMeasure(int group, String unit, double measure) {
        if (group == 1) {
            if (measure >= 1000) { return "kg"; }
            else if (measure >= 10) { return "dkg"; }
            else return "g";
        }
        else if (group == 2) {
            if (measure >= 1000) { return "l"; }
            else if (measure >= 10) { return "dl"; }
            else return "ml";
        }
        else return unit;
    }
}
