package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:oracle:thin:@localhost:1521:XE";
    private static final String USER = "SYSTEM";
    private static final String PASS = "23emi";

    static {
        try {
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver JDBC Oracle nu a fost găsit!", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public static void testConnection() {
        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("Conexiune reușită!");
        } catch (SQLException ex) {
            System.out.println("Eroare la conexiune: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        testConnection();
    }
}






