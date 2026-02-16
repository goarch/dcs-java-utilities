package htmlmodifiers;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility: DCS Website Index Editor
 * Environment: Java 8 / Eclipse
 */
public class dcsIndexEditor extends JFrame {
    
    private final String BASE_PATH = "C:\\git\\ages-alwb-templates\\net.ages.liturgical.workbench.templates\\src-gen\\website";
    
    private JComboBox<String> siteCombo;
    private JComboBox<String> yearCombo, monthCombo, dayCombo;
    private JLabel statusLabel = new JLabel("Status: Ready.");
    private JPanel editorPanel;
    private File currentFile;
    private List<ServiceRow> serviceRows = new ArrayList<>();

    public dcsIndexEditor() {
        setTitle("DCS Website Index Editor");
        setSize(850, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        String[] detectedSites = discoverWebsites();
        siteCombo = new JComboBox<>(detectedSites);
        
        for (int i = 0; i < detectedSites.length; i++) {
            if (detectedSites[i].equalsIgnoreCase("goa")) {
                siteCombo.setSelectedIndex(i);
                break;
            }
        }

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        String[] years = new String[11]; 
        for (int i = 0; i < years.length; i++) {
            years[i] = String.valueOf((currentYear - 1) + i);
        }

        String[] months = {"01","02","03","04","05","06","07","08","09","10","11","12"};
        String[] days = new String[31];
        for(int i=0; i<31; i++) days[i] = String.format("%02d", i+1);

        yearCombo = new JComboBox<>(years);
        yearCombo.setSelectedItem(String.valueOf(currentYear));
        monthCombo = new JComboBox<>(months);
        dayCombo = new JComboBox<>(days);

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p1.add(new JLabel("Website: "));
        p1.add(siteCombo);

        JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p2.add(new JLabel("Date: "));
        p2.add(yearCombo); p2.add(new JLabel("/"));
        p2.add(monthCombo); p2.add(new JLabel("/"));
        p2.add(dayCombo);
        
        JButton loadBtn = new JButton("Load File");
        JButton saveBtn = new JButton("Save All");
        saveBtn.setBackground(new Color(180, 220, 180));
        
        // NEW: View in Browser button
        JButton viewBtn = new JButton("View in Browser");
        
        p2.add(loadBtn);
        p2.add(saveBtn);
        p2.add(viewBtn);

        JPanel p3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusLabel.setForeground(Color.BLUE);
        statusLabel.setFont(new Font("Monospaced", Font.PLAIN, 11));
        p3.add(statusLabel);

        header.add(p1);
        header.add(p2);
        header.add(p3);
        add(header, BorderLayout.NORTH);

        editorPanel = new JPanel();
        editorPanel.setLayout(new BoxLayout(editorPanel, BoxLayout.Y_AXIS));
        add(new JScrollPane(editorPanel), BorderLayout.CENTER);

        loadBtn.addActionListener(e -> loadFile());
        saveBtn.addActionListener(e -> saveChanges());
        viewBtn.addActionListener(e -> openInBrowser());

        setLocationRelativeTo(null);
    }

    private String[] discoverWebsites() {
        File root = new File(BASE_PATH);
        List<String> siteList = new ArrayList<>();
        File[] files = root.listFiles();
        if (files != null) {
            for (File f : files) if (f.isDirectory()) siteList.add(f.getName());
        }
        if (siteList.isEmpty()) return new String[] {"[No websites found]"};
        Collections.sort(siteList);
        return siteList.toArray(new String[0]);
    }

    private void loadFile() {
        String selectedSite = siteCombo.getSelectedItem().toString();
        String fileName = yearCombo.getSelectedItem().toString() + 
                          monthCombo.getSelectedItem().toString() + 
                          dayCombo.getSelectedItem().toString() + ".html";
        Path fullPath = Paths.get(BASE_PATH, selectedSite, "dcs", "indexes", fileName);
        File file = fullPath.toFile();

        if (!file.exists()) {
            statusLabel.setText("Status: ERROR - " + fileName + " missing.");
            statusLabel.setForeground(Color.RED);
            JOptionPane.showMessageDialog(this, "File not found at:\n" + file.getAbsolutePath());
            return;
        }

        currentFile = file;
        statusLabel.setText("FILE OPEN: " + file.getAbsolutePath());
        statusLabel.setForeground(new Color(0, 100, 0)); 
        refreshEditor();
    }

    private void refreshEditor() {
        editorPanel.removeAll();
        serviceRows.clear();
        try {
            byte[] encoded = Files.readAllBytes(currentFile.toPath());
            String content = new String(encoded, StandardCharsets.UTF_8);
            Pattern p = Pattern.compile("<span class='index-service-day'>(.*?)</span>");
            Matcher m = p.matcher(content);
            while (m.find()) {
                ServiceRow row = new ServiceRow(m.group(1));
                serviceRows.add(row);
                editorPanel.add(row);
            }
            editorPanel.revalidate();
            editorPanel.repaint();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Read Error: " + ex.getMessage());
        }
    }

    private void saveChanges() {
        if (currentFile == null) return;
        try {
            String content = new String(Files.readAllBytes(currentFile.toPath()), StandardCharsets.UTF_8);
            for (ServiceRow row : serviceRows) {
                String oldText = Pattern.quote(row.originalText);
                String newText = Matcher.quoteReplacement(row.editField.getText());
                content = content.replaceFirst("(<span class='index-service-day'>)" + oldText + "(</span>)", "$1" + newText + "$2");
            }
            Files.write(currentFile.toPath(), content.getBytes(StandardCharsets.UTF_8));
            JOptionPane.showMessageDialog(this, "Update Successful.");
            refreshEditor(); 
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Save Failed: " + ex.getMessage());
        }
    }

    private void openInBrowser() {
        if (currentFile == null || !currentFile.exists()) {
            JOptionPane.showMessageDialog(this, "No file loaded to view.");
            return;
        }
        try {
            Desktop.getDesktop().browse(currentFile.toURI());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not open browser: " + ex.getMessage());
        }
    }

    class ServiceRow extends JPanel {
        String originalText;
        JTextField editField;
        ServiceRow(String text) {
            this.originalText = text;
            this.editField = new JTextField(text, 40);
            add(new JLabel("Service:"));
            add(editField);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new dcsIndexEditor().setVisible(true));
    }
}