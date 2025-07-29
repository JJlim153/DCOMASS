package y3javaassignment1;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.rmi.RemoteException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class PayrollReportTable extends JFrame {
    private PayrollService service;
    private JComboBox<String> userFilter, monthFilter, yearFilter, sortFilter, sortOrder;
    private JTable table;
    private DefaultTableModel model;
    private JLabel totalBaseLabel, totalBonusLabel, totalEPFLabel, totalSOCSOLabel, totalTaxLabel, totalNetLabel;

    public PayrollReportTable(PayrollService service) {
        this.service = service;
        setTitle("Payroll Report");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Filters Panel
        JPanel topPanel = new JPanel(new FlowLayout());

        userFilter = new JComboBox<>();
        monthFilter = new JComboBox<>(new String[]{"All Months", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"});
        yearFilter = new JComboBox<>();
        sortFilter = new JComboBox<>(new String[]{"None", "Net Pay", "Base Salary", "Date"});
        sortOrder = new JComboBox<>(new String[]{"Ascending", "Descending"});

        topPanel.add(new JLabel("Filter by User:"));
        topPanel.add(userFilter);
        topPanel.add(new JLabel("Month:"));
        topPanel.add(monthFilter);
        topPanel.add(new JLabel("Year:"));
        topPanel.add(yearFilter);
        topPanel.add(new JLabel("Sort by:"));
        topPanel.add(sortFilter);
        topPanel.add(sortOrder);

        JButton filterBtn = new JButton("Apply Filter");
        filterBtn.addActionListener(e -> loadFilteredReport());
        topPanel.add(filterBtn);

        add(topPanel, BorderLayout.NORTH);

        // Table setup
        String[] columns = {"Username", "Date", "Base", "Bonus", "EPF", "SOCSO", "TAX", "Net"};
        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Summary Panel
        JPanel summaryPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        totalBaseLabel = new JLabel("Total Base: RM 0.00");
        totalBonusLabel = new JLabel("Total Bonus: RM 0.00");
        totalEPFLabel = new JLabel("Total EPF: RM 0.00");
        totalSOCSOLabel = new JLabel("Total SOCSO: RM 0.00");
        totalTaxLabel = new JLabel("Total Tax: RM 0.00");
        totalNetLabel = new JLabel("Total Net: RM 0.00");

        summaryPanel.add(totalBaseLabel);
        summaryPanel.add(totalBonusLabel);
        summaryPanel.add(totalEPFLabel);
        summaryPanel.add(totalSOCSOLabel);
        summaryPanel.add(totalTaxLabel);
        summaryPanel.add(totalNetLabel);
        add(summaryPanel, BorderLayout.SOUTH);

        // Back Button
        JButton backBtn = new JButton("Back");
        backBtn.addActionListener(e -> dispose());
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(backBtn);
        add(bottomPanel, BorderLayout.PAGE_END);

        populateUserDropdown();
        populateYearDropdown();
        loadFilteredReport();
    }

    private void populateUserDropdown() {
        try {
            userFilter.addItem("All");
            for (String username : service.getApprovedUsernames()) {
                userFilter.addItem(username);
            }
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this, "Failed to load users: " + e.getMessage());
        }
    }

    private void populateYearDropdown() {
        yearFilter.addItem("All");
        try {
            List<PayrollRecord> records = service.getAllPayslips();
            Set<Integer> years = new TreeSet<>();
            Calendar cal = Calendar.getInstance();
            for (PayrollRecord pr : records) {
                cal.setTime(pr.getPayDate());
                years.add(cal.get(Calendar.YEAR));
            }
            for (int year : years) {
                yearFilter.addItem(String.valueOf(year));
            }
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this, "Failed to load years: " + e.getMessage());
        }
    }

    private void loadFilteredReport() {
        try {
            model.setRowCount(0); // Clear table
            List<PayrollRecord> records = service.getAllPayslips();

            Calendar cal = Calendar.getInstance();
            String selectedUser = userFilter.getSelectedItem().toString();
            int selectedMonth = monthFilter.getSelectedIndex() - 1; // 0 = Jan, -1 = All
            String selectedYearStr = yearFilter.getSelectedItem().toString();
            int selectedYear = selectedYearStr.equals("All") ? -1 : Integer.parseInt(selectedYearStr);

            // Filtering
            List<PayrollRecord> filtered = records.stream().filter(pr -> {
                boolean matchUser = selectedUser.equals("All") || pr.getUsername().equals(selectedUser);

                cal.setTime(pr.getPayDate());
                boolean matchMonth = selectedMonth == -1 || cal.get(Calendar.MONTH) == selectedMonth;
                boolean matchYear = selectedYear == -1 || cal.get(Calendar.YEAR) == selectedYear;

                return matchUser && matchMonth && matchYear;
            }).collect(Collectors.toList());

            // Sorting
            String sortBy = sortFilter.getSelectedItem().toString();
            boolean ascending = sortOrder.getSelectedItem().equals("Ascending");

            Comparator<PayrollRecord> comparator = null;

            switch (sortBy) {
                case "Net Pay":
                    comparator = Comparator.comparingDouble(pr ->
                        pr.getBaseSalary() + pr.getBonus() - pr.getEpf() - pr.getSocso() - pr.getTax()
                    );
                    break;
                case "Base Salary":
                    comparator = Comparator.comparingDouble(PayrollRecord::getBaseSalary);
                    break;
                case "Date":
                    comparator = Comparator.comparing(PayrollRecord::getPayDate);
                    break;
                default:
                    comparator = null;
            }

            if (comparator != null) {
            if (!ascending) {
                comparator = comparator.reversed();
            }
            filtered.sort(comparator);
        }


            if (comparator != null) {
                if (!ascending) comparator = comparator.reversed();
                filtered.sort(comparator);
            }

            // Totals
            double totalBase = 0, totalBonus = 0, totalEPF = 0, totalSOCSO = 0, totalTax = 0, totalNet = 0;

            for (PayrollRecord pr : filtered) {
                double net = pr.getBaseSalary() + pr.getBonus() - pr.getEpf() - pr.getSocso() - pr.getTax();

                model.addRow(new Object[]{
                        pr.getUsername(),
                        pr.getPayDate().toString(),
                        pr.getBaseSalary(),
                        pr.getBonus(),
                        pr.getEpf(),
                        pr.getSocso(),
                        pr.getTax(),
                        String.format("%.2f", net)
                });

                totalBase += pr.getBaseSalary();
                totalBonus += pr.getBonus();
                totalEPF += pr.getEpf();
                totalSOCSO += pr.getSocso();
                totalTax += pr.getTax();
                totalNet += net;
            }

            totalBaseLabel.setText(String.format("Total Base: RM %.2f", totalBase));
            totalBonusLabel.setText(String.format("Total Bonus: RM %.2f", totalBonus));
            totalEPFLabel.setText(String.format("Total EPF: RM %.2f", totalEPF));
            totalSOCSOLabel.setText(String.format("Total SOCSO: RM %.2f", totalSOCSO));
            totalTaxLabel.setText(String.format("Total Tax: RM %.2f", totalTax));
            totalNetLabel.setText(String.format("Total Net: RM %.2f", totalNet));

        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this, "Failed to load report: " + e.getMessage());
        }
    }
}
