package y3javaassignment1;

import java.io.Serializable;

public class PayrollSummary implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private double baseSalary;
    private double bonus;
    private double epf;
    private double tax;

    public PayrollSummary(String username, double baseSalary, double bonus, double epf, double tax) {
        this.username = username;
        this.baseSalary = baseSalary;
        this.bonus = bonus;
        this.epf = epf;
        this.tax = tax;
    }

    public String getUsername() {
        return username;
    }

    public double getBaseSalary() {
        return baseSalary;
    }

    public double getBonus() {
        return bonus;
    }

    public double getEpf() {
        return epf;
    }

    public double getTax() {
        return tax;
    }
}
