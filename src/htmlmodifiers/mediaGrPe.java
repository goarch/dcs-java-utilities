package htmlmodifiers;

import java.util.*;
import java.util.regex.Pattern;
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;

public class mediaGrPe {

    public static void main(String[] args) throws IOException {

        Scanner scannerMediaMap = new Scanner(new File("C:/git/ages-alwb-templates/net.ages.liturgical.workbench.templates/media-maps/media_en_redirects_goarch.ares"));
        Scanner scannerHeaderFooter = new Scanner(new File("C:/git/ages-alwb-system/net.ages.liturgical.workbench.system/MEDIA_INDEX_UTILITY/header_footer.txt"));
        Scanner scannerKeyDescriptors = new Scanner(new File("C:/git/alwb-library-en-us-goadedes/alwb.library_en_US_goadedes/Properties/key.descriptors_en_US_goadedes.ares"));
        Scanner scannerIndexDescriptors = new Scanner(new File("C:/git/ages-alwb-templates/net.ages.liturgical.workbench.templates/b-preferences/goarch/website.index.titles_en_US_goarch.ares"));

        //array to hold current media map line
        String[] currentLine = new String[4];
        String[] previousLine = new String[4];
        
        String fs = ""; //for testing with loop logic
        
        String fileName = "";
        String str = "";
        String arrOrSinger = "";
        String musicStyle = "";
        String specialTr = "";
        String altText = "";
        
        boolean firstLine = true;

        //grab HTML header data and add to final string 
        for (int i = 1; i <= 203; i++) {
            fs += scannerHeaderFooter.nextLine()+"\n";
        }

        //Store each key and descriptor in a new list index
        ArrayList<String> keyDesc = new ArrayList<String>();
        while(scannerKeyDescriptors.hasNextLine()){
            keyDesc.add(scannerKeyDescriptors.nextLine());
        }

        //Store each day and descriptor in a new list index
        ArrayList<String> dayDesc = new ArrayList<String>();
        while(scannerIndexDescriptors.hasNextLine()){
            dayDesc.add(scannerIndexDescriptors.nextLine());
        }
        
      
        //Loop through the media map
        while(scannerMediaMap.hasNextLine()){   
            str = scannerMediaMap.nextLine(); 
            //if the first two letters aren't pe, then go to the next line
            if(str.length()<2||!str.substring(0,2).equals("pe")){continue;}
            
            if(str.contains(".MM.")) {str = str.replace(".MM.", ".");} //get rid of all .MM.
            
            //eliminating the .alt1
            altText = "";
            if(str.contains(".alt1")) {str = str.substring(0,8) + str.substring(13); altText = "Alternative: ";}
            if(str.contains(".alt2")) {str = str.substring(0,8) + str.substring(13); altText = "Another Alt.: ";}
            if(str.contains(".alt3")) {str = str.substring(0,8) + str.substring(13); altText = "Another Alt.: ";}
            
            
            //store the arranger or singer name and then skip the line
            if(str.contains("arranger")||str.contains("singer")){
            	str = str.substring(0,str.length()-1)+"/"+str.substring(str.length()-1); //adds a / before the final "
            	if(str.substring(str.length()-3).equals("//\"")){
            		arrOrSinger = str.substring(str.indexOf("= \"")+3, str.indexOf("//\""));
            	}else {
            		arrOrSinger = str.substring(str.indexOf("= \"")+3, str.indexOf("/\""));  //fix
            	}
                continue;
            }
            //01234567890123456789
            //pe.d076.peMA.Exaposteilarion1.c1211.sco
            currentLine[0] = str.substring(0, 2); //get book
            currentLine[1] = str.substring(3, 7); //get day including d
            currentLine[2] = str.substring(10, 12); //get service
            currentLine[3] = str.substring(13,str.substring(13).indexOf(".")+13); //get name of hymns
            

            String serviceIdentifier = currentLine[2];

            //set the display text data from the line
            currentLine[0] = "Pentecostarion (Greek)";
                
            //set the music style
            if(str.contains(".w.")){musicStyle = "Score: Staff";}
            if(str.contains(".b.")){musicStyle = "Score: Byzantine";}
            if(str.contains(".a")&&!str.contains(".arr")){musicStyle = "Audio";}
           
            //store the name/location of the file
            if(str.contains(".pdf")) fileName = str.substring(str.indexOf(".path")+9, str.indexOf(".pdf")+4);
            if(str.contains(".mp3")) fileName = str.substring(str.indexOf(".path")+9, str.indexOf(".mp3")+4); //     ../../a...
            
            //Change the abbreviations to their final format
            currentLine[1] = replaceDay(dayDesc, currentLine[1]);
            currentLine[2] = altText + replaceService(dayDesc, currentLine[2], specialTr);
            currentLine[3] = replaceKey(keyDesc,currentLine[3], specialTr+serviceIdentifier);
            
            //sets the beginning of the tree
            //This will only get accessed for the first iteration that finds an "pe"
            if(firstLine) {
            	firstLine = false;
                //beginning list structure
        		for(int i = 0 ; i < currentLine.length ; i++) {
        			fs += tabs(7+2*i)+"<li class=\"level_"+(i+1+1)+"\">"+currentLine[i]+"\n"+tabs(8+2*i)+"<ul>\n";
        		}
            	//add the media line
                if(fileName.contains(".mp3")){
                	fs += tabs(17)+"<li dcslink=\"https://dcs.goarch.org"+fileName+"\">"+musicStyle+"<span class = contributor>"+arrOrSinger+"</span></li>\n";
                }else if(fileName.contains(".pdf")){
                	fs += tabs(17)+"<li dcslink=\"https://dcs.goarch.org"+fileName+"\">"+musicStyle+"<span class = contributor>"+arrOrSinger+"</span></li>\n";
                }
                //set the previous line strings
                for(int i = 0 ; i < currentLine.length ; i++) {
                	previousLine[i] = currentLine[i];
                }
                //go to the next line of the media map
                continue;                
            }
            
            

            //loop through the lines and compare their values
            for(int i = 0 ; i < currentLine.length ; i++) {
            	//if the lines are the same through the hymn
            	if(i==currentLine.length-1 && currentLine[i].equals(previousLine[i])) {
                	//add the media line
                    if(fileName.contains(".mp3")){
                    	fs += tabs(17)+"<li dcslink=\"https://dcs.goarch.org"+fileName+"\">"+musicStyle+"<span class = contributor>"+arrOrSinger+"</span></li>\n";
                    }else if(fileName.contains(".pdf")){
                    	fs += tabs(17)+"<li dcslink=\"https://dcs.goarch.org"+fileName+"\">"+musicStyle+"<span class = contributor>"+arrOrSinger+"</span></li>\n";
                    }
            	}else if(!currentLine[i].equals(previousLine[i])) {
            		//close out the levels through the different level
            		//there will be i+1 levels to close out
            		for(int j = currentLine.length-1 ; j >= i  ; j--) {	// if level 3 diff, then loop once, if level 2 diff, then loop twice, etc
            			fs += tabs(8+2*j)+"</ul>\n"+tabs(7+2*j)+"</li>\n";
            		}
            		//open levels to the max level
            		for(int j = i ; j < currentLine.length ; j++) {
            			fs += tabs(7+2*j)+"<li class=\"level_"+(j+1+1)+"\">"+currentLine[j]+"\n"+tabs(8+2*j)+"<ul>\n";
            		}
                	//add the media line
                    if(fileName.contains(".mp3")){
                    	fs += tabs(17)+"<li dcslink=\"https://dcs.goarch.org"+fileName+"\">"+musicStyle+"<span class = contributor>"+arrOrSinger+"</span></li>\n";
                    }else if(fileName.contains(".pdf")){
                    	fs += tabs(17)+"<li dcslink=\"https://dcs.goarch.org"+fileName+"\">"+musicStyle+"<span class = contributor>"+arrOrSinger+"</span></li>\n";
                    }
                    break; //go to the next line
            	}
            }//end inner for loop
          
            
            //set the previous line strings
            for(int i = 0 ; i < currentLine.length ; i++) {
            	previousLine[i] = currentLine[i] +"";
            }
        } //end loop

        //close all five list levels
        for(int i = 0; i < currentLine.length; i++) {
        	fs += tabs(16-2*i) + "</ul>\n" + tabs(15-2*i) + "</li>\n";
        }
        
        //grab HTML footer data and add to final string 
        for (int i = 1; i <= 5; i++) {
            fs += scannerHeaderFooter.nextLine()+"\n";
        }

        //System.out.println(finalString);
        
        //Write the String to an HTML File
        try {
            FileWriter myWriter = new FileWriter("C:/git/ages-alwb-system/net.ages.liturgical.workbench.system/MEDIA_INDEX_UTILITY/output/grPE.html");
            myWriter.write(fs);
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    //find and replace function
    public static String replaceKey(ArrayList<String> arr, String str, String id) { //str is level3 -- arr is key desciriptor
        //search for key then replace with description
        String tempStr = "";
        for(int j = 0; j<arr.size(); j++){
            if(Pattern.matches("^pe"+id+".*[.]"+str+"[.].*",arr.get(j))){  //checking key descriptor
                if(Pattern.matches(".*[:]..[:].*",arr.get(j))){ //if there are the two colon sections
                    tempStr = arr.get(j).substring(arr.get(j).indexOf(":")+1);
                    return tempStr.substring(tempStr.indexOf(":")+1,tempStr.length()-1);
                }else if(Pattern.matches(".*[:].*",arr.get(j))){ //if there is one colon sections()
                	return arr.get(j).substring(arr.get(j).indexOf(":")+1, arr.get(j).length()-1);
                }else{
                    return arr.get(j).substring(arr.get(j).indexOf("\"")+1,arr.get(j).length()-1);
                }
                
            }
        }
        //if the loop turned up nothing
        return str;
    }

    
    public static String replaceService(ArrayList<String> arr, String s, String tr) {
     
    	for(int i = 0; i < arr.size(); i++) {
    		if(Pattern.matches(s.toLowerCase()+"[.]html[.]link.*", arr.get(i))) {
    			return arr.get(i).substring(arr.get(i).indexOf("\"")+1, arr.get(i).length()-1)+tr;
    		}
    	}
    	return s;
    	
    }
    //01234567890123456
    //mc.d001 = "Sunday of the Publican and Pharisee"
    public static String replaceDay(ArrayList<String> arr, String d) {
    	
    	for(int i = 0; i < arr.size(); i++) {
    		if(Pattern.matches("mc[.]"+d+".*", arr.get(i))) {
    			return arr.get(i).substring(11, arr.get(i).length()-1);
    		}
    	}
        return d;
    }
    
    public static String tabs(int n) {
    	String build = "";
    	for(int i = 0 ; i< n ; i++) {
    		build += "\t";
    	}
    	return build;
    }

}





