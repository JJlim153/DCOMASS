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
    private JComboBox<String> userFilter, monthFilter, yearFilter, sortFilter, sortOrder, viewMode;
    private JTable table;
    private DefaultTableModel model;
    private JLabel totalBaseLabel, totalBonusLabel, totalEPFLabel, totalSOCSOLabel, totalNetLabel;

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
        viewMode = new JComboBox<>(new String[]{"Detailed View", "Summary View"});

        topPanel.add(new JLabel("Filter by User:"));
        topPanel.add(userFilter);
        topPanel.add(new JLabel("Month:"));
        topPanel.add(monthFilter);
        topPanel.add(new JLabel("Year:"));
        topPanel.add(yearFilter);
        topPanel.add(new JLabel("Sort by:"));
        topPanel.add(sortFilter);
        topPanel.add(sortOrder);
        topPanel.add(new JLabel("View Mode:"));
        topPanel.add(viewMode);

        JButton filterBtn = new JButton("Apply Filter");
        filterBtn.addActionListener(e -> loadFilteredReport());
        topPanel.add(filterBtn);

        add(topPanel, BorderLayout.NORTH);

        // Table setup
        model = new DefaultTableModel();
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Summary Panel
        JPanel summaryPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        totalBaseLabel = new JLabel("Total Base: RM 0.00");
        totalBonusLabel = new JLabel("Total Bonus: RM 0.00");
        totalEPFLabel = new JLabel("Total EPF: RM 0.00");
        totalSOCSOLabel = new JLabel("Total SOCSO: RM 0.00");
        totalNetLabel = new JLabel("Total Net: RM 0.00");

        summaryPanel.add(totalBaseLabel);
        summaryPanel.add(totalBonusLabel);
        summaryPanel.add(totalEPFLabel);
        summaryPanel.add(totalSOCSOLabel);
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

            String mode = viewMode.getSelectedItem().toString();
            if (mode.equals("Summary View")) {
                loadSummaryReport(filtered);
                return;
            }

            // Detailed View
            model.setRowCount(0);
            model.setColumnIdentifiers(new String[]{"Username", "Date", "Base", "Bonus", "EPF", "SOCSO", "Net"});

            String sortBy = sortFilter.getSelectedItem().toString();
            boolean ascending = sortOrder.getSelectedItem().equals("Ascending");

            Comparator<PayrollRecord> comparator = null;
            switch (sortBy) {
                case "Net Pay":
                    comparator = Comparator.comparingDouble(pr -> pr.getBaseSalary() + pr.getBonus() - pr.getEpf() - pr.getSocso());
                    break;
                case "Base Salary":
                    comparator = Comparator.comparingDouble(PayrollRecord::getBaseSalary);
                    break;
                case "Date":
                    comparator = Comparator.comparing(PayrollRecord::getPayDate);
                    break;
            }

            if (comparator != null) {
                if (!ascending) comparator = comparator.reversed();
                filtered.sort(comparator);
            }

            double totalBase = 0, totalBonus = 0, totalEPF = 0, totalSOCSO = 0, totalNet = 0;

            for (PayrollRecord pr : filtered) {
                double net = pr.getBaseSalary() + pr.getBonus() - pr.getEpf() - pr.getSocso();

                model.addRow(new Object[]{
                        pr.getUsername(),
                        pr.getPayDate().toString(),
                        pr.getBaseSalary(),
                        pr.getBonus(),
                        pr.getEpf(),
                        pr.getSocso(),
                        String.format("%.2f", net)
                });

                totalBase += pr.getBaseSalary();
                totalBonus += pr.getBonus();
                totalEPF += pr.getEpf();
                totalSOCSO += pr.getSocso();
                totalNet += net;
            }

            totalBaseLabel.setText(String.format("Total Base: RM %.2f", totalBase));
            totalBonusLabel.setText(String.format("Total Bonus: RM %.2f", totalBonus));
            totalEPFLabel.setText(String.format("Total EPF: RM %.2f", totalEPF));
            totalSOCSOLabel.setText(String.format("Total SOCSO: RM %.2f", totalSOCSO));
            totalNetLabel.setText(String.format("Total Net: RM %.2f", totalNet));

        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this, "Failed to load report: " + e.getMessage());
        }
    }

    private void loadSummaryReport(List<PayrollRecord> records) {
        model.setRowCount(0);
        model.setColumnIdentifiers(new String[]{"Period", "Total Base", "Total Bonus", "Total EPF", "Total SOCSO", "Total Net"});

        Map<String, double[]> totalsMap = new TreeMap<>();
        Calendar cal = Calendar.getInstance();

        for (PayrollRecord pr : records) {
            cal.setTime(pr.getPayDate());
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH) + 1;

            String key = year + "-" + String.format("%02d", month);

            double[] totals = totalsMap.getOrDefault(key, new double[6]);
            totals[0] += pr.getBaseSalary();
            totals[1] += pr.getBonus();
            totals[2] += pr.getEpf();
            totals[3] += pr.getSocso();
            totals[4] += pr.getBaseSalary() + pr.getBonus() - pr.getEpf() - pr.getSocso();
            totalsMap.put(key, totals);
        }

        double totalBase = 0, totalBonus = 0, totalEPF = 0, totalSOCSO = 0, totalNet = 0;

        for (Map.Entry<String, double[]> entry : totalsMap.entrySet()) {
            double[] vals = entry.getValue();
            model.addRow(new Object[]{
                    entry.getKey(),
                    String.format("RM %.2f", vals[0]),
                    String.format("RM %.2f", vals[1]),
                    String.format("RM %.2f", vals[2]),
                    String.format("RM %.2f", vals[3]),
                    String.format("RM %.2f", vals[4])
            });

            totalBase += vals[0];
            totalBonus += vals[1];
            totalEPF += vals[2];
            totalSOCSO += vals[3];
            totalNet += vals[4];
        }

        totalBaseLabel.setText(String.format("Total Base: RM %.2f", totalBase));
        totalBonusLabel.setText(String.format("Total Bonus: RM %.2f", totalBonus));
        totalEPFLabel.setText(String.format("Total EPF: RM %.2f", totalEPF));
        totalSOCSOLabel.setText(String.format("Total SOCSO: RM %.2f", totalSOCSO));
        totalNetLabel.setText(String.format("Total Net: RM %.2f", totalNet));
    }
}
