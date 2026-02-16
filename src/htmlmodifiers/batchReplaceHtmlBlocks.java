package htmlmodifiers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class batchReplaceHtmlBlocks {

    public static void main(String[] args) {
        // --- Global Configuration ---
        
        // **TARGET FILE PATH:** The file that receives all the updates.
        String targetFilePath = "C:/git/ages-alwb-assets/net.ages.liturgical.workbench.website.assets.ages/root/booksindex.html"; 

        // --------------------------------------------------------------------------------------
        // **BATCH CONFIGURATION:** Define all blocks to be replaced.
        // Each map represents one replacement operation.
        // --------------------------------------------------------------------------------------
        List<Map<String, String>> replacementConfig = new ArrayList<>();

        // --- BLOCK 1:
        replacementConfig.add(createBlockConfig(
            "C:/git/ages-alwb-system/net.ages.liturgical.workbench.system/MEDIA_INDEX_UTILITY/output/enKA.html", 
            "<li class=\"level_2\">Katavasias", 
            "</li>"
        ));

        // --- BLOCK 2:
        // **ACTION REQUIRED:** Update the path and unique markers for your second block.
        replacementConfig.add(createBlockConfig(
            "C:/git/ages-alwb-system/net.ages.liturgical.workbench.system/MEDIA_INDEX_UTILITY/output/enME.html", 
            "<li class=\"level_2\">Menaion", // Example Start Marker
            "</li>"
        ));

        // --- BLOCK 3:
        replacementConfig.add(createBlockConfig(
            "C:/git/ages-alwb-system/net.ages.liturgical.workbench.system/MEDIA_INDEX_UTILITY/output/enOC.html", 
            "<li class=\"level_2\">Octoechos", // Example Start Marker
            "</li>"
        ));

        // --- BLOCK 4:
        replacementConfig.add(createBlockConfig(
            "C:/git/ages-alwb-system/net.ages.liturgical.workbench.system/MEDIA_INDEX_UTILITY/output/enTR.html", 
            "<li class=\"level_2\">Triodion", // Example Start Marker
            "</li>"
        ));

        // --- BLOCK 5:
        replacementConfig.add(createBlockConfig(
            "C:/git/ages-alwb-system/net.ages.liturgical.workbench.system/MEDIA_INDEX_UTILITY/output/enPE.html", 
            "<li class=\"level_2\">Pentecostarion", // Example Start Marker
            "</li>"
        ));
        
        // --- BLOCK 6:
        replacementConfig.add(createBlockConfig(
            "C:/git/ages-alwb-system/net.ages.liturgical.workbench.system/MEDIA_INDEX_UTILITY/output/grKA.html", 
            "<li class=\"level_2\">Katavasias (Greek)", 
            "</li>"
        ));

        // --- BLOCK 7:
        // **ACTION REQUIRED:** Update the path and unique markers for your second block.
        replacementConfig.add(createBlockConfig(
            "C:/git/ages-alwb-system/net.ages.liturgical.workbench.system/MEDIA_INDEX_UTILITY/output/grME.html", 
            "<li class=\"level_2\">Menaion (Greek)", // Example Start Marker
            "</li>"
        ));

        // --- BLOCK 8:
        replacementConfig.add(createBlockConfig(
            "C:/git/ages-alwb-system/net.ages.liturgical.workbench.system/MEDIA_INDEX_UTILITY/output/grOC.html", 
            "<li class=\"level_2\">Octoechos (Greek)", // Example Start Marker
            "</li>"
        ));

        // --- BLOCK 9:
        replacementConfig.add(createBlockConfig(
            "C:/git/ages-alwb-system/net.ages.liturgical.workbench.system/MEDIA_INDEX_UTILITY/output/grTR.html", 
            "<li class=\"level_2\">Triodion (Greek)", // Example Start Marker
            "</li>"
        ));

        // --- BLOCK 10:
        replacementConfig.add(createBlockConfig(
            "C:/git/ages-alwb-system/net.ages.liturgical.workbench.system/MEDIA_INDEX_UTILITY/output/grPE.html", 
            "<li class=\"level_2\">Pentecostarion (Greek)", // Example Start Marker
            "</li>"
        ));
        
        // --------------------------------------------------------------------------------------

        try {
            Path targetPath = Paths.get(targetFilePath);
            if (!Files.exists(targetPath)) {
                System.err.println("Error: Target file not found: " + targetPath);
                return;
            }
            
            System.out.println("Starting batch replacement on: " + targetPath.getFileName());
            
            int count = 0;
            for (Map<String, String> config : replacementConfig) {
                count++;
                String source = config.get("source");
                String start = config.get("start");
                String end = config.get("end");
                
                System.out.println("--- Processing Block " + count + " (" + start.substring(0, Math.min(start.length(), 20)) + "...)" + " ---");

                // 1. Extract the entire List Block Content from the Source File
                String blockContent = extractBlockContent(Paths.get(source), start, end);

                if (blockContent == null) {
                    System.err.println("Skipping block " + count + ": Extraction failed from " + Paths.get(source).getFileName());
                    continue; // Move to the next block
                }
                
                // 2. Replace the old block in the Target File with the new content
                replaceBlockInTarget(targetPath, start, end, blockContent);
            }

            System.out.println("\n✅ Batch replacement completed. Total blocks processed: " + count);
        } catch (IOException e) {
            System.err.println("An I/O error occurred during processing: " + e.getMessage());
        }
    }
    
    // Helper method to create configuration maps
    private static Map<String, String> createBlockConfig(String source, String start, String end) {
        Map<String, String> config = new LinkedHashMap<>();
        config.put("source", source);
        config.put("start", start);
        config.put("end", end);
        return config;
    }

    // ------------------------------------------------------------------
    
    /**
     * Reads the source file and extracts all content between the markers (including markers).
     */
    private static String extractBlockContent(Path sourcePath, String startMarker, String endMarker) throws IOException {
        if (!Files.exists(sourcePath)) {
            System.err.println("Source file not found: " + sourcePath);
            return null;
        }

        List<String> allLines = Files.readAllLines(sourcePath, StandardCharsets.UTF_8);
        String content = String.join(System.lineSeparator(), allLines);

        // Regex to capture everything from the Start Marker up to and including the End Marker.
        String regex = "(?s)(" + java.util.regex.Pattern.quote(startMarker) + ".*?" + java.util.regex.Pattern.quote(endMarker) + ")";
        
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            return matcher.group(1); 
        } else {
            System.err.println("Extraction failed. Markers not found in source: " + sourcePath.getFileName());
            return null;
        }
    }
    
    /**
     * Reads the target file, replaces the old block with the new content, and writes the file back.
     * This method reads and writes the target file ONCE per block replacement.
     */
    private static void replaceBlockInTarget(Path targetPath, String startMarker, String endMarker, String newContent) throws IOException {
        
        List<String> allLines = Files.readAllLines(targetPath, StandardCharsets.UTF_8);
        String content = String.join(System.lineSeparator(), allLines);

        // Check for both markers before attempting replacement
        if (!content.contains(startMarker) || !content.contains(endMarker)) {
            System.err.println("Replacement skipped. Markers not found in target file for start: " + startMarker);
            return;
        }

        // Regex to find the existing block in the target file.
        String regex = "(?s)(" + java.util.regex.Pattern.quote(startMarker) + ".*?" + java.util.regex.Pattern.quote(endMarker) + ")";
        
        // Use replaceFirst to replace the *entire* old block with the new content
        String modifiedContent = content.replaceFirst(regex, newContent);

        // Write the modified content back to the target file
        Files.write(targetPath, modifiedContent.getBytes(StandardCharsets.UTF_8));
        System.out.println("  -> Successfully replaced block starting with: " + startMarker);
    }
}