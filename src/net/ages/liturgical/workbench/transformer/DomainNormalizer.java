package net.ages.liturgical.workbench.transformer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.mongodb.BasicDBObject;

import net.ages.liturgical.workbench.transformer.AlwbFileUtils;

/**
 * The purpose of this program is to change the keys in designated files so that they
 * point to another file.
 * 
 * There are three variables you have to set:
 * 
 * pathToLIbrarySrc - the path to your library project source folder
 * domainForEntryValue - the domain you want each key to use
 * fileDomainRegEx - the regular expression to match filenames.  
 * 
 * For example, if you want all en_US_ages Menaion files to have keys pointing to en_US_dedes,
 * set domainForEntryValue = "_en_US_dedes" (note the initial underscore)
 * set fileDomainRegEx = "me.m...d.._en_US_ages.ares"
 * 
 * For a file named me.m02.d02_en_US_ages.ares,  
 * and a key meSV.Stichera1.mode = misc_en_US_dedes.Mode1
 * 
 * it will be changed to
 * 
 * meSV.Stichera1.mode = me.m02.d02_en_US_dedes.meSV.Stichera1.mode
 *
 * When you first write a regular expression for fileDomainRegEx, first run it
 * with updateMatchingFiles set to false.  That way you can test the regular expression
 * without updating files.
 * 
 * If this works, then the next step in testing is to temporarily set fileDomainRegEx to
 * a copy of a file, e.g. fileDomainRegEx = "me.m...d.._en_US_test"
 * and change updateMatchingFiles to true.  The program will update the test file.
 * 
 * If you prefer to preserve the test file and generate a separate updated file,
 * set generateTestFiles to true;  You can also set the test file suffix to use.
 * 
 * @author mac002
 *
 */
public class DomainNormalizer {

	// You need to set these three variables
//	private static String pathToLibrarySrc = "C:\\ALWB_Additional_Resources\\ages-alwb-transformer-old\\net.ages.liturgical.workbench.transformer\\src\\net\\ages\\liturgical\\workbench\\transformer\\ares";
	private static String pathToLibrarySrc = "C:\\git\\gr-redirects-goarch\\gr_redirects_goarch";
	private static String domainForEntryValue = "_gr_GR_cog";
	private static String fileDomainRegEx = "le\\..*_gr_redirects_goarch.ares";

	// Change the following variables to test your fileDomainRegEx 
	// When you believe your fileDomainRegEx is correct, set both of the following booleans to false
	private static boolean updateMatchingFiles = true; // set to false to just get a list of matching files 
	private static boolean generateTestFiles = false; // set to true to create a new file instead of updating the existing one
	private static String testFileSuffix = ".txt"; // if generateTestFiles = true, this is the file suffix to use for test files
	
	public static void main(String[] args) {
		List<File> filesToProcess = AlwbFileUtils.getMatchingFilesInDirectory(pathToLibrarySrc, fileDomainRegEx, "ares");
		int nbrFiles = filesToProcess.size();
		BufferedReader br = null;
		String line;
		String resourcePrefix = "";
		System.out.println(nbrFiles + " files matched...");
		String [] parts = null;
		String comment = "";
		String fileOut = "";
		
		for (File f: filesToProcess) {
			if (updateMatchingFiles) {
				StringBuffer newContent = new StringBuffer();
				try {
					br = new BufferedReader(new FileReader(f));
					while ((line = br.readLine()) != null) {
						if (line.contains("A_Resource_Whose_Name")) {
							resourcePrefix = line.split(" = ")[1].split("_")[0] + domainForEntryValue;
							System.out.println("Setting entry values to " + resourcePrefix + " for " + f.getPath());
						} else if (line.startsWith("//")) {
							// do nothing
						} else if (line.contains("=")) {
							try {
								comment = "";
								parts = line.split("=");
								if (parts.length > 1) {
									if (parts[1].contains("//")) {
										comment = parts[1].split("//")[1];
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
							line = parts[0].trim() + " = " + resourcePrefix + "." + parts[0].trim();
							if (comment.length() > 0) {
								line = line + " // " + comment;
							}							
						}
						
						newContent.append(line);
						newContent.append("\n");
					}
					br.close();
					fileOut = f.getPath();
					if (generateTestFiles) {
						fileOut = fileOut.replace(".ares", testFileSuffix);
					} else {
					}
					AlwbFileUtils.writeFile(fileOut, newContent.toString());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
