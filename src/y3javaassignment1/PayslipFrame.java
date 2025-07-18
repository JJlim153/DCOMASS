package y3javaassignment1;

import javax.swing.*;
import java.awt.*;

/**
 * @author Amjad Parker
 */
public class PayslipFrame extends JFrame {

    public PayslipFrame(PayrollSummary ps) {
        setTitle("Payslip for " + ps.getUsername());
        setSize(400, 300);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(7, 2, 10, 10));

        double netPay = ps.getBaseSalary() + ps.getBonus() - ps.getEpf() - ps.getTax();

        add(new JLabel("Username:"));
        add(new JLabel(ps.getUsername()));

        add(new JLabel("Base Salary:"));
        add(new JLabel("RM " + ps.getBaseSalary()));

        add(new JLabel("Bonus:"));
        add(new JLabel("RM " + ps.getBonus()));

        add(new JLabel("EPF Deduction:"));
        add(new JLabel("RM " + ps.getEpf()));

        add(new JLabel("Tax Deduction:"));
        add(new JLabel("RM " + ps.getTax()));

        add(new JLabel("Net Pay:"));
        add(new JLabel("RM " + netPay));

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());
        add(new JLabel()); // Empty label for layout alignment
        add(closeBtn);

        setVisible(true);
    }
}