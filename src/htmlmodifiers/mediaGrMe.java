package htmlmodifiers;

import java.util.*;
import java.util.regex.Pattern;
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;

public class mediaGrMe {

    public static void main(String[] args) throws IOException {

        Scanner scannerMediaMap = new Scanner(new File("C:/git/ages-alwb-templates/net.ages.liturgical.workbench.templates/media-maps/media_en_redirects_goarch.ares"));
        Scanner scannerHeaderFooter = new Scanner(new File("C:/git/ages-alwb-system/net.ages.liturgical.workbench.system/MEDIA_INDEX_UTILITY/header_footer.txt"));
        Scanner scannerKeyDescriptors = new Scanner(new File("C:/git/alwb-library-en-us-goadedes/alwb.library_en_US_goadedes/Properties/key.descriptors_en_US_goadedes.ares"));
        Scanner scannerIndexDescriptors = new Scanner(new File("C:/git/ages-alwb-templates/net.ages.liturgical.workbench.templates/b-preferences/goarch/website.index.titles_en_US_goarch.ares"));

        //array to hold current media map line
        String[] currentLine = new String[5];
        String[] previousLine = new String[5];
        
        String fs = ""; //for testing with loop logic
        
        String fileName = "";
        String str = "";
        String arrOrSinger = "";
        String musicStyle = "";
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
        while(scannerMediaMap.hasNextLine())/*for(int i = 1; i<=6756; i++)*/{   //5787 is last line without an error  added space at end of line in some instances and may fixed error
            str = scannerMediaMap.nextLine();  //least line is 6756
            //if the first two letters aren't me, then go to the next line
            if(str.length()<2||!str.substring(0,2).equals("me")){continue;}
            
            if(str.contains(".MM.")) {str = str.replace(".MM.", ".");} //get rid of all .MM.
            
            altText = "";
            if(str.contains(".alt1")) {str = str.substring(0,11) + str.substring(16); altText = "Alternative Commemoration: ";}
            if(str.contains(".alt2")) {str = str.substring(0,11) + str.substring(16); altText = "Another Alt. Commemoration: ";}
            if(str.contains(".alt3")) {str = str.substring(0,11) + str.substring(16); altText = "Another Alt. Commemoration: ";}
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
            //me.m01.d01.meVE.Stichera01.c1211.score.b.arranger = "SDedes/"
            currentLine[0] = str.substring(0, 2); //get book
            currentLine[1] = str.substring(3, 6); //get month
            currentLine[2] = str.substring(7, 10); //get day
            currentLine[3] = str.substring(13,15); //get service
            currentLine[4] = str.substring(16,str.substring(16).indexOf(".")+16); //get name of media
            


            String serviceIdentifier = currentLine[3];

            //set the display text data from the line
            currentLine[0] = "Menaion (Greek)";
                
            //set the music style
            if(str.contains(".w.")){musicStyle = "Score: Staff";}
            if(str.contains(".b.")){musicStyle = "Score: Byzantine";}
            if(str.contains(".a")&&!str.contains(".arr")){musicStyle = "Audio";}

            //store the name/location of the file
            if(str.contains(".pdf")) fileName = str.substring(str.indexOf(".path")+9, str.indexOf(".pdf")+4);
            if(str.contains(".mp3")) fileName = str.substring(str.indexOf(".path")+9, str.indexOf(".mp3")+4); //     ../../a...
            if(str.contains(".m4a")) fileName = str.substring(str.indexOf(".path")+9, str.indexOf(".m4a")+4); //     ../../a...
            
            //Change the abbreviations to their final format
            currentLine[1] = replaceMonth(currentLine[1]); //Final format for the month
            currentLine[2] = replaceDay(currentLine[2], currentLine[1]);
            currentLine[3] = altText + replaceService(dayDesc, currentLine[3]);
            currentLine[4] = replaceKey(keyDesc,currentLine[4], serviceIdentifier);
            
            //sets the beginning of the tree
            //This will only get accessed for the first iteration that finds an "me"
            if(firstLine) {
            	firstLine = false;
                //beginning list structure
        		for(int i = 0 ; i < currentLine.length ; i++) {
        			fs += tabs(7+2*i)+"<li class=\"level_"+(i+1+1)+"\">"+currentLine[i]+"\n"+tabs(8+2*i)+"<ul>\n";
        		}
            	//add the media line
                if(fileName.contains(".mp3")){
                	fs += tabs(17)+"<li dcslink=\"https://dcs.goarch.org"+fileName+"\">"+musicStyle+"<span class = contributor>"+arrOrSinger+"</span></li>\n";
                }else if(fileName.contains(".m4a")){
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
                        //fs += "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<li dcslink=\""+"../.."+fileName+"\">"+musicStyle+"<span class = contributor>"+arrOrSinger+"</span></li>\n";
                    }else if(fileName.contains(".m4a")){
                    	fs += tabs(17)+"<li dcslink=\"https://dcs.goarch.org"+fileName+"\">"+musicStyle+"<span class = contributor>"+arrOrSinger+"</span></li>\n";
                    	//fs += "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<li dcslink=\"js/viewer/web/viewer.html?file="+fileName+"\">"+musicStyle+"<span class = contributor>"+arrOrSinger+"</span></li>\n";
                    }else if(fileName.contains(".pdf")){
                    	fs += tabs(17)+"<li dcslink=\"https://dcs.goarch.org"+fileName+"\">"+musicStyle+"<span class = contributor>"+arrOrSinger+"</span></li>\n";
                    	//fs += "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<li dcslink=\"js/viewer/web/viewer.html?file="+fileName+"\">"+musicStyle+"<span class = contributor>"+arrOrSinger+"</span></li>\n";
                    }
            	}else if(!currentLine[i].equals(previousLine[i])) {
            		//close out the levels through the different level
            		//there will be i+1 levels to close out
            		for(int j = currentLine.length-1 ; j >= i  ; j--) {	// if level 4 diff, then loop once, if level 3 diff, then loop twice, etc
            			fs += tabs(8+2*j)+"</ul>\n"+tabs(7+2*j)+"</li>\n";
            		}
            		//open levels to the max level and include their names
            		for(int j = i ; j < currentLine.length ; j++) {
            			fs += tabs(7+2*j)+"<li class=\"level_"+(j+1+1)+"\">"+currentLine[j]+"\n"+tabs(8+2*j)+"<ul>\n";
            		}
                	//add the media line
                    if(fileName.contains(".mp3")){
                    	fs += tabs(17)+"<li dcslink=\"https://dcs.goarch.org"+fileName+"\">"+musicStyle+"<span class = contributor>"+arrOrSinger+"</span></li>\n";
                    }else if(fileName.contains(".m4a")){
                    	fs += tabs(17)+"<li dcslink=\"https://dcs.goarch.org"+fileName+"\">"+musicStyle+"<span class = contributor>"+arrOrSinger+"</span></li>\n";
                    }else if(fileName.contains(".pdf")){
                    	fs += tabs(17)+"<li dcslink=\"https://dcs.goarch.org"+fileName+"\">"+musicStyle+"<span class = contributor>"+arrOrSinger+"</span></li>\n";
                    }
                    break; //go to the next line
            	}
            }
          
            
            //set the previous line strings
            for(int i = 0 ; i < currentLine.length ; i++) {
            	previousLine[i] = currentLine[i];
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
            FileWriter myWriter = new FileWriter("C:/git/ages-alwb-system/net.ages.liturgical.workbench.system/MEDIA_INDEX_UTILITY/output/grME.html");
            myWriter.write(fs);
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    //find and replace function
    //meVE.Stichera01.desc = "ME:VE:Sticheron 1"
    public static String replaceKey(ArrayList<String> arr, String str, String id) { //str is level4 -- arr is key desciriptor
        //search for key then replace with description
        String tempStr = "";
        for(int j = 0; j<arr.size(); j++){
            if(Pattern.matches("^me"+id+".*[.]"+str+"[.].*",arr.get(j))){  //checking key descriptor
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
    
    public static String replaceMonth(String s) {

    	switch(Integer.parseInt(s.substring(1))) {
    	case 1:return "January";
    	case 2:return "February";
    	case 3:return "March";
    	case 4:return "April";
    	case 5:return "May";
    	case 6:return "June";
    	case 7:return "July";
    	case 8:return "August";
    	case 9:return "September";
    	case 10:return "October";
    	case 11:return "November";
    	case 12:return "December";
    	default: break;	
    	}
    	return "Month";
    }
    
    public static String replaceService(ArrayList<String> arr, String s) {
        
    	for(int i = 0; i < arr.size(); i++) {
    		if(Pattern.matches(s.toLowerCase()+"[.]html[.]link.*", arr.get(i))) {
    			return arr.get(i).substring(arr.get(i).indexOf("\"")+1, arr.get(i).length()-1);
    		}
    	}
    	return s;        
    }
    

    public static String replaceDay(String d, String m) {
    	if(Pattern.matches(".\\d\\d",d)) {
    		return (m.substring(0,3)+ " " + Integer.parseInt(d.substring(1)));
    	}
    	else {
   	      if(d.equals("dHF") && m.equals("July")) {return "Jul 13-19: Sunday of the Holy Fathers";} //July version
   	      else if(d.equals("dHF") && m.equals("October")) {return "Oct 11-17: Sunday of the Holy Fathers";} //October version
   	      else if(d.equals("dFF")) {return "Dec 11-17: Sunday of the Forefathers";}
   	      else if(d.equals("dBC")) {return "Dec 18-24: Sunday Before Christmas";}
   	      else if(d.equals("dAC")) {return "Sunday After Christmas";}
   	      return m;
    	}
    }
    
    public static String tabs(int n) {
    	String build = "";
    	for(int i = 0 ; i< n ; i++) {
    		build += "\t";
    	}
    	return build;
    }

}

