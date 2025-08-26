package gui;

import db.ApplicationDAO;
import model.Application;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.List;

public class MainWindow extends JFrame {

    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> filterBox;
    private ApplicationDAO dao = new ApplicationDAO();

    public MainWindow() {
        setTitle("Job Application Tracker");
        setSize(900, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Filter Dropdown
        String[] filterOptions = {"All", "Applied", "Interviewed", "Rejected", "Accepted"};
        filterBox = new JComboBox<>(filterOptions);
        filterBox.addActionListener(e -> loadApplications());
        add(filterBox, BorderLayout.NORTH);

        // Table Setup with Non-Editable Model
        String[] columns = {"ID", "Company", "Position", "Date", "Status", "Notes"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Disable cell editing
            }
        };
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        table.removeColumn(table.getColumnModel().getColumn(0)); // remove id view
        add(scrollPane, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel();
        JButton addBtn = new JButton("Add Application");
        JButton deleteBtn = new JButton("Delete Selected");
        buttonPanel.add(addBtn);
        buttonPanel.add(deleteBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        // Button Actions
        addBtn.addActionListener(e -> openAddDialog());
        deleteBtn.addActionListener(e -> deleteSelected());

        // Double-Click Listener for Editing
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    openEditDialog(table.getSelectedRow());
                }
            }
        });

        loadApplications(); 
        setVisible(true);
    }

    private void loadApplications() {
        tableModel.setRowCount(0); // Clear table
        List<Application> apps = dao.getAllApplications();
        String selectedFilter = (String) filterBox.getSelectedItem();

        for (Application app : apps) {
            if (!"All".equals(selectedFilter) && !app.getStatus().equalsIgnoreCase(selectedFilter)) {
                continue;
            }
            tableModel.addRow(new Object[]{
                app.getId(),
                app.getCompany(),
                app.getPosition(),
                app.getApplicationDate(),
                app.getStatus(),
                app.getNotes()
            });
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a row to delete.");
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        dao.deleteApplication(id);
        tableModel.removeRow(row);
        JOptionPane.showMessageDialog(this, "Application deleted.");
    }

    private void openAddDialog() {
        JDialog dialog = new JDialog(this, "Add Application", true);
        dialog.setLayout(new GridLayout(7, 2, 5, 5));
        dialog.setSize(400, 350);

        JTextField companyField = new JTextField();
        JTextField positionField = new JTextField();
        JTextField dateField = new JTextField(LocalDate.now().toString());
        JComboBox<String> statusBox = new JComboBox<>(new String[]{"Applied", "Interviewed", "Rejected", "Accepted"});
        JTextArea notesArea = new JTextArea();
        JButton submitBtn = new JButton("Submit");

        dialog.add(new JLabel("Company:")); dialog.add(companyField);
        dialog.add(new JLabel("Position:")); dialog.add(positionField);
        dialog.add(new JLabel("Date (YYYY-MM-DD):")); dialog.add(dateField);
        dialog.add(new JLabel("Status:")); dialog.add(statusBox);
        dialog.add(new JLabel("Notes:")); dialog.add(new JScrollPane(notesArea));
        dialog.add(new JLabel()); dialog.add(submitBtn);

        submitBtn.addActionListener(e -> {
            Application app = new Application(
                0,
                companyField.getText(),
                positionField.getText(),
                dateField.getText(),
                (String) statusBox.getSelectedItem(),
                notesArea.getText()
            );

            dao.addApplication(app);
            JOptionPane.showMessageDialog(dialog, "Application added!");
            dialog.dispose();
            loadApplications();
        });

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void openEditDialog(int rowIndex) {
        int id = (int) tableModel.getValueAt(rowIndex, 0);
        String company = (String) tableModel.getValueAt(rowIndex, 1);
        String position = (String) tableModel.getValueAt(rowIndex, 2);
        String date = (String) tableModel.getValueAt(rowIndex, 3);
        String status = (String) tableModel.getValueAt(rowIndex, 4);
        String notes = (String) tableModel.getValueAt(rowIndex, 5);

        JDialog dialog = new JDialog(this, "Edit Application", true);
        dialog.setLayout(new GridLayout(7, 2, 5, 5));
        dialog.setSize(400, 350);

        JTextField companyField = new JTextField(company);
        JTextField positionField = new JTextField(position);
        JTextField dateField = new JTextField(date);
        JComboBox<String> statusBox = new JComboBox<>(new String[]{"Applied", "Interviewed", "Rejected", "Accepted"});
        statusBox.setSelectedItem(status);
        JTextArea notesArea = new JTextArea(notes);
        JButton submitBtn = new JButton("Save Changes");

        dialog.add(new JLabel("Company:")); dialog.add(companyField);
        dialog.add(new JLabel("Position:")); dialog.add(positionField);
        dialog.add(new JLabel("Date (YYYY-MM-DD):")); dialog.add(dateField);
        dialog.add(new JLabel("Status:")); dialog.add(statusBox);
        dialog.add(new JLabel("Notes:")); dialog.add(new JScrollPane(notesArea));
        dialog.add(new JLabel()); dialog.add(submitBtn);

        submitBtn.addActionListener(e -> {
            Application updatedApp = new Application(
                id,
                companyField.getText(),
                positionField.getText(),
                dateField.getText(),
                (String) statusBox.getSelectedItem(),
                notesArea.getText()
            );

            dao.updateApplication(updatedApp);
            JOptionPane.showMessageDialog(dialog, "Application updated!");
            dialog.dispose();
            loadApplications();
        });

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}
