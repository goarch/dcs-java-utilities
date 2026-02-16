package net.ages.liturgical.workbench.transformer;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class CleanSmartSvnPatch {
	private static String pathToPatchFile = "/Users/mac002/Documents/workspaces/kepler-dsl/alwb/p1/runfs_split/patches/test.diff";

	public static void main(String[] args) {
		try {
			File file = new File(pathToPatchFile);
			StringBuffer sb = new StringBuffer();
			boolean inDeleteZone = false;
			List<String> lines = FileUtils.readLines(file, "UTF-8");
			for (String line : lines) {
				if (inDeleteZone) {
					if (line.startsWith("Index:")) {
						inDeleteZone = false;
					}
				} else {
					if (line.startsWith("Property changes on")) {
						inDeleteZone = true;
					}
				}
				if (! inDeleteZone) {
					sb.append(line+"\n");
				}
			}
			FileUtils.writeStringToFile(file, sb.toString(), "UTF-8");
		} catch (Exception e) {
			
		}
	}

}
