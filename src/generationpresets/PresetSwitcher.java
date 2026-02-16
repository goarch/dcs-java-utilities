package generationpresets;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Stream;

public class PresetSwitcher {

    // --- CONFIGURATION PATHS ---
    private static final String TARGET_FILE = "C:/git/ages-alwb-templates/net.ages.liturgical.workbench.templates/c-generator-settings/pref.generation_alwb.ares"; 
    private static final String ATEM_DIRECTORY = "C:/git/ages-alwb-templates/net.ages.liturgical.workbench.templates/a-templates/Pdf_Covers"; 

    public static void main(String[] args) {
        if (args.length == 0) return;

        String mode = args[0].toUpperCase();
        Map<String, String> settings = new HashMap<>();

        if (mode.startsWith("HTML")) {
            settings.put("generate.file.html", "yes");
            settings.put("generate.file.pdf", "no");
            applyHtmlLogic(mode, settings);
        } 
        else if (mode.startsWith("PDF")) {
            settings.put("generate.file.html", "no");
            settings.put("generate.file.pdf", "yes");
            applyPdfLogic(mode, settings);
            
            // Start the .atem file deep scan
            System.out.println("Scanning .atem files in: " + ATEM_DIRECTORY);
            int modifiedCount = handleAtemUpdates(mode);
            System.out.println(">>> ATEM Update: " + modifiedCount + " files updated.");
        }

        try {
            updateSettings(settings);
            System.out.println("----------------------------------------------");
            System.out.println("SUCCESS: Mode [" + mode + "] applied.");
            System.out.println("----------------------------------------------");
        } catch (IOException e) {
            System.err.println("CRITICAL ERROR: Could not write to .ares file.");
            e.printStackTrace();
        }
    }

    private static int handleAtemUpdates(String mode) {
        final String searchPattern;
        final String targetLine;

        // Determine which specific line we are flipping
        if (mode.equals("PDF_E") || mode.equals("PDF_GE")) {
            searchPattern = "Switch-Version L1 End-Switch-Version";
            targetLine = "Switch-Version L2 End-Switch-Version";
        } else {
            // PDF_G mode
            searchPattern = "Switch-Version L2 End-Switch-Version";
            targetLine = "Switch-Version L1 End-Switch-Version";
        }

        final int[] count = {0};
        try (Stream<Path> paths = Files.walk(Paths.get(ATEM_DIRECTORY))) {
            paths.filter(Files::isRegularFile)
                 .filter(p -> p.toString().endsWith(".atem"))
                 .forEach(path -> {
                     if (updateSingleAtem(path, searchPattern, targetLine)) {
                         count[0]++;
                         System.out.println("   [Flipped] " + Paths.get(ATEM_DIRECTORY).relativize(path));
                     }
                 });
        } catch (IOException e) {
            System.err.println("Error walking .atem directory: " + e.getMessage());
        }
        return count[0];
    }

    private static boolean updateSingleAtem(Path path, String searchPattern, String newLine) {
        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            List<String> updatedLines = new ArrayList<>();
            boolean modified = false;

            for (String line : lines) {
                // SAFETY SHIELD: If the line contains "Both", keep it exactly as it is.
                if (line.contains("Both")) {
                    updatedLines.add(line);
                    continue; 
                }

                // TARGETED FLIP: Only change lines matching the current L1 or L2 pattern
                if (line.trim().equals(searchPattern)) {
                    updatedLines.add(newLine);
                    modified = true;
                } else {
                    updatedLines.add(line);
                }
            }

            if (modified) {
                Files.write(path, updatedLines, StandardCharsets.UTF_8);
                return true;
            }
        } catch (IOException e) {
            System.err.println("Could not process file: " + path);
        }
        return false;
    }

    private static void applyHtmlLogic(String mode, Map<String, String> settings) {
        if (mode.equals("HTML_GE_E")) {
            settings.put("generate.file.html.version.v1", "no");
            settings.put("generate.file.html.version.v2", "yes");
            settings.put("generate.file.html.version.v1v2", "yes");
        } else {
            settings.put("generate.file.html.version.v1", mode.equals("HTML_G") ? "yes" : "no");
            settings.put("generate.file.html.version.v2", mode.equals("HTML_E") ? "yes" : "no");
            settings.put("generate.file.html.version.v1v2", mode.equals("HTML_GE") ? "yes" : "no");
        }
    }

    private static void applyPdfLogic(String mode, Map<String, String> settings) {
        settings.put("generate.file.pdf.version.v1", mode.equals("PDF_G") ? "yes" : "no");
        settings.put("generate.file.pdf.version.v2", mode.equals("PDF_E") ? "yes" : "no");
        settings.put("generate.file.pdf.version.v1v2", mode.equals("PDF_GE") ? "yes" : "no");

        if (mode.equals("PDF_GE")) {
            settings.put("cover.version", "pdf.covers_en_US_goarch.GE.text");
            settings.put("page.columns.quantity", "1");
            settings.put("page.columns.gap", "0in");
        } else {
            String lang = mode.equals("PDF_G") ? "G" : "E";
            settings.put("cover.version", "pdf.covers_en_US_goarch." + lang + ".text");
            settings.put("page.columns.quantity", "2");
            settings.put("page.columns.gap", ".1in");
        }
    }

    private static void updateSettings(Map<String, String> settings) throws IOException {
        Path path = Paths.get(TARGET_FILE);
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        List<String> newLines = new ArrayList<>();

        for (String line : lines) {
            String processedLine = line;
            for (Map.Entry<String, String> entry : settings.entrySet()) {
                String regex = "(" + Pattern.quote(entry.getKey()) + "\\s*=\\s*)([^\\s/]+|\"[^\"]*\")";
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(processedLine);
                if (m.find()) {
                    String oldValue = m.group(2);
                    String newValue = entry.getValue();
                    if (oldValue.startsWith("\"")) newValue = "\"" + newValue + "\"";
                    processedLine = m.replaceAll("$1" + Matcher.quoteReplacement(newValue));
                }
            }
            newLines.add(processedLine);
        }
        Files.write(path, newLines, StandardCharsets.UTF_8);
    }
}