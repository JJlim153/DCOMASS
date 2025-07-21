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
    private Date payDate;
    private double baseSalary, bonus, epf, socso, annualIncome;

    public PayrollRecord(Date payDate, double baseSalary, double bonus, double epf, double socso, double annualIncome) {
        this.payDate = payDate;
        this.baseSalary = baseSalary;
        this.bonus = bonus;
        this.epf = epf;
        this.socso = socso;
        this.annualIncome = annualIncome;
    }

    public Date getPayDate() { return payDate; }
    public double getBaseSalary() { return baseSalary; }
    public double getBonus() { return bonus; }
    public double getEpf() { return epf; }
    public double getSocso() { return socso; }
    public double getAnnualIncome() { return annualIncome; }
}
