package net.ages.liturgical.workbench.transformer;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

public class AlwbFileUtils {
	
	// I am a test comment. I am an addition.
	
	public static String[] getPathsToFilesInDirectory(String directory, String extension, String excludeSubPath) {
		List<File> list = getFilesInDirectory(directory, extension);
		List<String> paths = new ArrayList<String>();
		Iterator<File> it = list.iterator();
		while(it.hasNext()) {
			File f = it.next();
			String a = f.getAbsolutePath();
			String b = f.pathSeparator;
			String path = (String)f.getPath();
			if (! path.contains(excludeSubPath)) {
				paths.add(path);
			}
		}
		return paths.toArray(new String[paths.size()]);	
	}
	

	/**
	 * Finds all index.html files that are for services
	 * @param directory
	 * @param extension
	 * @return list<File> of index.html files
	 */
	public static List<File> getServicesHtmlFilesInDirectory(String directory, final String extension) {
		List<File> list = getFilesInDirectory(directory, extension);
		List<File> files = new ArrayList<File>();
		Iterator<File> it = list.iterator();
		String path = "";
		File f;
		while(it.hasNext()) {
			f = it.next();
			path = FilenameUtils.separatorsToUnix((String) f.getPath());
			if (path.contains("/h/s") && (path.endsWith("index.html"))) {
				files.add(f);
			}
		}
		return files;
	}
	
	public static List<File> getMatchingFilesInDirectory(String directory, String fileRegularExpression, String extension) {
		List<File> list = getFilesInDirectory(directory, extension);
		List<File> files = new ArrayList<File>();
		Iterator<File> it = list.iterator();
		String path = "";
		File f;
		while(it.hasNext()) {
			f = it.next();
			if (f.getName().matches(fileRegularExpression)) {
				files.add(f);
			}
		}
		return files;
	}
	
	/**
	 * Recursively read contents of directory and return all files found
	 * @param directory
	 * @param extension period + file extension, e.g. .html
	 * @return List containing all files found
	 */
	public static List<File> getFilesInDirectory(String directory, final String extension) {
		File dir = new File(directory);
		String [] extensions = {extension};
		List<File> files = null;
		try {
			files = (List<File>) FileUtils.listFiles(dir, extensions, true);
		} catch (Exception e) {
			files = null;
		}
		return files;
	}
	/**
	 * The static main method is provided for test purposes and as an example of how to use this class.
	 * <p>Normal usage is to instantiate the class elsewhere 
	 * set the parameters, and call the 
	 * xmlToHtml method or the xmlToPdf method.
	 * 
	 * @param args -- do not use.
	 */
	public static void main(String[] args) {
		String serviceYear = "2014"; 
//		String source = "../net.ages.liturgical.workbench.templates.ematins/src-gen/website";
		String source = "filesIn";
		List<File> files = getFilesInDirectory(source,"pdf");
		System.out.println(files.size() + " files found!");
	}
	
	public static Elements getPdfHrefs(File file) {
		Elements result = null;
		try {
			org.jsoup.nodes.Document doc = Jsoup.parse(file, "UTF-8", "http://example.com/");
			result = doc.select("a[href$=pdf]");
		} catch (IOException e) {
			e.printStackTrace();
			result = null;
		}
//		hrefsToString(result); // use for debugging
		return result;
	}
	
	/**
	 * Get all the left cells for table rows in the html
	 * @param file
	 * @return
	 */
	public static Elements getHtmlLeftCellsFromTable(File file) {
		Elements result = null;
		try {
			org.jsoup.nodes.Document doc = Jsoup.parse(file, "UTF-8", "http://example.com/");
			result = doc.select(".leftCell");
		} catch (IOException e) {
			e.printStackTrace();
			result = null;
		}
		return result;
	}

	// Use for debugging.  Prints elements to system out
	public static void hrefsToString(Elements list) {
		Iterator<Element> it = list.iterator();
		while (it.hasNext()) {
			Element e = it.next();
			System.out.println(e.attr("href"));
		}
	}
	
	/**
	 * Normalizes a HREF so it points to the local media Eclipse project
	 * @param hrefBaseUrl - part of URL to strip from href.  Needs to end with forward slash
	 * @param localMediaPath - path to prefix to the stripped href.  Needs to end with forward slash
	 * @param href - href to normalize
	 * @return normalized href
	 */
	public static String normalizeMediaPath(String hrefBaseUrl, String localMediaPath, String href) {
		String result = "";
		try {
			result = localMediaPath + href.split(hrefBaseUrl)[1];
		} catch (Exception e) {
//			e.printStackTrace();
			result = localMediaPath + href;
		}
		return result;
	}
	
	/**
	 * Get the part of the path that starts after the delimiter
	 * @param delimiter - string at end of which to split
	 * @param path - the path to be split
	 * @return the subpath
	 */
	public static String getSubPath(String delimiter, String path) {
		String result = "";
		try {
			result = path.split(delimiter)[1];
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		return result;
	}
	
	/**
	 * Replaces forward slashes in path with periods and returns a file 
	 * name based on the path information
	 * @param path
	 * @param fileName
	 * @return
	 */
	public static String pathToName(String path, String fileName) {
		return path.split("m/s/")[1].replace("/", ".") + fileName;
	}
	
	/**
	 * Attempts to get the Href to use for the combined PDFs for this service.
	 * @param file
	 * @return the Href if found, else null
	 */
	public static String getMergedPdfHrefFromHtmlFile(File file) {
		String result = null;
		try {
			org.jsoup.nodes.Document doc = Jsoup.parse(file, "UTF-8", "http://example.com/");
			result = doc.select("title").attr("data-combo-pdf-href");
		} catch (IOException e) {
			e.printStackTrace();
			result = null;
		}
		return result;
	}
	
	public static String pathFromHref(String href) {
		String result = "";
		try {
			String [] parts = href.split("m/s")[1].split("/");
			result = StringUtils.join(parts,"/",0,parts.length-1);
		} catch (Exception e) {
			e.printStackTrace();
			result = href;
		}
		return result;
	}

	public static String filenameFromHref(String href) {
		String result = "";
		try {
			String [] parts = href.split("m/s")[1].split("/");
			result = parts[parts.length-1];
		} catch (Exception e) {
			e.printStackTrace();
			result = href;
		}
		return result;
	}
	
	/**
	 * For a given ares filename, return the prefix and domain parts
	 * @param file - ares filename
	 * @return array with prefix in [0] and domain in [1]
	 */
	public static String[] getAresFileParts(String file) {
		String [] theParts;
		String [] result;
		try {
			theParts = file.split("_");
			result = new String[2];
			if (theParts.length ==4) {
				result[0] = theParts[0];
				result[1] = (theParts[1] + "." + theParts[2] + "." + theParts[3].replace(".tsf", "")).toLowerCase();
			} else {
				result = null;
			}
		} catch (Exception e) {
			result = null;
		}
		return result;
	}	
	
	public static String aresFileToMongoDbCollectionName(String filename) {
		String [] parts = getAresFileParts(filename);
		return parts[1]+"."+parts[0];
	}
	
	/**
	 * Create a new file and write the contents to it
	 * @param filename the name of the file, including the path
	 * @param content the contents to write
	 */
			
	public static void writeFile(String filename, String content) {
		File file = new File(filename);
		BufferedWriter bw = null;
		 
		try {
			file = new File(filename);
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			bw = new BufferedWriter(fw);
			bw.write(content);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
}
