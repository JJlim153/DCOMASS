package y3javaassignment1;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.rmi.RemoteException;
import java.util.List;

public class ModifyUsersForm extends javax.swing.JFrame {

    private PayrollService service;

    public ModifyUsersForm(PayrollService service) {
        this.service = service;
        initComponents();
        loadUserData();
    }

    private void initComponents() {
        setTitle("Modify Users");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        String[] columnNames = {"Username", "Password", "Role", "First Name", "Last Name", "IC/Passport", "Status", "Subrole"};
        tableModel = new DefaultTableModel(columnNames, 0);
        userTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(userTable);

        JButton editButton = new JButton("Edit Selected User");
        editButton.addActionListener(e -> openEditDialog());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(scrollPane);
        panel.add(editButton);

        add(panel);
        pack();
        setLocationRelativeTo(null);
    }

    private void loadUserData() {
        try {
            List<String[]> users = service.getAllUsers();
            for (String[] user : users) {
                if (user.length >= 7) {
                    String username = user[0];
                    String role = user[5];
                    String subrole = "-";
                    if ("Employee".equalsIgnoreCase(role)) {
                        try {
                            subrole = service.getSubroleForUser(username);
                            if (subrole == null || subrole.trim().isEmpty()) subrole = "-";
                        } catch (RemoteException e) {
                            subrole = "Error";
                        }
                    }

                    tableModel.addRow(new Object[]{
                        user[0], // username
                        user[1], // password
                        role,
                        user[2], // firstname
                        user[3], // lastname
                        user[4], // ic_passport
                        user[6], // status
                        subrole
                    });
                }
            }

        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this, "Error loading users: " + e.getMessage());
        }
    }



private void openEditDialog() {
    int row = userTable.getSelectedRow();
    if (row == -1) {
        JOptionPane.showMessageDialog(this, "Please select a user.");
        return;
    }

    String username = (String) tableModel.getValueAt(row, 0);
    String role = (String) tableModel.getValueAt(row, 2);
    String firstName = (String) tableModel.getValueAt(row, 3);
    String lastName = (String) tableModel.getValueAt(row, 4);
    String ic = (String) tableModel.getValueAt(row, 5);
    String currentStatus = (String) tableModel.getValueAt(row, 6);

    JTextField firstNameField = new JTextField(firstName);
    JTextField lastNameField = new JTextField(lastName);
    JTextField icField = new JTextField(ic);
    JTextField roleField = new JTextField(role);
    roleField.setEditable(false); // Make role read-only

    // ✅ Create dropdown for status
    String[] statuses = {"Pending", "Approved", "Rejected"};
    JComboBox<String> statusDropdown = new JComboBox<>(statuses);
    statusDropdown.setSelectedItem(currentStatus);
    
    String currentSubrole = (String) tableModel.getValueAt(row, 7);
    
    JComboBox<String> subroleDropdown = new JComboBox<>();
    subroleDropdown.setEnabled("Employee".equalsIgnoreCase(role));

    if ("Employee".equalsIgnoreCase(role)) {
        try {
            List<String> subroles = service.getDistinctSubroles(); // already exists for group payslip
            for (String sr : subroles) {
                subroleDropdown.addItem(sr);
            }
            subroleDropdown.setEditable(true); // allow new subroles
            subroleDropdown.setSelectedItem("-".equals(currentSubrole) ? null : currentSubrole);
        } catch (RemoteException e) {
            subroleDropdown.addItem("Error loading");
        }
    } else {
        subroleDropdown.addItem("-");
    }

    Object[] message = {
        "First Name:", firstNameField,
        "Last Name:", lastNameField,
        "IC/Passport:", icField,
        "Role (Read-Only):", roleField,
        "Status:", statusDropdown,
        "Subrole:", subroleDropdown
    };


    int option = JOptionPane.showConfirmDialog(this, message, "Edit User", JOptionPane.OK_CANCEL_OPTION);
    if (option == JOptionPane.OK_OPTION) {
        try {
            boolean profileUpdated = service.updateUserProfile(
                    username,
                    null, // No password update
                    firstNameField.getText(),
                    lastNameField.getText(),
                    icField.getText()
            );

            boolean statusUpdated = service.updateUserStatus(
                    username,
                    (String) statusDropdown.getSelectedItem()
            );
            
            boolean subroleUpdated = true;
                if ("Employee".equalsIgnoreCase(role)) {
                    String selectedSubrole = (String) subroleDropdown.getSelectedItem();
                    if (selectedSubrole != null) {
                        subroleUpdated = service.updateSubroleForUser(username, selectedSubrole.trim());
                    }
                }


            if (profileUpdated || statusUpdated || subroleUpdated) {
                JOptionPane.showMessageDialog(this, "User updated.");
                tableModel.setRowCount(0); // refresh table
                loadUserData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update user.");
            }
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    


}



    private JTable userTable;
    private DefaultTableModel tableModel;
}
