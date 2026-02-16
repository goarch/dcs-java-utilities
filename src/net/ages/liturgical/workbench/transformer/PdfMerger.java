package net.ages.liturgical.workbench.transformer;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.util.PDFMergerUtility;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class PdfMerger {

	// You have to modify these Strings depending on the site you are processing.  
	// Do not run it against /src-gen/website.  Be specific, e.g. src-gen/website/public/dcs.
	static String sourceDir = "../net.ages.liturgical.workbench.templates.ematins/src-gen/website/test/dcs";
	static String pathToLocalMedia = "../net.ages.liturgical.workbench.media/m/";
	static String destination = sourceDir + "/m/s";
	
	// set this to true if you want to view messages during the process of combining PDFs
	static boolean verbose = true;
	
	public static void main(String[] args) {
		List<File> theFiles = AlwbFileUtils.getServicesHtmlFilesInDirectory(sourceDir+"/h", "html");
		processFiles(theFiles);
	}

	public static void processFiles(List<File> list) {
		Iterator<File> it = list.iterator();
		while (it.hasNext()) {
			try {
				mergePdfFiles(it.next());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void mergePdfFiles(File file) {
 		PDFMergerUtility mergePdf = new PDFMergerUtility();
		String hrefFromHtml = AlwbFileUtils.getMergedPdfHrefFromHtmlFile(file);
		if ((hrefFromHtml != null) && (hrefFromHtml != "") ) {
			Elements hrefs = AlwbFileUtils.getPdfHrefs(file);
			if (verbose) {
				System.out.println("\nProcessing " + file.getName() + ", which has " + hrefs.size() + " PDF Hrefs "+ file.getPath());
			}
			Iterator<Element> it = hrefs.iterator();
			while (it.hasNext()) {
				String href = FilenameUtils.separatorsToUnix(it.next().attr("href"));
				String normalized = AlwbFileUtils.normalizeMediaPath("/m/", pathToLocalMedia,href);
				if (verbose) {
					File test = new File(normalized);
					System.out.println("Adding PDF: " + normalized + " Exists? " + test.exists());
				}
				mergePdf.addSource(normalized);
			}
			File newFile;
			String hrefFilename = AlwbFileUtils.filenameFromHref(hrefFromHtml);
			String hrefPath = FilenameUtils.separatorsToUnix(AlwbFileUtils.pathFromHref(hrefFromHtml));
			String path = destination + hrefPath;
			newFile = new File(path);
			newFile.mkdirs();
			mergePdf.setDestinationFileName(path + "/" + hrefFilename);
			if (verbose) {
				System.out.println("Combining files into: " + mergePdf.getDestinationFileName());
			}
			try {
				mergePdf.mergeDocuments();
			} catch (COSVisitorException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
