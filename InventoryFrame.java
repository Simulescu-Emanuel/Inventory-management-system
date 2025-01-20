package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class InventoryFrame extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField nameField;
    private JTextField categoryField;
    private JTextField quantityField;
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton searchButton;
    private JButton lowStockButton;
    private JButton showAllButton;

    public InventoryFrame() {
        setTitle("Gestionare Stoc - Magazin");
        setSize(800, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        populateTable();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new GridLayout(1, 6, 5, 5));

        topPanel.add(new JLabel("Nume:"));
        nameField = new JTextField();
        topPanel.add(nameField);

        topPanel.add(new JLabel("Categorie:"));
        categoryField = new JTextField();
        topPanel.add(categoryField);

        topPanel.add(new JLabel("Cantitate:"));
        quantityField = new JTextField();
        topPanel.add(quantityField);

        JPanel crudButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        addButton = new JButton("Adăugare");
        addButton.addActionListener(e -> addProduct());
        crudButtonPanel.add(addButton);

        updateButton = new JButton("Actualizare");
        updateButton.addActionListener(e -> updateProduct());
        crudButtonPanel.add(updateButton);

        deleteButton = new JButton("Ștergere");
        deleteButton.addActionListener(e -> deleteProduct());
        crudButtonPanel.add(deleteButton);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        searchButton = new JButton("Căutare");
        searchButton.addActionListener(e -> searchProducts());
        searchPanel.add(searchButton);

        lowStockButton = new JButton("Cantitate scăzută (< 5)");
        lowStockButton.addActionListener(e -> showLowStock());
        searchPanel.add(lowStockButton);

        showAllButton = new JButton("Afișează tot");
        showAllButton.addActionListener(e -> populateTable());
        searchPanel.add(showAllButton);

        tableModel = new DefaultTableModel();
        tableModel.setColumnIdentifiers(new String[] {"ID", "Nume", "Categorie", "Cantitate"});
        table = new JTable(tableModel);

        getContentPane().setLayout(new BorderLayout(5, 5));

        getContentPane().add(topPanel, BorderLayout.NORTH);

        Box centerBox = Box.createVerticalBox();
        centerBox.add(crudButtonPanel);
        centerBox.add(new JScrollPane(table));

        getContentPane().add(centerBox, BorderLayout.CENTER);
        getContentPane().add(searchPanel, BorderLayout.SOUTH);
    }

    private void populateTable() {
        tableModel.setRowCount(0);
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM products";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("name"));
                row.add(rs.getString("category"));
                row.add(rs.getInt("quantity"));
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Eroare la încărcarea produselor: " + ex.getMessage());
        }
    }

    private void addProduct() {
        String name = nameField.getText();
        String category = categoryField.getText();

        int quantity = Integer.parseInt(quantityField.getText());

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO products (name, category, quantity) VALUES (?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, category);
            ps.setInt(3, quantity);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Produs adăugat cu succes!");
            populateTable();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Eroare la adăugare: " + ex.getMessage());
        }
    }

    private void updateProduct() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Selectează un rând pentru actualizare!");
            return;
        }
        int id = (int) table.getValueAt(selectedRow, 0);

        String name = nameField.getText();
        String category = categoryField.getText();
        int quantity = Integer.parseInt(quantityField.getText());

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE products SET name=?, category=?, quantity=? WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, category);
            ps.setInt(3, quantity);
            ps.setInt(4, id);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Produs actualizat cu succes!");
            populateTable();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Eroare la actualizare: " + ex.getMessage());
        }
    }

    private void deleteProduct() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Selectează un rând pentru ștergere!");
            return;
        }
        int id = (int) table.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Ești sigur că vrei să ștergi acest produs?",
                "Confirmare",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "DELETE FROM products WHERE id=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, id);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Produs șters cu succes!");
                populateTable();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Eroare la ștergere: " + ex.getMessage());
            }
        }
    }

    private void searchProducts() {
        String name = nameField.getText();
        String category = categoryField.getText();

        StringBuilder sql = new StringBuilder("SELECT * FROM products WHERE 1=1 ");
        if (!name.isEmpty()) {
            sql.append(" AND name LIKE '%").append(name).append("%'");
        }
        if (!category.isEmpty()) {
            sql.append(" AND category LIKE '%").append(category).append("%'");
        }

        tableModel.setRowCount(0);
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql.toString())) {

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("name"));
                row.add(rs.getString("category"));
                row.add(rs.getInt("quantity"));
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Eroare la căutare: " + ex.getMessage());
        }
    }

    private void showLowStock() {
        tableModel.setRowCount(0);
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM products WHERE quantity < 5";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("name"));
                row.add(rs.getString("category"));
                row.add(rs.getInt("quantity"));
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Eroare la afișarea produselor cu stoc scăzut: " + ex.getMessage());
        }
    }
}


