package generationpresets;

import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.UIManager;

/**
 * Utility for JRE 1.8.0_471 to switch projects in .ares configuration files
 * and provide a visual indicator in the Eclipse Project Explorer.
 */
public class SetGenerationClient {

    private static final String TARGET_FILE = "C:/git/ages-alwb-templates/net.ages.liturgical.workbench.templates/c-generator-settings/pref.master.templates.ares"; 
    private static final String PROJECT_ROOT = "C:/git/ages-alwb-templates/net.ages.liturgical.workbench.templates/";

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) { }

        List<String> clientList = new ArrayList<>();
        String currentlyActive = "";

        // 1. Discover clients and current setting from the file
        try {
            Path path = Paths.get(TARGET_FILE);
            if (Files.exists(path)) {
                List<String> lines = Files.readAllLines(path);
                for (String line : lines) {
                    String trimmed = line.trim();
                    if (trimmed.startsWith("// client:")) {
                        clientList.add(trimmed.replace("// client:", "").trim());
                    }
                    if (trimmed.startsWith("selected.pref.main") && !trimmed.startsWith("//")) {
                        int start = trimmed.indexOf("pref.main_") + 10;
                        int end = trimmed.indexOf("\"", start);
                        if (start > 9 && end > start) {
                            currentlyActive = trimmed.substring(start, end);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Collections.sort(clientList);

        // 2. Setup the GUI
        JFrame frame = new JFrame("Set Generation Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(420, 150);
        frame.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 25));
        frame.setLocationRelativeTo(null); 

        JLabel label = new JLabel("Target Client:");
        final JComboBox<String> clientDropdown = new JComboBox<>(clientList.toArray(new String[0]));
        
        if (!currentlyActive.isEmpty()) {
            clientDropdown.setSelectedItem(currentlyActive);
        }

        JButton switchButton = new JButton("Update and Close");

        // The update logic
        Runnable updateAction = () -> {
            String selectedClient = (String) clientDropdown.getSelectedItem();
            try {
                // --- NEW LINE ADDED HERE ---
                ClientContext.setCurrentClient(selectedClient);
                // ---------------------------
                
                updateAresFile(selectedClient);
                updateSidebarIndicator(selectedClient);
                
                // Note: If you want to run SetIndexStatus automatically right now, 
                // you could call SetIndexStatus.executeToggle(selectedClient) here.
                
                System.exit(0); 
            } catch (IOException ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        };

        // UI Listeners
        switchButton.addActionListener(e -> updateAction.run());
        clientDropdown.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    updateAction.run();
                }
            }
        });

        frame.add(label);
        frame.add(clientDropdown);
        frame.add(switchButton);
        frame.getRootPane().setDefaultButton(switchButton);
        frame.setVisible(true);
    }

    private static void updateAresFile(String clientKey) throws IOException {
        Path path = Paths.get(TARGET_FILE);
        List<String> lines = Files.readAllLines(path);
        List<String> updatedLines = lines.stream().map(line -> {
            if (line.trim().startsWith("selected.pref.main") && !line.trim().startsWith("//")) {
                return "\tselected.pref.main = \"pref.main_" + clientKey + "\"";
            }
            return line;
        }).collect(Collectors.toList());
        Files.write(path, updatedLines);
    }

    private static void updateSidebarIndicator(String clientKey) {
        try {
            Path rootPath = Paths.get(PROJECT_ROOT);
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(rootPath, "_CURRENT_CLIENT_*")) {
                for (Path entry : stream) {
                    Files.delete(entry);
                }
            }
            Path flagFile = rootPath.resolve("_CURRENT_CLIENT_" + clientKey.toUpperCase() + ".txt");
            Files.createFile(flagFile);
        } catch (IOException e) {
            System.err.println("Indicator update failed: " + e.getMessage());
        }
    }
}