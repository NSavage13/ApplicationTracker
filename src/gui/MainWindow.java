package gui;

import db.ApplicationDAO;
import model.Application;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

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

        pack();
        setMinimumSize(new Dimension(850, 350));
        loadApplications(); 
        setLocationRelativeTo(null);
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
        // Dialog basics
        JDialog dialog = new JDialog(this, "Add Application", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);
    
        // Root with padding
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        dialog.setContentPane(root);
    
        // ---- FORM (labels right, fields aligned) ----
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.fill = GridBagConstraints.HORIZONTAL;
    
        JTextField companyField  = new JTextField(24);
        JTextField positionField = new JTextField(24);
    
        // yyyy-MM-dd field
        final JFormattedTextField dateField;
        JFormattedTextField dfTmp;
        try {
            javax.swing.text.MaskFormatter mf = new javax.swing.text.MaskFormatter("####-##-##");
            mf.setPlaceholderCharacter('_');
            dfTmp = new JFormattedTextField(mf);
        } catch (java.text.ParseException ex) {
            dfTmp = new JFormattedTextField();
        }
        dfTmp.setColumns(24);
        dfTmp.setText(java.time.LocalDate.now().toString());
        dateField = dfTmp; // cast to final
    
        JComboBox<String> statusBox = new JComboBox<>(new String[]{"Applied", "Interviewed", "Rejected", "Accepted"});
        JTextArea notesArea = new JTextArea(4, 24);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
    
        // helper to add a row
        java.util.function.BiConsumer<String, JComponent> row = (label, comp) -> {
            int y = form.getComponentCount() / 2;
            g.gridx = 0; g.gridy = y; g.weightx = 0;
            form.add(new JLabel(label, SwingConstants.RIGHT), g);
            g.gridx = 1; g.weightx = 1.0;
            form.add(comp instanceof JTextArea ? new JScrollPane(comp) : comp, g);
        };
    
        row.accept("Company:",  companyField);
        row.accept("Position:", positionField);
        row.accept("Date (YYYY-MM-DD):", dateField);
        row.accept("Status:",   statusBox);
        row.accept("Notes:",    notesArea);
    
        root.add(form, BorderLayout.CENTER);
    
        // ---- BUTTON BAR ----
        JButton submitBtn = new JButton("Submit");
        JButton cancelBtn = new JButton("Cancel");
    
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.add(cancelBtn);
        actions.add(submitBtn);
        root.add(actions, BorderLayout.SOUTH);
    
        // Default button (Enter) & ESC to close
        dialog.getRootPane().setDefaultButton(submitBtn);
        KeyStroke esc = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0);
        dialog.getRootPane().registerKeyboardAction(e -> dialog.dispose(), esc, JComponent.WHEN_IN_FOCUSED_WINDOW);
    
        // Enable/disable submit based on required fields
        submitBtn.setEnabled(false);
        javax.swing.event.DocumentListener dl = new javax.swing.event.DocumentListener() {
            void toggle() {
                boolean ok = !companyField.getText().trim().isEmpty()
                          && !positionField.getText().trim().isEmpty()
                          && dateField.getText().matches("\\d{4}-\\d{2}-\\d{2}");
                submitBtn.setEnabled(ok);
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { toggle(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { toggle(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { toggle(); }
        };
        companyField.getDocument().addDocumentListener(dl);
        positionField.getDocument().addDocumentListener(dl);
        dateField.getDocument().addDocumentListener(dl);
    
        // Submit action (your existing DAO flow kept)
        submitBtn.addActionListener(e -> {
            // final validation
            String date = dateField.getText().trim();
            if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
                JOptionPane.showMessageDialog(dialog, "Please enter date as YYYY-MM-DD.");
                return;
            }
            Application app = new Application(
                0,
                companyField.getText().trim(),
                positionField.getText().trim(),
                date,
                (String) statusBox.getSelectedItem(),
                notesArea.getText().trim()
            );
            dao.addApplication(app);
            JOptionPane.showMessageDialog(dialog, "Application added!");
            dialog.dispose();
            loadApplications();
        });
    
        cancelBtn.addActionListener(e -> dialog.dispose());
    
        // Final polish
        dialog.pack();                       // use natural sizes
        dialog.setLocationRelativeTo(this);  // center on parent
        dialog.setVisible(true);
    }
    

    private void openEditDialog(int rowIndex) {
        // Pull current values from the table/model
        int id          = (int)    tableModel.getValueAt(rowIndex, 0);
        String company  = (String) tableModel.getValueAt(rowIndex, 1);
        String position = (String) tableModel.getValueAt(rowIndex, 2);
        String date     = (String) tableModel.getValueAt(rowIndex, 3);
        String status   = (String) tableModel.getValueAt(rowIndex, 4);
        String notes    = (String) tableModel.getValueAt(rowIndex, 5);
    
        // --- Dialog shell ---
        JDialog dialog = new JDialog(this, "Edit Application", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);
    
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        dialog.setContentPane(root);
    
        // --- Form with alignment ---
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.fill = GridBagConstraints.HORIZONTAL;
    
        final JTextField companyField  = new JTextField(company, 24);
        final JTextField positionField = new JTextField(position, 24);
    
        // yyyy-MM-dd with mask
        final JFormattedTextField dateField;
        JFormattedTextField dfTmp;
        try {
            javax.swing.text.MaskFormatter mf = new javax.swing.text.MaskFormatter("####-##-##");
            mf.setPlaceholderCharacter('_');
            dfTmp = new JFormattedTextField(mf);
        } catch (java.text.ParseException ex) {
            dfTmp = new JFormattedTextField();
        }
        dfTmp.setColumns(24);
        dfTmp.setText(date);
        dateField = dfTmp;
    
        final JComboBox<String> statusBox =
            new JComboBox<>(new String[]{"Applied", "Interviewed", "Rejected", "Accepted"});
        statusBox.setSelectedItem(status);
    
        final JTextArea notesArea = new JTextArea(4, 24);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setText(notes);
    
        // helper to add rows
        java.util.function.BiConsumer<String, JComponent> row = (label, comp) -> {
            int y = form.getComponentCount() / 2;
            g.gridx = 0; g.gridy = y; g.weightx = 0;
            form.add(new JLabel(label, SwingConstants.RIGHT), g);
            g.gridx = 1; g.weightx = 1.0;
            form.add(comp instanceof JTextArea ? new JScrollPane(comp) : comp, g);
        };
    
        row.accept("Company:",  companyField);
        row.accept("Position:", positionField);
        row.accept("Date (YYYY-MM-DD):", dateField);
        row.accept("Status:",   statusBox);
        row.accept("Notes:",    notesArea);
    
        root.add(form, BorderLayout.CENTER);
    
        // --- Buttons ---
        JButton saveBtn   = new JButton("Save Changes");
        JButton cancelBtn = new JButton("Cancel");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.add(cancelBtn);
        actions.add(saveBtn);
        root.add(actions, BorderLayout.SOUTH);
    
        // Default button & ESC to close
        dialog.getRootPane().setDefaultButton(saveBtn);
        KeyStroke esc = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0);
        dialog.getRootPane().registerKeyboardAction(e -> dialog.dispose(),
                esc, JComponent.WHEN_IN_FOCUSED_WINDOW);
    
        // Enable only when required fields valid
        saveBtn.setEnabled(false);
        javax.swing.event.DocumentListener dl = new javax.swing.event.DocumentListener() {
            void toggle() {
                boolean ok = !companyField.getText().trim().isEmpty()
                          && !positionField.getText().trim().isEmpty()
                          && dateField.getText().matches("\\d{4}-\\d{2}-\\d{2}");
                saveBtn.setEnabled(ok);
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { toggle(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { toggle(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { toggle(); }
        };
        companyField.getDocument().addDocumentListener(dl);
        positionField.getDocument().addDocumentListener(dl);
        dateField.getDocument().addDocumentListener(dl);
        // initialize state
        SwingUtilities.invokeLater(() -> {
            // ensures button state is correct if fields were already valid
            saveBtn.setEnabled(!companyField.getText().trim().isEmpty()
                            && !positionField.getText().trim().isEmpty()
                            && dateField.getText().matches("\\d{4}-\\d{2}-\\d{2}"));
        });
    
        // Save action
        saveBtn.addActionListener(e -> {
            String d = dateField.getText().trim();
            if (!d.matches("\\d{4}-\\d{2}-\\d{2}")) {
                JOptionPane.showMessageDialog(dialog, "Please enter date as YYYY-MM-DD.");
                return;
            }
    
            Application updated = new Application(
                id,
                companyField.getText().trim(),
                positionField.getText().trim(),
                d,
                (String) statusBox.getSelectedItem(),
                notesArea.getText().trim()
            );
    
            dao.updateApplication(updated);
            JOptionPane.showMessageDialog(dialog, "Application updated!");
            dialog.dispose();
            loadApplications();
        });
    
        cancelBtn.addActionListener(e -> dialog.dispose());
    
        dialog.pack();
        dialog.setLocationRelativeTo(this);  // center on parent
        dialog.setVisible(true);
    }
    
}
