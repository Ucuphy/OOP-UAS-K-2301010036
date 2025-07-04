package uas_2301010036;

import db.DatabaseHelper;

import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainAppGUI extends JFrame {

    private JDateChooser dateChooser, dateDari, dateSampai;
    private JTextField tfKeterangan, tfJumlah;
    private JComboBox<String> cbJenis;
    private DefaultTableModel tableModel;
    private JLabel saldoLabel;
    private JTable table;

    public MainAppGUI() {
        super("Catatan Keuangan");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        initUI();

        DatabaseHelper.initializeDatabase();
        loadData();
    }

    private void initUI() {
        JPanel panelInput = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panelInput.add(new JLabel("Tanggal:"), gbc);

        dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        gbc.gridx = 1;
        panelInput.add(dateChooser, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panelInput.add(new JLabel("Jenis:"), gbc);

        cbJenis = new JComboBox<>(new String[]{"Pemasukan", "Pengeluaran"});
        gbc.gridx = 1;
        panelInput.add(cbJenis, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panelInput.add(new JLabel("Keterangan:"), gbc);

        tfKeterangan = new JTextField();
        gbc.gridx = 1;
        panelInput.add(tfKeterangan, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panelInput.add(new JLabel("Jumlah:"), gbc);

        tfJumlah = new JTextField();
        gbc.gridx = 1;
        panelInput.add(tfJumlah, gbc);

        JPanel panelButton = new JPanel(new FlowLayout());
        JButton btnTambah = new JButton("Tambah");
        JButton btnEdit = new JButton("Edit");
        JButton btnHapus = new JButton("Hapus");
        panelButton.add(btnTambah);
        panelButton.add(btnEdit);
        panelButton.add(btnHapus);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        panelInput.add(panelButton, gbc);

        JPanel panelFilter = new JPanel(new FlowLayout());
        dateDari = new JDateChooser();
        dateDari.setDateFormatString("yyyy-MM-dd");
        dateSampai = new JDateChooser();
        dateSampai.setDateFormatString("yyyy-MM-dd");
        JButton btnFilter = new JButton("Filter");
        JButton btnReset = new JButton("Reset");
        panelFilter.add(new JLabel("Dari:"));
        panelFilter.add(dateDari);
        panelFilter.add(new JLabel("Sampai:"));
        panelFilter.add(dateSampai);
        panelFilter.add(btnFilter);
        panelFilter.add(btnReset);

        gbc.gridy = 5;
        panelInput.add(panelFilter, gbc);

        add(panelInput, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"ID", "Tanggal", "Jenis", "Keterangan", "Jumlah"}, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        saldoLabel = new JLabel("Saldo: 0", SwingConstants.CENTER);
        add(saldoLabel, BorderLayout.SOUTH);

        btnTambah.addActionListener(e -> tambahTransaksi());
        btnEdit.addActionListener(e -> editTransaksi());
        btnHapus.addActionListener(e -> hapusTransaksi());
        btnFilter.addActionListener(e -> filterTransaksi());
        btnReset.addActionListener(e -> {
            dateDari.setDate(null);
            dateSampai.setDate(null);
            loadData();
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    try {
                        Date date = new SimpleDateFormat("yyyy-MM-dd")
                                .parse(tableModel.getValueAt(row, 1).toString());
                        dateChooser.setDate(date);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    cbJenis.setSelectedItem(tableModel.getValueAt(row, 2).toString());
                    tfKeterangan.setText(tableModel.getValueAt(row, 3).toString());
                    tfJumlah.setText(tableModel.getValueAt(row, 4).toString());
                }
            }
        });
    }

    private void tambahTransaksi() {
        try {
            Date selectedDate = dateChooser.getDate();
            if (selectedDate == null) {
                JOptionPane.showMessageDialog(this, "Pilih tanggal.");
                return;
            }
            String tanggal = new SimpleDateFormat("yyyy-MM-dd").format(selectedDate);
            String jenis = cbJenis.getSelectedItem().toString();
            String keterangan = tfKeterangan.getText();
            double jumlah = Double.parseDouble(tfJumlah.getText());

            String sql = "INSERT INTO transaksi (tanggal, jenis, keterangan, jumlah) VALUES (?, ?, ?, ?)";
            try (Connection conn = DatabaseHelper.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, tanggal);
                pstmt.setString(2, jenis);
                pstmt.setString(3, keterangan);
                pstmt.setDouble(4, jumlah);
                pstmt.executeUpdate();
                clearForm();
                loadData();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void editTransaksi() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih dulu.");
            return;
        }
        try {
            int id = (int) tableModel.getValueAt(row, 0);
            Date selectedDate = dateChooser.getDate();
            String tanggal = new SimpleDateFormat("yyyy-MM-dd").format(selectedDate);
            String jenis = cbJenis.getSelectedItem().toString();
            String keterangan = tfKeterangan.getText();
            double jumlah = Double.parseDouble(tfJumlah.getText());

            String sql = "UPDATE transaksi SET tanggal=?, jenis=?, keterangan=?, jumlah=? WHERE id=?";
            try (Connection conn = DatabaseHelper.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, tanggal);
                pstmt.setString(2, jenis);
                pstmt.setString(3, keterangan);
                pstmt.setDouble(4, jumlah);
                pstmt.setInt(5, id);
                pstmt.executeUpdate();
                clearForm();
                loadData();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void hapusTransaksi() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih dulu.");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        String sql = "DELETE FROM transaksi WHERE id=?";
        try (Connection conn = DatabaseHelper.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            clearForm();
            loadData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void filterTransaksi() {
        Date dariDate = dateDari.getDate();
        Date sampaiDate = dateSampai.getDate();

        if (dariDate == null || sampaiDate == null) {
            JOptionPane.showMessageDialog(this, "Pilih tanggal Dari & Sampai.");
            return;
        }

        String dari = new SimpleDateFormat("yyyy-MM-dd").format(dariDate);
        String sampai = new SimpleDateFormat("yyyy-MM-dd").format(sampaiDate);

        loadData(dari, sampai);
    }

    private void loadData() {
        loadData(null, null);
    }

    private void loadData(String dari, String sampai) {
        tableModel.setRowCount(0);
        double pemasukan = 0, pengeluaran = 0;
        String sql = "SELECT * FROM transaksi";
        if (dari != null && sampai != null) sql += " WHERE tanggal BETWEEN ? AND ?";
        try (Connection conn = DatabaseHelper.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (dari != null && sampai != null) {
                pstmt.setString(1, dari);
                pstmt.setString(2, sampai);
            }
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String tanggal = rs.getString("tanggal");
                String jenis = rs.getString("jenis");
                String ket = rs.getString("keterangan");
                double jumlah = rs.getDouble("jumlah");
                tableModel.addRow(new Object[]{id, tanggal, jenis, ket, jumlah});
                if (jenis.equalsIgnoreCase("Pemasukan")) pemasukan += jumlah;
                else pengeluaran += jumlah;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
        saldoLabel.setText(String.format("💰 Pemasukan: %.2f | Pengeluaran: %.2f | Saldo: %.2f",
                pemasukan, pengeluaran, pemasukan - pengeluaran));
    }

    private void clearForm() {
        dateChooser.setDate(null);
        cbJenis.setSelectedIndex(0);
        tfKeterangan.setText("");
        tfJumlah.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainAppGUI().setVisible(true));
    }
}