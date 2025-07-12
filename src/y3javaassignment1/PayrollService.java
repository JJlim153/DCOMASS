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
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface PayrollService extends Remote {
    // Registration
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


    // Update personal details
    boolean updatePersonalDetails(
        String username,
        String firstName,
        String lastName,
        String icPassport
    ) throws RemoteException;
    
    List<String[]> getAllUsers() throws RemoteException;
    
    boolean updateUserStatus(String username, String status) throws RemoteException;
}

