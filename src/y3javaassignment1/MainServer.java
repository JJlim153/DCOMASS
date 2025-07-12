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
import java.rmi.Naming;

public class MainServer {
    public static void main(String[] args) {
        try {
            System.setProperty("java.rmi.server.hostname", "127.0.0.1");
            PayrollServiceImpl obj = new PayrollServiceImpl();
            Naming.rebind("PayrollService", obj);
            System.out.println("PayrollService is running...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

