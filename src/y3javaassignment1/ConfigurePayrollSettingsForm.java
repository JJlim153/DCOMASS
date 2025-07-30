package y3javaassignment1;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;

public class ConfigurePayrollSettingsForm extends JFrame {
    private JTextField epfField, socsoField, taxField;
    private PayrollService service;

    public ConfigurePayrollSettingsForm(PayrollService service) {
        this.service = service;

        setTitle("Configure Payroll Settings");
        setSize(400, 250);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Labels and Fields
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("EPF Rate (%):"), gbc);
        epfField = new JTextField(10);
        gbc.gridx = 1;
        add(epfField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        add(new JLabel("SOCSO Rate (%):"), gbc);
        socsoField = new JTextField(10);
        gbc.gridx = 1;
        add(socsoField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        add(new JLabel("TAX Rate(%):"), gbc);
        taxField = new JTextField(10);
        gbc.gridx = 1;
        add(taxField, gbc);

        // Load current settings
        loadSettings();

        // Buttons
        gbc.gridx = 1;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton saveButton = new JButton("Save");
        add(saveButton, gbc);

        saveButton.addActionListener(e -> saveSettings());
    }

    private void loadSettings() {
        try {
            PayrollSettings settings = service.getPayrollSettings();
            epfField.setText(String.valueOf(settings.getEpfRate() * 100));
            socsoField.setText(String.valueOf(settings.getSocsoRate() * 100));
            taxField.setText(String.valueOf(settings.getTaxRate() * 100));
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this, "Error loading settings: " + e.getMessage());
        }
    }


    private void saveSettings() {
        try {
            double epfInput = Double.parseDouble(epfField.getText());
            double socsoInput = Double.parseDouble(socsoField.getText());
            double taxInput = Double.parseDouble(taxField.getText());

            // Convert percentage to decimal
            double epf = epfInput / 100.0;
            double socso = socsoInput / 100.0;
            double tax = taxInput / 100.0;

            boolean updated = service.updatePayrollSettings(epf, socso, tax);
            if (updated) {
                JOptionPane.showMessageDialog(this, "Settings updated successfully!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update settings.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numeric values.");
        } catch (RemoteException ex) {
            JOptionPane.showMessageDialog(this, "Error saving settings: " + ex.getMessage());
        }
    }

}
