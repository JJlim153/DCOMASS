package y3javaassignment1;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface PayrollService extends Remote {
    
    // User Registration
    boolean registerUser(
        String username,
        String password,
        String role,
        String firstName,
        String lastName,
        String icPassport
    ) throws RemoteException;

    // Login
    LoginResult loginUser(String username, String password) throws RemoteException;

    // HR Features
    List<String[]> getAllUsers() throws RemoteException;
    boolean updateUserStatus(String username, String status) throws RemoteException;

    // Profile Management
    String[] getUserProfile(String username) throws RemoteException;
    boolean updateUserProfile(
        String username,
        String password,
        String firstName,
        String lastName,
        String icPassport
    ) throws RemoteException;

    // Payroll Functions
    List<PayrollSummary> generatePayrollReport() throws RemoteException;
    PayrollSummary getPayslip(String username) throws RemoteException;
    
    List<String> getApprovedUsernames() throws RemoteException;
    PayrollSummary getLatestPayrollForUser(String username) throws RemoteException;
    boolean insertPayslip(String username, java.sql.Date payDate, double base, double bonus, double epf, double socso) throws RemoteException;
    List<PayrollRecord> getPayslipsForUser(String username) throws RemoteException;


}
