package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class LoginFrame extends JFrame {

    private JTextField userField;
    private JPasswordField passField;
    private JButton loginButton;
    private JButton signUpButton;

    public LoginFrame() {
        setTitle("Login");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));

        panel.add(new JLabel("Utilizator:"));
        userField = new JTextField();
        panel.add(userField);

        panel.add(new JLabel("Parola:"));
        passField = new JPasswordField();
        panel.add(passField);

        loginButton = new JButton("Login");
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                autentificare();
            }
        });

        signUpButton = new JButton("Sign Up");
        signUpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inregistrare();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(loginButton);
        buttonPanel.add(signUpButton);

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }

    private void autentificare() {
        String user = userField.getText();
        String pass = new String(passField.getPassword());

        if ("emi".equals(user) && "23".equals(pass)) {
            JOptionPane.showMessageDialog(this,
                    "Salut, 'emi'! Ai acces direct la pagina următoare.");
            InventoryFrame inventoryFrame = new InventoryFrame();
            inventoryFrame.setVisible(true);
            dispose();
        } else {
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, user);
                ps.setString(2, pass);

                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "Autentificare reușită!");
                    InventoryFrame inventoryFrame = new InventoryFrame();
                    inventoryFrame.setVisible(true);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Utilizator sau parolă incorecte!");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Eroare la conexiunea cu BD: " + ex.getMessage());
            }
        }
    }

    private void inregistrare() {
        String user = userField.getText();
        String pass = new String(passField.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Completați toate câmpurile!");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, user);
            ps.setString(2, pass);

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Cont creat cu succes! Acum vă puteți autentifica.");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Eroare la crearea contului: " + ex.getMessage());
        }
    }
}
