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
import java.io.Serializable;

public class LoginResult implements Serializable {
    private String status;
    private String role;

    public LoginResult(String status, String role) {
        this.status = status;
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public String getRole() {
        return role;
    }
}
