package y3javaassignment1;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.rmi.RemoteException;
import java.sql.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jdatepicker.impl.*;

public class GeneratePayslip extends JFrame {
    private JComboBox<String> usernameComboBox;
    private JDatePickerImpl datePicker;
    private JTextField baseSalaryField, bonusField;
    private String loggedInUsername;
    private String loggedInRole;
    private PayrollService service;

    private JRadioButton individualRadio, groupRadio;
    private ButtonGroup radioGroup;

    public GeneratePayslip(String loggedInUsername, String loggedInRole, PayrollService service) {
        this.service = service;
        this.loggedInUsername = loggedInUsername;
        this.loggedInRole = loggedInRole;

        setTitle("Generate Payslip");
        setSize(500, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Radio buttons
        individualRadio = new JRadioButton("Individual", true);
        groupRadio = new JRadioButton("Group (Subrole)");
        radioGroup = new ButtonGroup();
        radioGroup.add(individualRadio);
        radioGroup.add(groupRadio);

        JPanel radioPanel = new JPanel(new FlowLayout());
        radioPanel.add(individualRadio);
        radioPanel.add(groupRadio);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(radioPanel, gbc);
        gbc.gridwidth = 1;

        // Username/Subrole Dropdown
        JLabel userLabel = new JLabel("Select:");
        gbc.gridy++;
        gbc.gridx = 0;
        add(userLabel, gbc);

        usernameComboBox = new JComboBox<>();
        gbc.gridx = 1;
        add(usernameComboBox, gbc);
        usernameComboBox.addActionListener(e -> {
            if (individualRadio.isSelected()) loadLatestPayrollData();
        });

        // Date Picker
        JLabel dateLabel = new JLabel("Select Date:");
        gbc.gridx = 0;
        gbc.gridy++;
        add(dateLabel, gbc);

        UtilDateModel model = new UtilDateModel();
        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");
        JDatePanelImpl datePanel = new JDatePanelImpl(model, p);
        datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());
        gbc.gridx = 1;
        add(datePicker, gbc);

        // Base Salary
        gbc.gridx = 0;
        gbc.gridy++;
        add(new JLabel("Base Salary:"), gbc);
        baseSalaryField = new JTextField(10);
        gbc.gridx = 1;
        add(baseSalaryField, gbc);

        // Bonus
        gbc.gridx = 0;
        gbc.gridy++;
        add(new JLabel("Bonus:"), gbc);
        bonusField = new JTextField(10);
        gbc.gridx = 1;
        add(bonusField, gbc);

        // Submit and Back
        gbc.gridx = 1;
        gbc.gridy++;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton generateButton = new JButton("Generate Payslip");
        JButton backButton = new JButton("Back");
        buttonPanel.add(generateButton);
        buttonPanel.add(backButton);
        add(buttonPanel, gbc);

        // Actions
        generateButton.addActionListener(e -> {
            if (individualRadio.isSelected()) {
                insertPayslip();
            } else {
                insertPayslipForGroup();
            }
        });

        backButton.addActionListener(e -> {
            dispose();
            new HRDashboard(loggedInUsername, service, loggedInRole).setVisible(true);
        });

        // Load default options
        individualRadio.addActionListener(e -> loadUsernames());
        groupRadio.addActionListener(e -> loadSubroles());

        loadUsernames(); // Default load
    }

    private void loadUsernames() {
        try {
            usernameComboBox.removeAllItems();
            List<String> usernames = service.getApprovedUsernames();
            for (String name : usernames) {
                usernameComboBox.addItem(name);
            }
            if (!usernames.isEmpty()) {
                usernameComboBox.setSelectedIndex(0);
                loadLatestPayrollData();
            }
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this, "Failed to load usernames: " + e.getMessage());
        }
    }

    private void loadSubroles() {
        try {
            usernameComboBox.removeAllItems();
            List<String> subroles = service.getDistinctSubroles();
            for (String sr : subroles) {
                usernameComboBox.addItem(sr);
            }
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this, "Failed to load subroles: " + e.getMessage());
        }
    }

    private void loadLatestPayrollData() {
        if (!individualRadio.isSelected()) return;
        String selectedUsername = (String) usernameComboBox.getSelectedItem();
        if (selectedUsername == null) return;

        try {
            PayrollSummary summary = service.getLatestPayrollForUser(selectedUsername);
            if (summary != null) {
                baseSalaryField.setText(String.valueOf(summary.getBaseSalary()));
                bonusField.setText("");
            } else {
                baseSalaryField.setText("");
                bonusField.setText("");
            }
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this, "Error fetching previous data: " + e.getMessage());
        }
    }

    private void insertPayslip() {
        try {
            String username = (String) usernameComboBox.getSelectedItem();
            java.util.Date selectedDate = (java.util.Date) datePicker.getModel().getValue();

            if (selectedDate == null) {
                JOptionPane.showMessageDialog(this, "Please select a date.");
                return;
            }

            Date payDate = new Date(selectedDate.getTime());
            double base = Double.parseDouble(baseSalaryField.getText().trim());
            double bonus = Double.parseDouble(bonusField.getText().trim());

            boolean success = service.insertPayslip(username, payDate, base, bonus);
            if (success) {
                JOptionPane.showMessageDialog(this, "Payslip generated successfully for " + username);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to insert payslip.");
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

        private void insertPayslipForGroup() {
        String subrole = (String) usernameComboBox.getSelectedItem();
        java.util.Date selectedDate = (java.util.Date) datePicker.getModel().getValue();

        if (subrole == null || selectedDate == null) {
            JOptionPane.showMessageDialog(this, "Please select subrole and date.");
            return;
        }

        try {
            double base = Double.parseDouble(baseSalaryField.getText().trim());
            double bonus = Double.parseDouble(bonusField.getText().trim());
            Date payDate = new Date(selectedDate.getTime());

            List<String> usernames = service.getUsernamesBySubrole(subrole);
            ExecutorService executor = Executors.newFixedThreadPool(5); // parallel thread pool

            for (String user : usernames) {
                executor.submit(() -> {
                    try {
                        // Simulate unpredictable execution time
                        int delay = (int)(Math.random() * 1000); // 0–1000ms
                        Thread.sleep(delay);

                        service.insertPayslip(user, payDate, base, bonus);
                        System.out.println("Payslip done for: " + user);

                    } catch (Exception e) {
                        System.err.println("[" + Thread.currentThread().getName() + "] Error for: " + user + " - " + e.getMessage());
                    }
                });
            }

            executor.shutdown();
            JOptionPane.showMessageDialog(this, "Concurrent payslip generation started for subrole: " + subrole);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid salary/bonus.");
        } catch (RemoteException ex) {
            JOptionPane.showMessageDialog(this, "Remote error: " + ex.getMessage());
        }
    }


}
