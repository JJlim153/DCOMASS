package y3javaassignment1;

import java.io.Serializable;

public class PayrollSettings implements java.io.Serializable {
    private double epfRate, socsoRate, taxRate;

    public PayrollSettings(double epfRate, double socsoRate, double taxRate) {
        this.epfRate = epfRate;
        this.socsoRate = socsoRate;
        this.taxRate = taxRate;
    }

    public double getEpfRate() { return epfRate; }
    public double getSocsoRate() { return socsoRate; }
    public double getTaxRate() { return taxRate; }

    public void setEpfRate(double rate) { this.epfRate = rate; }
    public void setSocsoRate(double rate) { this.socsoRate = rate; }
    public void setTaxRate(double rate) { this.taxRate = rate; }
}

