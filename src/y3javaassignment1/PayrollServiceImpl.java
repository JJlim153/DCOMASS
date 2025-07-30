/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package y3javaassignment1;

/**
 *
 * @author Daniellim & Parker
 */

import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;

public class PayrollServiceImpl extends UnicastRemoteObject implements PayrollService {


    public PayrollServiceImpl() throws RemoteException {

        try {
            Class.forName("org.apache.derby.jdbc.ClientDriver");
            Connection conn = DriverManager.getConnection(
                "jdbc:derby://localhost:1527/PayrollAssignment", "group18", "group18"
            );

            System.out.println("Connected to DB successfully.");

            // Optional test query
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM payroll");
            while (rs.next()) {
                System.out.println("Found payroll: " + rs.getString("username"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    @Override
    public boolean registerUser(String username, String password, String role,
                                String firstName, String lastName, String ic) throws RemoteException {
        try (Connection conn = getConnection()) {

            // 🔍 Step 1: Check for existing username first
            PreparedStatement checkStmt = conn.prepareStatement("SELECT * FROM USERS WHERE username = ?");
            checkStmt.setString(1, username);
            ResultSet checkResult = checkStmt.executeQuery();
            if (checkResult.next()) {
                return false; // Username exists
            }

            // ✅ Step 2: Count total users (now we know this is a new username)
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM USERS");
            rs.next();
            int userCount = rs.getInt(1);

            // ✅ Step 3: Decide status
            String status = "Pending";

            // Check if any approved Admin exists
            PreparedStatement adminCheck = conn.prepareStatement(
                "SELECT * FROM USERS WHERE LOWER(role) = 'admin' AND LOWER(status) = 'approved'"
            );
            
            ResultSet adminResult = adminCheck.executeQuery();
            boolean adminExists = adminResult.next();
            System.out.println("Role: " + role + ", Admin exists: " + adminExists);

            if (!adminExists && role.equalsIgnoreCase("Admin")) {
                status = "Approved"; // First approved admin
            }

            // ✅ Step 4: Insert
            PreparedStatement insertStmt = conn.prepareStatement(
                "INSERT INTO USERS (username, password, role, firstName, lastName, ic_passport, status) VALUES (?, ?, ?, ?, ?, ?, ?)"
            );
            insertStmt.setString(1, username);
            insertStmt.setString(2, password);
            insertStmt.setString(3, role);
            insertStmt.setString(4, firstName);
            insertStmt.setString(5, lastName);
            insertStmt.setString(6, ic);
            insertStmt.setString(7, status);

            return insertStmt.executeUpdate() > 0;

        } catch (Exception e) {
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

    @Override
    public List<PayrollRecord> getAllPayslips() throws RemoteException {
        List<PayrollRecord> list = new ArrayList<>();
        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM PAYROLL ORDER BY PAY_DATE DESC");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new PayrollRecord(
                    rs.getString("USERNAME"),                    
                    rs.getDate("PAY_DATE"),
                    rs.getDouble("BASE_SALARY"),
                    rs.getDouble("BONUS"),
                    rs.getDouble("EPF"),
                    rs.getDouble("SOCSO"),
                    rs.getDouble("ANNUALINCOME")
                ));
            }
        } catch (SQLException e) {
            throw new RemoteException("Error fetching all payslips: " + e.getMessage());
        }
        return list;
    }

    
    @Override
    public List<String[]> getAllUsers() throws RemoteException {
        List<String[]> users = new ArrayList<>();
        try (Connection conn = getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT USERNAME, PASSWORD, FIRSTNAME, LASTNAME, IC_PASSPORT, ROLE, STATUS FROM USERS"
            );
            while (rs.next()) {
                String username = rs.getString("USERNAME");
                String password = rs.getString("PASSWORD");
                String firstName = rs.getString("FIRSTNAME");
                String lastName = rs.getString("LASTNAME");
                String ic = rs.getString("IC_PASSPORT");
                String role = rs.getString("ROLE");
                String status = rs.getString("STATUS");

                users.add(new String[]{username, password, firstName, lastName, ic, role, status});
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


    @Override
    public String[] getUserProfile(String username) throws RemoteException {
        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT PASSWORD, FIRSTNAME, LASTNAME, IC_PASSPORT FROM USERS WHERE USERNAME = ?"
            );
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new String[] {
                    rs.getString("PASSWORD"),
                    rs.getString("FIRSTNAME"),
                    rs.getString("LASTNAME"),
                    rs.getString("IC_PASSPORT")
                };
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Error loading user profile: " + e.getMessage());
        }
        return null;
    }



    @Override
    public boolean updateUserProfile(String username, String password, String firstName, String lastName, String icPassport) throws RemoteException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            if (password != null && !password.trim().isEmpty()) {
                PreparedStatement ps = conn.prepareStatement("UPDATE USERS SET PASSWORD = ? WHERE USERNAME = ?");
                ps.setString(1, password);
                ps.setString(2, username);
                ps.executeUpdate();
            }

            PreparedStatement ps2 = conn.prepareStatement("UPDATE USERS SET FIRSTNAME = ?, LASTNAME = ?, IC_PASSPORT = ? WHERE USERNAME = ?");
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

    
    
         
    
    
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            "jdbc:derby://localhost:1527/PayrollAssignment",
            "group18",
            "group18"
        );
    }

    @Override
    public List<PayrollSummary> generatePayrollReport() throws RemoteException {
        List<PayrollSummary> list = new ArrayList<>();

        try (Connection conn = getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM payroll");

            while (rs.next()) {
                String username = rs.getString("username");
                double base = rs.getDouble("base_salary");
                double bonus = rs.getDouble("bonus");
                double epf = rs.getDouble("epf");
                double tax = rs.getDouble("tax");

                list.add(new PayrollSummary(username, base, bonus, epf, tax));
            }

            return list;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Database error: " + e.getMessage());
        }
    }

    @Override
    public PayrollSummary getPayslip(String username) throws RemoteException {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM payroll WHERE username = ?");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                double base = rs.getDouble("base_salary");
                double bonus = rs.getDouble("bonus");
                double epf = rs.getDouble("epf");
                double tax = rs.getDouble("tax");
                return new PayrollSummary(username, base, bonus, epf, tax);
            } else {
                throw new RemoteException("No payroll record found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Database error: " + e.getMessage());
        }
    }
    
    @Override
    public List<String> getApprovedUsernames() throws RemoteException {
        List<String> usernames = new ArrayList<>();
        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT USERNAME FROM USERS WHERE STATUS = 'Approved'");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                usernames.add(rs.getString("USERNAME"));
            }
        } catch (SQLException e) {
            throw new RemoteException("Error fetching usernames: " + e.getMessage());
        }
        return usernames;
    }

    @Override
    public PayrollSummary getLatestPayrollForUser(String username) throws RemoteException {
        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT BASE_SALARY, BONUS, EPF, SOCSO FROM PAYROLL WHERE USERNAME = ? ORDER BY PAY_DATE DESC FETCH FIRST ROW ONLY"
            );
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new PayrollSummary(
                    username,
                    rs.getDouble("BASE_SALARY"),
                    rs.getDouble("BONUS"),
                    rs.getDouble("EPF"),
                    rs.getDouble("SOCSO")
                );
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new RemoteException("Error fetching payroll: " + e.getMessage());
        }
    }

@Override
public boolean insertPayslip(String username, java.sql.Date payDate, double base, double bonus,double epf, double socso, double tax, double netpay) throws RemoteException {
    try (Connection conn = getConnection()) {
        String sql = "INSERT INTO PAYROLL (USERNAME, PAY_DATE, BASE_SALARY, BONUS, EPF, SOCSO, TAX, ANNUALINCOME) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, username);
        ps.setDate(2, payDate);
        ps.setDouble(3, base);
        ps.setDouble(4, bonus);
        ps.setDouble(5, epf);    // ✅ from frontend
        ps.setDouble(6, socso);  // ✅ from frontend
        ps.setDouble(7, tax);    // ✅ from frontend
        ps.setDouble(8, netpay);

        return ps.executeUpdate() > 0;
    } catch (SQLException e) {
        e.printStackTrace();
        throw new RemoteException("Error inserting payslip: " + e.getMessage());
    }
}




    @Override
    public List<PayrollRecord> getPayslipsForUser(String username) throws RemoteException {
    List<PayrollRecord> list = new ArrayList<>();
    try (Connection conn = getConnection()) {
        PreparedStatement ps = conn.prepareStatement(
            "SELECT * FROM PAYROLL WHERE USERNAME = ? ORDER BY PAY_DATE DESC"
        );
        ps.setString(1, username);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            list.add(new PayrollRecord(
                rs.getString("USERNAME"),                    
                rs.getDate("PAY_DATE"),
                rs.getDouble("BASE_SALARY"),
                rs.getDouble("BONUS"),
                rs.getDouble("EPF"),
                rs.getDouble("SOCSO"),
                rs.getDouble("ANNUALINCOME")
            ));
        }
    } catch (SQLException e) {
        throw new RemoteException("Error loading payslips: " + e.getMessage());
    }
        return list;
    }
    
    @Override
    public PayrollSettings getPayrollSettings() throws RemoteException {
        try (Connection conn = getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT EPF_RATE, SOCSO_RATE, TAX_RATE FROM PAYROLLSETTINGS FETCH FIRST ROW ONLY");
            if (rs.next()) {
                return new PayrollSettings(
                    rs.getDouble("EPF_RATE"),
                    rs.getDouble("SOCSO_RATE"),
                    rs.getDouble("TAX_RATE")
                );
            } else {
                // Default fallback if no config found
                return new PayrollSettings(0.11, 0.005, 0.05);
            }
        } catch (SQLException e) {
            throw new RemoteException("Error fetching settings: " + e.getMessage());
        }
    }
    
    @Override
    public boolean updatePayrollSettings(double epfRate, double socsoRate, double taxRate) throws RemoteException {
        try (Connection conn = getConnection()) {
            // Simple logic: truncate and insert new
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("DELETE FROM PAYROLLSETTINGS");

            PreparedStatement ps = conn.prepareStatement("INSERT INTO PAYROLLSETTINGS (EPF_RATE, SOCSO_RATE, TAX_RATE) VALUES (?, ?, ?)");
            ps.setDouble(1, epfRate);
            ps.setDouble(2, socsoRate);
            ps.setDouble(3, taxRate);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RemoteException("Error updating settings: " + e.getMessage());
        }
    }


    
    @Override
    public boolean hasAnyUsers() throws RemoteException {
        try (Connection conn = getConnection()) {
            String sql = "SELECT COUNT(*) FROM USERS";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Generate Payroll
    
    @Override
    public List<String> getDistinctSubroles() throws RemoteException {
        List<String> subroles = new ArrayList<>();
        try (Connection conn = getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT DISTINCT SUBROLE FROM EMPLOYEESUBROLES");
            while (rs.next()) {
                subroles.add(rs.getString("SUBROLE"));
            }
        } catch (SQLException e) {
            throw new RemoteException("Error fetching subroles: " + e.getMessage());
        }
        return subroles;
    }

    @Override
    public List<String> getUsernamesBySubrole(String subrole) throws RemoteException {
        List<String> usernames = new ArrayList<>();
        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT USERNAME FROM EMPLOYEESUBROLES WHERE SUBROLE = ?");
            ps.setString(1, subrole);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                usernames.add(rs.getString("USERNAME"));
            }
        } catch (SQLException e) {
            throw new RemoteException("Error fetching usernames by subrole: " + e.getMessage());
        }
        return usernames;
    }
    
    @Override
    public boolean insertPayslip(String username, Date payDate, double base, double bonus) throws RemoteException {
        try (Connection conn = getConnection()) {
            PayrollSettings settings = getPayrollSettings();

            double epf = base * settings.getEpfRate();
            double socso = base * settings.getSocsoRate();
            double tax = base * settings.getTaxRate();
            double annualIncome = (base + bonus) - epf - socso - tax;

            String sql = "INSERT INTO PAYROLL (USERNAME, PAY_DATE, BASE_SALARY, BONUS, EPF, SOCSO, TAX, ANNUALINCOME) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setDate(2, payDate);
            ps.setDouble(3, base);
            ps.setDouble(4, bonus);
            ps.setDouble(5, epf);
            ps.setDouble(6, socso);
            ps.setDouble(7, tax);
            ps.setDouble(8, annualIncome);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Error inserting payslip (group): " + e.getMessage());
        }
    }
    
    @Override
    public String getSubroleForUser(String username) throws RemoteException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT subrole FROM EmployeeSubRoles WHERE username = ?")) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getString("subrole") : null;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Error fetching subrole", e);
        }
    }

    @Override
    public boolean updateSubroleForUser(String username, String subrole) throws RemoteException {
        try (Connection conn = getConnection()) {
            PreparedStatement checkStmt = conn.prepareStatement("SELECT username FROM EmployeeSubRoles WHERE username = ?");
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                // update existing
                PreparedStatement updateStmt = conn.prepareStatement("UPDATE EmployeeSubRoles SET subrole = ? WHERE username = ?");
                updateStmt.setString(1, subrole);
                updateStmt.setString(2, username);
                return updateStmt.executeUpdate() > 0;
            } else {
                // insert new
                PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO EmployeeSubRoles (username, role, subrole) VALUES (?, 'Employee', ?)");
                insertStmt.setString(1, username);
                insertStmt.setString(2, subrole);
                return insertStmt.executeUpdate() > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Error updating subrole", e);
        }
    }

    // IC Validation
    
    @Override
    public boolean icPassportExists(String icPassport) throws RemoteException {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM USERS WHERE IC_Passport = ?")) {
            ps.setString(1, icPassport);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


}

