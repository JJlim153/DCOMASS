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

        String[] columnNames = {"Username","Password", "Role", "First Name", "Last Name", "IC/Passport", "Status"};
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
                if (user.length >= 7) {  // Ensure array is complete
                    tableModel.addRow(new Object[]{
                        user[0], // username
                        user[1], // password
                        user[5], // role
                        user[2], // firstname
                        user[3], // lastname
                        user[4], // ic_passport
                        user[6]  // status
                    });
                } else {
                    System.err.println("Incomplete user record: " + java.util.Arrays.toString(user));
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
        String role = (String) tableModel.getValueAt(row, 1);
        String firstName = (String) tableModel.getValueAt(row, 2);
        String lastName = (String) tableModel.getValueAt(row, 3);
        String ic = (String) tableModel.getValueAt(row, 4);

        JTextField firstNameField = new JTextField(firstName);
        JTextField lastNameField = new JTextField(lastName);
        JTextField icField = new JTextField(ic);
        JTextField roleField = new JTextField(role);
        JTextField passwordField = new JPasswordField();

        Object[] message = {
            "First Name:", firstNameField,
            "Last Name:", lastNameField,
            "IC/Passport:", icField,
            "Role:", roleField,
            "New Password (optional):", passwordField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Edit User", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                boolean success = service.updateUserProfile(
                        username,
                        passwordField.getText().isEmpty() ? null : passwordField.getText(),
                        firstNameField.getText(),
                        lastNameField.getText(),
                        icField.getText()
                );
                if (success) {
                    JOptionPane.showMessageDialog(this, "User updated.");
                    tableModel.setRowCount(0); // refresh
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
