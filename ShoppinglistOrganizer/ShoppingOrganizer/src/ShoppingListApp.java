package src;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class ShoppingListApp extends JFrame {
    private JComboBox<String> categoryComboBox;
    private JTextField itemNameField, descriptionField;
    private JSpinner quantitySpinner;
    private JTable table;
    private DefaultTableModel tableModel;

    private static final String DATA_FILE = "shopping_list_data.txt";

    public ShoppingListApp() {
        setTitle("Shopping List App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Set background color for the entire frame to black
        getContentPane().setBackground(Color.BLACK);

        // Top Panel for Inputs with a red background
        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Add New Item"));
        // inputPanel.setBackground(new Color(139, 69, 19)); // Red background for the input panel

        inputPanel.add(new JLabel("Item Name:"));
        itemNameField = new JTextField();
        inputPanel.add(itemNameField);

        inputPanel.add(new JLabel("Quantity:"));
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        inputPanel.add(quantitySpinner);

        inputPanel.add(new JLabel("Description:"));
        descriptionField = new JTextField();
        inputPanel.add(descriptionField);

        inputPanel.add(new JLabel("Category:"));
        categoryComboBox = new JComboBox<>(new String[]{"Groceries", "Electronics", "Clothing", "Other"});
        categoryComboBox.setEditable(true);
        categoryComboBox.setSelectedItem("Select or type a category");  // Placeholder text
        inputPanel.add(categoryComboBox);

        JButton addButton = new JButton("Add Item");
        inputPanel.add(addButton);
        add(inputPanel, BorderLayout.NORTH);

        // Button styling
        addButton.setBackground(new Color(255, 51, 51)); // Red background for the add button
        addButton.setForeground(Color.WHITE); // White text color
        addButton.setFont(new Font("Arial", Font.BOLD, 14));

        // Add listener to clear placeholder text when the user starts typing
        categoryComboBox.getEditor().getEditorComponent().addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                // Clear placeholder text when focus is gained
                if (categoryComboBox.getSelectedItem().equals("Select or type a category")) {
                    categoryComboBox.setSelectedItem("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                // Reset placeholder text if the user leaves the field empty
                if (categoryComboBox.getSelectedItem().toString().isEmpty()) {
                    categoryComboBox.setSelectedItem("Select or type a category");
                }
            }
        });

        // Table for displaying shopping list (added "Edit" and "Delete" columns)
        tableModel = new DefaultTableModel(new String[]{"Item Name", "Quantity", "Description", "Category", "Edit", "Delete"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Only "Edit" and "Delete" columns are editable
                return column == 4 || column == 5;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(30);  // Set row height for buttons
        table.setBackground(new Color(255, 204, 204)); // Light pink background for table
        table.setFont(new Font("Arial", Font.PLAIN, 14)); // Set font for table

        table.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor(new JCheckBox()));
        table.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane tableScrollPane = new JScrollPane(table);
        add(tableScrollPane, BorderLayout.CENTER);

        // Add button action listener
        addButton.addActionListener(e -> addItem());

        // Load saved data
        loadShoppingList();

        // Save data automatically on window close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveShoppingList();
                super.windowClosing(e);
            }
        });

        setSize(800, 600);
        setLocationRelativeTo(null);

        // Set focus on the itemNameField when the app starts
        SwingUtilities.invokeLater(() -> itemNameField.requestFocusInWindow());
    }

    private void addItem() {
        String itemName = itemNameField.getText();
        int quantity = (int) quantitySpinner.getValue();
        String description = descriptionField.getText();
        String category = categoryComboBox.getSelectedItem().toString();

        if (itemName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Item Name is required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Object[] row = {itemName, quantity, description, category, "Edit", "Delete"};
        tableModel.addRow(row);

        // Add the category to the combo box if it's new
        if (((DefaultComboBoxModel<String>) categoryComboBox.getModel()).getIndexOf(category) == -1) {
            categoryComboBox.addItem(category);
        }

        // Clear input fields
        itemNameField.setText("");
        descriptionField.setText("");
        quantitySpinner.setValue(1);
    }

    private void editItem(int rowIndex) {
        String newItemName = JOptionPane.showInputDialog(this, "Enter new item name:", tableModel.getValueAt(rowIndex, 0));
        if (newItemName != null) {  // If user did not press Cancel
            tableModel.setValueAt(newItemName, rowIndex, 0);
        } else {
            return;  // If Cancel was pressed, return without making any changes
        }

        String newQuantityStr = JOptionPane.showInputDialog(this, "Enter new quantity:", tableModel.getValueAt(rowIndex, 1));
        if (newQuantityStr != null) {  // If user did not press Cancel
            try {
                int newQuantity = Integer.parseInt(newQuantityStr);
                tableModel.setValueAt(newQuantity, rowIndex, 1);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid quantity. Please enter a number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            return;  // If Cancel was pressed, return without making any changes
        }

        String newDescription = JOptionPane.showInputDialog(this, "Enter new description:", tableModel.getValueAt(rowIndex, 2));
        if (newDescription != null) {  // If user did not press Cancel
            tableModel.setValueAt(newDescription, rowIndex, 2);
        } else {
            return;  // If Cancel was pressed, return without making any changes
        }

        String newCategory = JOptionPane.showInputDialog(this, "Enter new category:", tableModel.getValueAt(rowIndex, 3));
        if (newCategory != null) {  // If user did not press Cancel
            tableModel.setValueAt(newCategory, rowIndex, 3);
        } else {
            return;  // If Cancel was pressed, return without making any changes
        }
    }

    private void deleteItem(int rowIndex) {
        // Confirm deletion with a dialog
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this item?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            tableModel.removeRow(rowIndex);
        }
    }

    private class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setBackground(new Color(255, 102, 102)); // Red background for buttons
            setForeground(Color.WHITE); // White text color
            setFont(new Font("Arial", Font.BOLD, 12));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (column == 4) {
                setText("EDIT");
            } else if (column == 5) {
                setText("DELETE");
            }
            return this;
        }
    }

    private class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        private int rowIndex;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> {
                fireEditingStopped();
                isPushed = true;
                rowIndex = table.getSelectedRow();
                if (table.getColumnName(table.getSelectedColumn()).equals("Edit")) {
                    editItem(rowIndex);
                } else if (table.getColumnName(table.getSelectedColumn()).equals("Delete")) {
                    deleteItem(rowIndex);
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return label;
        }
    }

    private void saveShoppingList() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATA_FILE))) {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String itemName = (String) tableModel.getValueAt(i, 0);
                int quantity = (int) tableModel.getValueAt(i, 1);
                String description = (String) tableModel.getValueAt(i, 2);
                String category = (String) tableModel.getValueAt(i, 3);
                writer.write(itemName + "|" + quantity + "|" + description + "|" + category);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadShoppingList() {
        File dataFile = new File(DATA_FILE);
        if (dataFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\\|");
                    if (parts.length == 4) {
                        tableModel.addRow(new Object[]{parts[0], Integer.parseInt(parts[1]), parts[2], parts[3], "Edit", "Delete"});
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ShoppingListApp app = new ShoppingListApp();
            app.setVisible(true);
        });
    }
}
