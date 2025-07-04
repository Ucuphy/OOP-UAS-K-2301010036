package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseHelper {

    private static final String URL = "jdbc:mysql://localhost:3306/catatan_keuangan";
    private static final String USER = "root"; // ganti kalau user lain
    private static final String PASS = "";     // ganti kalau ada password

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public static void initializeDatabase() {
        try (Connection conn = connect()) {
            System.out.println("✅ Koneksi ke MySQL berhasil.");
        } catch (SQLException e) {
            System.err.println("❌ Gagal konek ke MySQL: " + e.getMessage());
        }
    }
}