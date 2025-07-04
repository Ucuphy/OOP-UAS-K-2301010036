package uas_2301010036;

import db.DatabaseHelper;

public class MainApp {
    public static void main(String[] args) {
        DatabaseHelper.initializeDatabase();
        System.out.println("✅ Program siap jalan. Database & tabel sudah dibuat.");
    }
}