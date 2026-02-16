package net.ages.liturgical.workbench.transformer;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;

import net.ages.liturgical.workbench.transformer.AlwbFileUtils;
/**
 * <b>Provides methods to transform XML-FO to PDF:
 * 
 * </b>
 * <p>Utilizes Saxon, Apache FOP for the HTML and PDF, and additionally, Eclipse EMF and Mylyn Docs EPUB for the ePub generation.
 * <p>Based on examples from 
 * <a href="http://blog.msbbc.co.uk/2007/06/simple-saxon-java-example.html">simple-saxon-java-example</a>,
 * <br>
 * <a href="http://xmlgraphics.apache.org/fop/1.1/embedding.html">Embedding Saxon in Java Code</a>, and
 * <br>a href="http://www.ibm.com/developerworks/library/x-xslfo2app/">HTML to Formatting Objects (FO) Conversion Guide</a>.
 * <br>
 * <a href="https://github.com/turesheim/epub-examples/blob/master/src-java/no/resheim/epub/example/Basic.java">Mylyn ePub Example</a>
 * <p>Note that the build path must include all the FOP 1.1 jars for the XSL transforms and the Mylan jars for the ePub.
 * 
 * <p>When trying to understand this class, start with the method
 * xmlTransformer().  It is used to transform
 * xml (DocBook) to html and to XSL-FO.
 * 
 * <p>Then look at the method foToPdf().  It takes the 
 * fo output from xmlTransformer and converts it to PDF.
 * 
 * <p>Everything else boils down to convenience methods.
 * 
 * @author Michael Colburn
 * @version 1.0
 * 
 * @see AlwbTransformer#xmlTransformer(String, String, String)
 * @see AlwbTransformer#foToPdf()
 *
 */
public class AlwbTransformer {
	
	private FopFactory fopFactory;
	public HashMap<String, String> parameters = new HashMap<String, String>();
	private String sourceFile;
	private String outputDirectory;
	private boolean deleteIntermediateFiles;
	private boolean preserveLinebreaks;
	private String outputFileRoot;
	private final String epubSuffix = ".epub";
	private final String htmlSuffix = ".html";
	private final String pdfSuffix = ".pdf";
	private final String psSuffix = ".ps";
	private final String foSuffix = ".fo";
	private String xsl_docbook_html = "lib/docbook-xsl-1.76.1/html/docbook.xsl";
	private String xsl_docbook_fo = "lib/docbook-xsl-1.76.1/fo/docbook.xsl"; 
	private String xsl_html_fo = "lib/html-to-fo-xsl/xhtml-to-xslfo.xsl"; 
	private String fonts = "lib/user-fop/fonts";
	private String fopConfig = "lib/user-fop/alwbFOP.xconf";
	private String htmlOutputFile;
	private String pdfOutputFile;
	private String pdfOutputPath;
	private String psOutputFile;
	private String foOutputFile;
	private String foInputFile;
	private String ePubOutputFile;
	
	/**
	 * 
	 * @param xmlSourceFile - file containing the DocBook xml.  Include full path to file.
	 * @param outputDirectory - the path to the directory to place generated files
	 * @param deleteIntermediateFiles - set to true if you wish to delete intermediate files, e.g. *.fo
	 * <p>Note that each parameter has a pair of get/set methods so you can change values
	 * after instantiation.
	 * 
	 * @see AlwbTransformer#setXmlSourceFileName
	 * @see AlwbTransformer#setOutputDirectory
	 * @see AlwbTransformer#setDeleteIntermediateFiles
	 */
	public AlwbTransformer(
			String xmlSourceFile, 
			String outputDirectory, 
			boolean deleteIntermediateFiles,
			boolean preserveLinebreaks) {
				
		// set the TransformFactory to use the Saxon TransformerFactoryImpl method 
		 System.setProperty("javax.xml.transform.TransformerFactory",
		 "com.icl.saxon.TransformerFactoryImpl"); 
		 
		 // create a reusable factory instance
		 fopFactory = FopFactory.newInstance();
		 System.out.println(xmlSourceFile);
		 System.out.println(outputDirectory);
		 setSourceFileName(xmlSourceFile);
		 setOutputDirectory(outputDirectory);
		 setDeleteIntermediateFiles(deleteIntermediateFiles);
		 setPreserveLinebreaks(preserveLinebreaks);
		 setOutputFiles();
	}

	/**
	 * <b>Converts an XML-FO file to PDF.</b>
	 * <p>You must call xmlToFo() before calling foToPdf()
	 * @see AlwbTransformer#xmlToPdf()
	 */
	private void foToPdf() {

		OutputStream out = null;
		File srcFile = new File(sourceFile);
		
		try {
			
			// Initialize
			fopFactory.setUserConfig(confUriString());
			fopFactory.getFontManager().setFontBaseURL(fontUriString());
			
			// Step 1: Set up output stream.
			// Note: Using BufferedOutputStream for performance reasons (helpful with FileOutputStreams).
			File outFile = new File(pdfOutputFile);
			out = new BufferedOutputStream(new FileOutputStream(outFile));

			// Step 2: Construct fop with desired output format
		    Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);

		    // Step 3: Setup JAXP using identity transformer
		    TransformerFactory factory = TransformerFactory.newInstance();
		    Transformer transformer = factory.newTransformer(); // identity transformer

		    // Step 4: Setup input and output for XSLT transformation
		    // Setup input stream
		    Source src = new StreamSource(srcFile);

		    // Resulting SAX events (the generated FO) must be piped through to FOP
		    Result res = new SAXResult(fop.getDefaultHandler());

		    // Step 5: Start XSLT transformation and FOP processing
		    transformer.transform(src, res);

		} catch (Exception e) {
			handleException(e);
		} finally {
		    //Clean-up
		    try {
				out.close();
				if (deleteIntermediateFiles) {
					srcFile.delete();
				}
			} catch (IOException e) {
			    handleException(e);
			}
		}
	}


	/**
	 * Set the value of the deleteIntermediateFiles variable
	 * @param deleteIntermediateFiles -- if set to true, will delete intermediate (temporary work) files
	 */
	public void setDeleteIntermediateFiles(boolean deleteIntermediateFiles) {
		this.deleteIntermediateFiles = deleteIntermediateFiles;
	}

	/**
	 * Set the value of the preserveLinebreaks variable
	 * @param preserveLineBreaks -- if set to true, will preserve the linebreaks from the output file.
	 * @info If you set this to false, you will have a smaller HTML file, but won't be able to read it easily.
	 */
	public void setPreserveLinebreaks(boolean preserveLinebreaks) {
		this.preserveLinebreaks = preserveLinebreaks;
	}
	/**
	 * Sets the root name of the output file.  
	 * The root is the part to the left of the file extension.  
	 * For example, if a file is named <code>examples.xml</code>, <code>examples</code> is the root.
	 * 
	 * By default, the root of the input file is used as the root of the output files.  If the input file is examples.xml, the HTML output will be examples.html, and the PDF output will be examples.pdf.
	 * 
	 * @param outputFileRoot - the output file root name
	 */
	private void setOutputFileRoot(String outputFileRoot) {
		this.outputFileRoot = outputFileRoot;
		setOutputFiles();
	}
	
	/**
	 * Get the name of the XML or HTML source file.
	 * @return the name of the XML or HTML source file.  Typically this is the path plus the name.
	 */
	public String getSourceFile() {
		return sourceFile;
	}

	/**
	 * Set the name of the XML or HTML source file.
	 * @param sourceFile - name of the XML or HTML source file.  Should include the path to the file.
	 */
	public void setSourceFileName(String sourceFile) {
		this.sourceFile = sourceFile;
		setOutputFileRoot(getRoot(sourceFile));
		pdfOutputPath = getPath(sourceFile);
	}
	
	public String fontUriString() {
		return new File(fonts).toURI().toString();
	}

	public String confUriString() {
		return new File(fopConfig).toURI().toString();
	}
	
	/**
	 * Extract the root part of a filename. 
	 * <p>Example: <code>foo/bar/myfile.html</code> will return <code>myfile</file>
	 * @param file -- the name of the file.  May include the path.
	 * @return the root part of the filename.
	 */
	private String getRoot(String file) {
		String result = "";
		try {
			String[] pathParts = file.split("/");
			String[] fileParts = pathParts[pathParts.length-1].split("\\.");
			String[] newArray = ArrayUtils.subarray(fileParts, 0, fileParts.length-1);
			result =  StringUtils.join(newArray,".");
		} catch (Exception e) {
		    handleException(e);
		}
		return result;
	}
	
	private String getPath(String file) {
		String result = "";
		try {
			String[] pathParts = file.split("/");
			for (int i=1; i < pathParts.length-1; i++) {
				result = result + "/" + pathParts[i];
			}
			result = result + "/";
		} catch (Exception e) {
		    handleException(e);
		    return "/";
		}
		return result;
	}

	/**
	 * Get the directory in which all generated files will be placed.
	 * <p>
	 * @return the output directory path
	 */
	public String getOutputDirectory() {
		return outputDirectory;
	}

	/**
	 * Set the directory in which to place all generated files.
	 * <p>This is set automatically via the outputDirectory parm in 
	 * the class constructor when you instantiate the class.  It is provided as a public method
	 * so you can change the path after instantiation if you have a need
	 * to do so.
	 * <p>
	 * @param outputDirectory
	 */
	public void setOutputDirectory(String outputDirectory) {
		this.outputDirectory = outputDirectory;
		setOutputFiles();
	}
	
	/**
	 * Set the various output file path/names
	 */
	private void setOutputFiles() {
		setPdfOutputFile();
	}
	/**
	 * Shared method to handle exceptions.
	 * @param e -- the exception that has occurred
	 */
	private static void handleException(Exception e) {
	     System.out.println("EXCEPTION: " + e);
	     e.printStackTrace();
	}
	
	
	/**
	 * Set the path and name of the PDF output file
	 * <p>This method is called automatically whenever the output Directory or output Filename root change.
	 */
	private void setPdfOutputFile() {
		if (outputFileRoot.startsWith("..")) {
			this.pdfOutputFile = outputFileRoot + pdfSuffix;
		} else {
			this.pdfOutputFile = ".." + pdfOutputPath + outputFileRoot + pdfSuffix;
		}
	}

	/**
	 * Set the path and name of the FO output file
	 * <p>This method is called automatically whenever the output Directory or output Filename root change.
	 */
	public void setFoOutputFile() {
		this.foOutputFile = outputDirectory + "/" + outputFileRoot + foSuffix;
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
		String foSource = "../../../../git/ages-alwb-templates/net.ages.liturgical.workbench.templates/src-gen/website";
		boolean deleteFoFiles = true;
		List<File> files = AlwbFileUtils.getFilesInDirectory(foSource,"fo");

		for (File f : files) {
			System.out.println(f.getPath());
			generatePdf(f.getPath(),foSource, deleteFoFiles);
		}
	}
	
	public static void generatePdf(String inputFile, String dirOut, boolean deleteFoFiles) {
		AlwbTransformer t = new AlwbTransformer(inputFile,dirOut,deleteFoFiles,true);
		t.parameters.clear();
		t.foToPdf();
	}
}
