/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package y3javaassignment1;

/**
 *
 * @author Daniellim
 */
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;

public class PayrollServiceImpl extends UnicastRemoteObject implements PayrollService {


    public PayrollServiceImpl() throws RemoteException {
        try {
            // Load Derby driver
            Class.forName("org.apache.derby.jdbc.ClientDriver");
            // Connect to your database
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean registerUser(
        String username,
        String password,
        String role,
        String firstName,
        String lastName,
        String icPassport
    ) throws RemoteException {
        try (
            Connection conn = DriverManager.getConnection(
                "jdbc:derby://localhost:1527/PayrollAssignment",
                "group18",
                "group18"
            )
        ) {
            conn.setAutoCommit(true);

            // Check if username exists
            PreparedStatement check = conn.prepareStatement(
                "SELECT * FROM USERS WHERE USERNAME = ?"
            );
            check.setString(1, username);
            ResultSet rs = check.executeQuery();
            if (rs.next()) {
                return false; // Username exists
            }

            // Insert into USERS
            PreparedStatement insertUser = conn.prepareStatement(
                "INSERT INTO USERS (USERNAME, PASSWORD, ROLE, STATUS) VALUES (?, ?, ?, ?)"
            );
            insertUser.setString(1, username);
            insertUser.setString(2, password);
            insertUser.setString(3, role);
            insertUser.setString(4, "Pending");
            insertUser.executeUpdate();

            // Insert into USERINFO
            PreparedStatement insertInfo = conn.prepareStatement(
                "INSERT INTO USERINFO (USERNAME, FIRSTNAME, LASTNAME, ICPASSPORT) VALUES (?, ?, ?, ?)"
            );
            insertInfo.setString(1, username);
            insertInfo.setString(2, firstName);
            insertInfo.setString(3, lastName);
            insertInfo.setString(4, icPassport);
            insertInfo.executeUpdate();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    
    public LoginResult loginUser(String username, String password) throws RemoteException {
        try (
            Connection conn = DriverManager.getConnection(
                "jdbc:derby://localhost:1527/PayrollAssignment",
                "group18",
                "group18"
            )
        ) {
            conn.setAutoCommit(true);

            PreparedStatement ps = conn.prepareStatement(
                "SELECT ROLE, STATUS FROM USERS WHERE USERNAME = ? AND PASSWORD = ?"
            );
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String status = rs.getString("STATUS").trim();
                String role = rs.getString("ROLE").trim();
                return new LoginResult(status, role);
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Error during login: " + e.getMessage());
        }
    }


    
    public List<String[]> getAllUsers() throws RemoteException {
        List<String[]> users = new ArrayList<>();
        try (
            Connection conn = DriverManager.getConnection(
                "jdbc:derby://localhost:1527/PayrollAssignment",
                "group18",
                "group18"
            )
        ) {
            conn.setAutoCommit(true);

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT USERNAME, ROLE, STATUS FROM USERS");
            while (rs.next()) {
                String username = rs.getString("USERNAME");
                String role = rs.getString("ROLE");
                String status = rs.getString("STATUS");
                users.add(new String[]{username, role, status});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Error fetching users: " + e.getMessage());
        }
        return users;
    }



    
    public boolean updateUserStatus(String username, String status) throws RemoteException {
        try (
            Connection conn = DriverManager.getConnection(
                "jdbc:derby://localhost:1527/PayrollAssignment",
                "group18",
                "group18"
            )
        ) {
            conn.setAutoCommit(true);

            PreparedStatement ps = conn.prepareStatement(
                "UPDATE USERS SET STATUS = ? WHERE USERNAME = ?"
            );
            ps.setString(1, status);
            ps.setString(2, username);
            int updated = ps.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Error updating user status: " + e.getMessage());
        }
    }


    public String[] getUserProfile(String username) throws RemoteException {
        try (
            Connection conn = DriverManager.getConnection("jdbc:derby://localhost:1527/PayrollAssignment", "group18", "group18")
        ) {
            String[] data = new String[4];
            // Load password
            PreparedStatement ps1 = conn.prepareStatement("SELECT PASSWORD FROM USERS WHERE USERNAME = ?");
            ps1.setString(1, username);
            ResultSet rs1 = ps1.executeQuery();
            if (rs1.next()) {
                data[0] = rs1.getString("PASSWORD");
            }
            // Load personal info
            PreparedStatement ps2 = conn.prepareStatement("SELECT FIRSTNAME, LASTNAME, ICPASSPORT FROM USERINFO WHERE USERNAME = ?");
            ps2.setString(1, username);
            ResultSet rs2 = ps2.executeQuery();
            if (rs2.next()) {
                data[1] = rs2.getString("FIRSTNAME");
                data[2] = rs2.getString("LASTNAME");
                data[3] = rs2.getString("ICPASSPORT");
            }
            return data;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Error loading profile: " + e.getMessage());
        }
    }


    public boolean updateUserProfile(String username, String password, String firstName, String lastName, String icPassport) throws RemoteException {
        try (
            Connection conn = DriverManager.getConnection("jdbc:derby://localhost:1527/PayrollAssignment", "group18", "group18")
        ) {
            conn.setAutoCommit(false);
            // Update password
            PreparedStatement ps1 = conn.prepareStatement("UPDATE USERS SET PASSWORD = ? WHERE USERNAME = ?");
            ps1.setString(1, password);
            ps1.setString(2, username);
            ps1.executeUpdate();
            // Update personal info
            PreparedStatement ps2 = conn.prepareStatement("UPDATE USERINFO SET FIRSTNAME = ?, LASTNAME = ?, ICPASSPORT = ? WHERE USERNAME = ?");
            ps2.setString(1, firstName);
            ps2.setString(2, lastName);
            ps2.setString(3, icPassport);
            ps2.setString(4, username);
            ps2.executeUpdate();
            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Error updating profile: " + e.getMessage());
        }
    }



}

