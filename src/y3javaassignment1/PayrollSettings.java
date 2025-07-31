package y3javaassignment1;

import java.io.Serializable;

public class PayrollSettings implements java.io.Serializable {
    private double epfRate, socsoRate;

    public PayrollSettings(double epfRate, double socsoRate) {
        this.epfRate = epfRate;
        this.socsoRate = socsoRate;
    }

    public double getEpfRate() { return epfRate; }
    public double getSocsoRate() { return socsoRate; }

    public void setEpfRate(double rate) { this.epfRate = rate; }
    public void setSocsoRate(double rate) { this.socsoRate = rate; }
}

