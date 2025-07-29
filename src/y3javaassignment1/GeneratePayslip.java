package y3javaassignment1;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.rmi.RemoteException;
import java.sql.Date;
import java.util.List;
import java.util.Properties;
import org.jdatepicker.impl.*;

public class GeneratePayslip extends JFrame {
    private JComboBox<String> usernameComboBox;
    private JDatePickerImpl datePicker;
    private JTextField baseSalaryField, bonusField;
    private String loggedInUsername;
    private String loggedInRole;
    private PayrollService service;

    public GeneratePayslip(String loggedInUsername, String loggedInRole, PayrollService service) {
        this.service = service;
        this.loggedInUsername = loggedInUsername;
        this.loggedInRole = loggedInRole;

        setTitle("Generate Payslip");
        setSize(450, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Username Dropdown
        JLabel userLabel = new JLabel("Select Username:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(userLabel, gbc);

        usernameComboBox = new JComboBox<>();
        gbc.gridx = 1;
        add(usernameComboBox, gbc);
        usernameComboBox.addActionListener(e -> loadLatestPayrollData());

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

        // Load user dropdown
        loadUsernames();

        // Submit Button
        gbc.gridx = 1;
        gbc.gridy++;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton generateButton = new JButton("Generate Payslip");
        JButton backButton = new JButton("Back");
        buttonPanel.add(generateButton);
        buttonPanel.add(backButton);
        add(buttonPanel, gbc);

        // Generate Button Action
        generateButton.addActionListener(e -> insertPayslip());

        // Back Button Action
        backButton.addActionListener(e -> {
            dispose(); // close current window
            new HRDashboard(loggedInUsername, service, loggedInRole).setVisible(true);
        });
    }

    private void loadUsernames() {
        try {
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

    private void loadLatestPayrollData() {
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

            // Step 1: Fetch rates from PayrollSettings table
            PayrollSettings settings = service.getPayrollSettings();
            double epfRate = settings.getEpfRate();
            double socsoRate = settings.getSocsoRate();
            double taxRate = settings.getTaxRate();

            // Step 2: Calculate payroll components
            double gross = base + bonus;
            double epf = gross * epfRate;
            double socso = gross * socsoRate;
            double tax = gross * taxRate;
            double annualIncome = base; // or use (base * 12) depending on your intent

            double netSalary = gross - epf - socso - tax;

            // Step 3: Call updated backend insertPayslip
            boolean success = service.insertPayslip(username, payDate, base, bonus);


            // Step 4: Feedback to user
            if (success) {
                JOptionPane.showMessageDialog(this,
                    String.format("Payslip generated!\nGross: RM %.2f\nEPF: RM %.2f\nSOCSO: RM %.2f\nTAX: RM %.2f\nNet: RM %.2f",
                        gross, epf, socso, tax, netSalary));
            } else {
                JOptionPane.showMessageDialog(this, "Failed to insert payslip.");
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for salary and bonus.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

}
