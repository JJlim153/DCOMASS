package y3javaassignment1;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class MainServer {
    public static void main(String[] args) {
        try {
            // Ensure your RMI registry is started inside the same JVM process
            LocateRegistry.createRegistry(1099);

            // Set the hostname to localhost (optional but helps avoid binding issues)
            System.setProperty("java.rmi.server.hostname", "127.0.0.1");

            PayrollService service = new PayrollServiceImpl();

            // Bind the object to the RMI registry
            Naming.rebind("rmi://localhost/PayrollService", service);

            System.out.println("PayrollService is bound and running.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}