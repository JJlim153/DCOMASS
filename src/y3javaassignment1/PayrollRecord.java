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
import java.sql.Date;

public class PayrollRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    
    private String username;
    private Date payDate;
    private double baseSalary;
    private double bonus;
    private double epf;
    private double socso;
    private double netPay;

    public PayrollRecord(String username, Date payDate, double baseSalary, double bonus, double epf, double socso, double netPay) {
        this.username = username;
        this.payDate = payDate;
        this.baseSalary = baseSalary;
        this.bonus = bonus;
        this.epf = epf;
        this.socso = socso;
        this.netPay = netPay;
    }

    public String getUsername() { return username; }
    public Date getPayDate() { return payDate; }
    public double getBaseSalary() { return baseSalary; }
    public double getBonus() { return bonus; }
    public double getEpf() { return epf; }
    public double getSocso() { return socso; }
    public double getNetPay() { return netPay; }
    

    
}
