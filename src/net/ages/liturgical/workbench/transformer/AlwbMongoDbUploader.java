package net.ages.liturgical.workbench.transformer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

public class AlwbMongoDbUploader {
	/**
	 * User to set the following variables
	 */
	private static String mongoDbUserId = "mcolburn";
	private static String mongoDbPassword = "huwari";
	private static String database = "agesdev";
	private static String pathToLibrarySrcGen = "/Users/mac002/Documents/Workspaces/kepler-dsl/alwb/p1/runfs/net.ages.liturgical.workbench.library/src-gen";
	private static List<Exception> errors = new ArrayList<Exception>();
	
	/**
	 * Do not change the variables below
	 */
	private ServerAddress serverAddress;
    private MongoClient mongoClient;
    public DB db;
    private int port;
    private static final int localPort = 27017;
    public static boolean runStatic;
    private static final String localHost = "localhost";
    MongoCredential credential;
	
    public AlwbMongoDbUploader(String dbname) {
    		init(localHost,localPort,dbname,"","");
    }
	public AlwbMongoDbUploader(String address, int port, String dbname, String userid, String password) {
		init(address,port,dbname, userid,password);
	}
	
	private void init(String address, int port, String dbname, String userid, String password) {
		try {
			serverAddress = setServerAddress(address, port);
			credential = MongoCredential.createMongoCRCredential(userid, dbname, password.toCharArray());
			mongoClient = setClient(serverAddress, credential);
			db = mongoClient.getDB(dbname);
		} catch (Exception e) {
			e.printStackTrace();
			errors.add(e);
		}		
	}
	public ServerAddress setServerAddress(String address, int port) {
		try {
			return new ServerAddress(address,port);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			errors.add(e);
			return null;
		}
	}
	
	public MongoClient setClient(ServerAddress address) {
		try {
			return new MongoClient(address);
		} catch (Exception e) {
			e.printStackTrace();
			errors.add(e);
			return null;
		}
	}

	public MongoClient setClient(ServerAddress address, MongoCredential credential) {
		try {
			return new MongoClient(address,Arrays.asList(credential));
		} catch (Exception e) {
			e.printStackTrace();
			errors.add(e);
			return null;
		}
	}
	
	public void insert(String collection, BasicDBObject dbObject) {
		try {
	        DBCollection dbCollection = db.getCollection(collection);
	        dbCollection.insert(dbObject);
		} catch (Exception e) {
			e.printStackTrace();
			errors.add(e);
		}
	}
	
	public void insertList(String collection, BasicDBObject[] dbObjectList) {
		try {
	        DBCollection dbCollection = db.getCollection(collection);
	        dbCollection.insert(dbObjectList);
		} catch (Exception e) {
			e.printStackTrace();
			errors.add(e);
		}
	}

	public void dropCollection(String collection) {
		try {
	        DBCollection theCollection = db.getCollection(collection);
	        theCollection.drop();			
		} catch (Exception e) {
			e.printStackTrace();
			errors.add(e);
		}
	}
	
	/**
	 * Drop all collections in the database
	 */
	public void dropAllCollections() {
		for (String c : db.getCollectionNames()) {
			if (! c.contains("system")) {
				dropCollection(c);
			}
		}
	}
	
	public static void test() {
		AlwbMongoDbUploader.runStatic = true;
//		MongoDb myDb = new MongoDb("bigMac");
		AlwbMongoDbUploader myDb = new AlwbMongoDbUploader("ds045157.mongolab.com",45157,"agesdev", "mcolburn","huwari");
		
		DB theDb = myDb.db;

		// get a list of the collections in this database and print them out
        Set<String> collectionNames = myDb.db.getCollectionNames();
        for (final String s : collectionNames) {
            System.out.println(s);
        }
        
        String grGrCog = "gr.gr.cog";
        String subColl = ".da.d1";
        String enUsEmatins = "en.us.ematins";
        
        myDb.dropCollection(grGrCog+subColl);
        
        // make a document and insert it
        BasicDBObject doc = new BasicDBObject(
        				 "_id", "daVE_OnTheEveningBefore")
        		.append("text", "τῷ Σαββάτῳ τὸ Βράδυ");

        myDb.insert("gr.gr.cog.da.d1", doc);

        myDb.dropCollection(enUsEmatins+subColl);

        doc = new BasicDBObject(
				 "_id", "daVE_OnTheEveningBefore")
		.append("text", "on Saturday Evening");

        myDb.insert("en.us.ematins.da.d1", doc);
	}
	
	public void process(String collection, String keyValue) {
		try {
			String[] parts = keyValue.split("~");
			String key = parts[0];
			String value = (parts.length > 1 ? parts[1] : null);

			BasicDBObject doc = new BasicDBObject(
       				 "_id", key)
       				.append("text", value);

			this.insert(collection, doc);
			
		} catch (Exception e) {
			e.printStackTrace();
			errors.add(e);
		}
	}

	/**
	 * Creates a BasicDBObject by splitting the supplied keyvalue string
	 * @param keyValue
	 * @return
	 */
	public BasicDBObject toDbObject(String filePrefix, String keyValue) {
		try {
			String[] parts = keyValue.split("~");
			String key = parts[0];
			String value = (parts.length > 1 ? parts[1] : null);

			return new BasicDBObject(
       				 "_id", filePrefix + ":" + key)
       				.append("text", value);
		} catch (Exception e) {
			e.printStackTrace();
			errors.add(e);
			return null;
		}
	}

	/**
	 * Main method will read all the *.tsf files in the library src-gen folder and 
	 * use them to populate the MongoDb database.  Set the user parameters at the
	 * top of this file.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		List<File> filesToProcess = AlwbFileUtils.getFilesInDirectory(pathToLibrarySrcGen, "tsf");
		int nbrFiles = filesToProcess.size();
		int count = 0;
		System.out.println(nbrFiles + " files to process...");

		AlwbMongoDbUploader.runStatic = true;
		AlwbMongoDbUploader myDb = new AlwbMongoDbUploader("ds063177.mongolab.com",63177,database, mongoDbUserId,mongoDbPassword);		

		BufferedReader br = null;
		String line;
		String collection;
		String filePrefix;
		String[] fileParts;
		List<BasicDBObject> dbObjectList;

		myDb.dropAllCollections();

		for (File f: filesToProcess) {
			try {
				count++;
				fileParts = AlwbFileUtils.getAresFileParts(f.getName());
				collection = fileParts[1];
				filePrefix = fileParts[0].replaceAll("\\.", "|");
//				collection = AlwbFileUtils.aresFileToMongoDbCollectionName(f.getName());
				System.out.println("\tProcessing " + count + " of " + nbrFiles + ": " + f.getName());
				br = new BufferedReader(new FileReader(f));
				dbObjectList = new ArrayList<BasicDBObject>();
				try {
					while ((line = br.readLine()) != null) {
						dbObjectList.add(myDb.toDbObject(filePrefix,line));
					}
					myDb.insertList(collection, dbObjectList.toArray(new BasicDBObject[dbObjectList.size()]));
				} catch (IOException e) {
					e.printStackTrace();
					errors.add(e);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					br.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
		for (Exception e : errors) {
			System.out.println(e.getMessage());
		}
		System.out.println("Done...");
	}
}
