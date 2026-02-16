package net.ages.liturgical.workbench.transformer.tms;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import net.ages.liturgical.workbench.transformer.AlwbFileUtils;

public class HtmlToJson {

	public static void main(String[] args) {
		String source = "../net.ages.liturgical.workbench.templates/src-gen/website/public/dcs/h/s/euchologion/liturgies/gr";
		List<File> files = AlwbFileUtils.getFilesInDirectory(source,"html");

		for (File f : files) {
			System.out.println(f.getPath());
			convertToJson(f,source);
		}	
	}

	/**
	 * Main method for the html to json conversion
	 * @param f - pointer to the file instance
	 * @param source - the directory in which the files were found
	 */
	private static void convertToJson(File f, String source) {
		Elements cells = AlwbFileUtils.getHtmlLeftCellsFromTable(f);
		System.out.println("Number of cells is: " + cells.size());
		ListIterator it = cells.listIterator();
		while (it.hasNext()) {
			String cell = it.next().toString();
			String [] parts = cell.split("<");
			System.out.println(cell);
/**
 * 			
 			Document doc = Jsoup.parse(it.next().toString());
			for (int i=0; i < doc.childNodeSize(); i++) {
				System.out.println(doc.child(i).toString());
			}
			List children = doc.childNodes();
			Iterator childIt = children.iterator();
			while (childIt.hasNext()) {
				System.out.println(childIt.next().toString());
			}
*/
		}
	}

	private static String toJson(String html) {
		String result = "";
		try {
            JSONObject xmlJSONObj = XML.toJSONObject(html);
            result = xmlJSONObj.toString(4);
            System.out.println(result);
        } catch (JSONException je) {
            System.out.println(je.toString());
        }
		return result;
	}

}
