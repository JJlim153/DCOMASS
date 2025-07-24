/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package y3javaassignment1;

/**
 *
 * @author PC
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.sql.*;
import java.util.List;
import java.util.Properties;
import org.jdatepicker.impl.*;

public class GeneratePayslip extends JFrame {
    private JComboBox<String> usernameComboBox;
    private JDatePickerImpl datePicker;
    private JTextField baseSalaryField, bonusField, epfField, socsoField;
    private String loggedInUsername;
    private String loggedInRole;
    private PayrollService service;


    public GeneratePayslip(String loggedInUsername, String loggedInRole, PayrollService service){
        this.service = service;
        this.loggedInUsername = loggedInRole;
        this.loggedInRole = loggedInUsername;
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
        usernameComboBox.addActionListener(new ActionListener() {
        @Override
            public void actionPerformed(ActionEvent e) {
                loadLatestPayrollData();  // this is your method to prefill fields
            }
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

        // Salary Fields
        gbc.gridx = 0;
        gbc.gridy++;
        add(new JLabel("Base Salary:"), gbc);
        baseSalaryField = new JTextField(10);
        gbc.gridx = 1;
        add(baseSalaryField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        add(new JLabel("Bonus:"), gbc);
        bonusField = new JTextField(10);
        gbc.gridx = 1;
        add(bonusField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        add(new JLabel("EPF:"), gbc);
        epfField = new JTextField(10);
        gbc.gridx = 1;
        add(epfField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        add(new JLabel("SOCSO:"), gbc);
        socsoField = new JTextField(10);
        gbc.gridx = 1;
        add(socsoField, gbc);
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
        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                insertPayslip();
            }
        });

        // Back Button Action
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // close current GeneratePayslip window
                new HRDashboard(loggedInUsername,service,loggedInRole).setVisible(true); // open HRDashboard
            }
        });

    }
    
private void loadUsernames() {
    try {
        List<String> usernames = service.getApprovedUsernames();
        for (String name : usernames) {
            usernameComboBox.addItem(name);
        }

        // ✅ Select first item and load its payroll data immediately
        if (!usernames.isEmpty()) {
            usernameComboBox.setSelectedIndex(0); // optional, but makes sure first is selected
            loadLatestPayrollData(); // 💡 trigger data load manually here
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
            epfField.setText(String.valueOf(summary.getEpf()));
            socsoField.setText(String.valueOf(summary.getTax())); // or socso
            bonusField.setText("");
        } else {
            baseSalaryField.setText("");
            epfField.setText("");
            socsoField.setText("");
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
        java.sql.Date payDate = new java.sql.Date(selectedDate.getTime());

        double base = Double.parseDouble(baseSalaryField.getText());
        double bonus = Double.parseDouble(bonusField.getText());
        double epf = Double.parseDouble(epfField.getText());
        double socso = Double.parseDouble(socsoField.getText());

        double netSalary = base + bonus - epf - socso;

        boolean success = service.insertPayslip(username, payDate, base, bonus, epf, socso);
        if (success) {
            JOptionPane.showMessageDialog(this,
                String.format("Payslip generated and saved via RMI!\nNet Salary: RM %.2f", netSalary));
        } else {
            JOptionPane.showMessageDialog(this, "Failed to insert payslip.");
        }
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
    }
}




}

