/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package y3javaassignment1;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.rmi.RemoteException;
import java.util.List;

public class ViewPayslip extends JFrame {
    private JTable payslipTable;
    private String loggedInUsername;
    private String loggedInRole;
    private PayrollService service;

    public ViewPayslip(String username, String role, PayrollService service) {
        this.loggedInUsername = username;
        this.loggedInRole = role;
        this.service = service;

        setTitle("View Payslip");
        setSize(800, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Table setup
        payslipTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(payslipTable);
        add(scrollPane, BorderLayout.CENTER);

        // Load data
        loadPayslipData();

        // Back button
        JButton backButton = new JButton("Back");
        
        backButton.addActionListener(e -> {
            dispose();
            switch (loggedInRole) {
                case "Admin":
                    new AdminDashboard(loggedInUsername, service, loggedInRole).setVisible(true);
                    break;
                case "HR":
                    new HRDashboard(loggedInUsername, service, loggedInRole).setVisible(true);
                    break;
                case "Employee":
                    new EmployeeDashboard(loggedInUsername, service, loggedInRole).setVisible(true);
                    break;
                default:
                    JOptionPane.showMessageDialog(this, "Unknown role. Cannot return to dashboard.");
            }
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(backButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }


    
    private void loadPayslipData() {
        try {
            List<PayrollRecord> records;

            if (loggedInRole.equalsIgnoreCase("HR") || loggedInRole.equalsIgnoreCase("Admin")) {
                records = service.getAllPayslips(); // Fetch all
            } else {
                records = service.getPayslipsForUser(loggedInUsername); // Only user's own
            }

            String[] columns = {"Username", "Date", "Base Salary", "Bonus", "EPF", "SOCSO", "Net Pay"};
            DefaultTableModel model = new DefaultTableModel(columns, 0);

            for (PayrollRecord record : records) {
                Object[] row = {
                    record.getUsername() != null ? record.getUsername() : loggedInUsername, // fallback if needed
                    record.getPayDate().toString(),
                    record.getBaseSalary(),
                    record.getBonus(),
                    record.getEpf(),
                    record.getSocso(),
                    record.getNetPay()
                };
                model.addRow(row);
            }

            payslipTable.setModel(model);

        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this, "Error loading payslip data: " + e.getMessage());
        }
    }

}

