/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package y3javaassignment1;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.rmi.RemoteException;
import java.util.List;

/**
 *
 * @author Amjad Parker
 */
public class PayrollReportTable extends JFrame {
    private PayrollService service;

    public PayrollReportTable(PayrollService service) {
        this.service = service;
        setTitle("Payroll Report");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 400);
        setLocationRelativeTo(null);

        String[] columnNames = {"Username", "Base Salary", "Bonus", "EPF", "Tax", "Net Pay"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(model);

        try {
            List<PayrollSummary> list = service.generatePayrollReport();
            for (PayrollSummary ps : list) {
                double netPay = ps.getBaseSalary() + ps.getBonus() - ps.getEpf() - ps.getTax();
                model.addRow(new Object[]{
                    ps.getUsername(),
                    ps.getBaseSalary(),
                    ps.getBonus(),
                    ps.getEpf(),
                    ps.getTax(),
                    netPay
                });
            }
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this, "Failed to load payroll data: " + e.getMessage());
        }

        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton backBtn = new JButton("Back");
        backBtn.addActionListener(e -> dispose());

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(backBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }
   
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
