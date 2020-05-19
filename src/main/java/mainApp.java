//package stegfs_dropbox;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;

import stegfs_dropbox.Auth;
import stegfs_dropbox.callBash;
import stegfs_dropbox.metaStorage;

import java.io.*;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import javax.crypto.BadPaddingException;
import javax.crypto.NoSuchPaddingException;


public class mainApp {
	
	static String authToken ="";

	static String file_2AF = "file:///home/toful/Dropbox/Uni/Master/PrivacyProtection/stegFS_Dropbox/test/keyfile.txt";
	static String file_stegMetaStorage = "/home/toful/Dropbox/Uni/Master/PrivacyProtection/stegFS_Dropbox/test/metaStorage.db";
	static String stegFolder = "/home/toful/Dropbox/Uni/Master/PrivacyProtection/stegFS_Dropbox/test/stegdrop/";
	static String googleAuth_2AF = "/home/toful/Dropbox/Uni/Master/PrivacyProtection/stegFS_Dropbox/test/GA_2AF_SK.key";
	public static String stegFSPartition = "/mnt/stegfs-2/";

	
	/**
	 * Get all files that are present in a directory
	  @param input The path to the directory
	  @return An array holding all files of a specific type
	*/
	public static File[] getFiles(File input){
		// if input is a directory, return all files
		if (input.isDirectory()) {
            return input.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String filename) {
                	return filename.endsWith(".txt");
                }
            });
        }
        else {
            // else return an empty array
            return new File[0];
        }
	}
	
	
	/**
	 * Scan a directory for files
	  @param directory the path were to scan
	 * 
	*/
	public static void scanDirectory(String directory) throws Exception{
		
		File dropDirectory = new File (directory);
		File[] files = getFiles(dropDirectory);
		if(files.length!=0) {
			System.out.println("Found " + files.length + " files");
		}
		// loop through every file
		for (int i=0; i<files.length; i++) {
			//process file
			processFile(files[i]);
		}
	}
	
	
	/**
	 * Process a file
	 * check if it is already stored. 
	 * 		if yes: ignore. 
	 * 		if not: generate a random salt, add it to the metadata store, generate a per-file authenticator, then write the file to stegFS
	  
	  @param input The path to the directory
	 * 
	*/
	public static void processFile(File file) throws Exception {
		
		// check if a file is already stored in metaStorage, add if not
		metaStorage.loadDecrypt( file_stegMetaStorage );
		
		if (metaStorage.contains(file.getName())){
			System.out.println("already stored");
		}
		else {
			// generate a random salt, add it to the metadata store, encrypt the metadata store , generate a per-file authenticator from (authToken XOR salt), then write the file to stegFS
			String salt = Auth.getRandomSalt();
			metaStorage.add(file.getName(), new metadata(salt));
			System.out.println("file " + file.getName() + " added to storage");
			
			// update metadata storage to disk
			metaStorage.saveEncrypted( file_stegMetaStorage );
			
			//write the file to stegfs
			//stegfs write filename:
			
			String passPerFile = Auth.calcPassPerFile(authToken, salt);
			callBash.writeToStegFS(file.getName() + ":" + authToken); //TODO: change to passperfile (static token used for testing)
			System.out.println("Write to stegfs: " + file.getName() + ":" + passPerFile);
		}
	}
	
	
	/**
	 * Print a list of all files stored in the metadata storage
	 * 
	*/
	public static void printFiles(){
		List<String> listOfFiles = metaStorage.getAllFiles();
		System.out.println("List of files:");
		for (int i=0; i<listOfFiles.size(); i++) {
			System.out.println("Filename: " + listOfFiles.get(i));
		}
	}
	
	
	/**
	 * Get all files from the steganographic file system
	 * Used at launch in order to copy all files from stegFS to the DropSteg folder on ram-disk
	 * 
	*/
	public static void readAllFromFS() throws InvalidKeyException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, IOException {
		
		// decrypt the metadata storage and get a list of all files
		metaStorage.loadDecrypt("/mnt/share/metaStorage.db");
		List<String> listOfFiles = metaStorage.getAllFiles();
		
		// loop through the list of all files and fetch the metadata of each file
		for (int i=0; i<listOfFiles.size(); i++) {
			String filename = listOfFiles.get(i);
			
			/* TODO: change static authToken to token XOR salt
			String salt = metaStorage.getSalt(filename);
			// only proceed if salt is available
			if (salt !=null) {
			String passPerFile = Auth.calcPassPerFile(authToken, salt);
			*/
			
			// get file from stegFS and write it to the stegdrop folder
			callBash.readFromStegFS(filename + ":" + authToken);
			
		}
		
	}
	
	
	
	
	public static void main(String[] args) throws Exception {

		// AUTHENTICATION
		// password authentication
		//String password = Auth.getPassword();
		String password = "f0e4c2f76c58916ec258f246851bea091d14d4247a2fc3e18694461b1816e13b"; //fixed password for testing
		
		// key-file authentication
		URI uri = new URI( file_2AF );
		String key = Auth.getKeyFile( uri );
		
		// 2FA authentication
		authToken = Auth.calculateAuthToken(password ,key);
		

		// 2FA authentication using Google Authenticator
		//
		//This section generates and check the GoogleAuth 2AF.
		//////////////////////////////////////////////////////////////////////////////////////////////////////
		String secretKey = "";
		File f = new File( googleAuth_2AF );
		if( !f.isFile() ) {
			try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter( googleAuth_2AF ))) {
				String fileContent = Auth.generate2AF();
				bufferedWriter.write(fileContent);
			} catch (IOException e) {
				// Exception handling
			}
		}
		try(BufferedReader bufferedReader = new BufferedReader(new FileReader( googleAuth_2AF ))) {
			secretKey = bufferedReader.readLine();
		} catch (FileNotFoundException e) {
			// Exception handling
		} catch (IOException e) {
			// Exception handling
		}
		System.out.println("Please, indicate the code generated by google Auth app:\n");
		Scanner in = new Scanner(System.in);
		int code = in.nextInt();
		if( !Auth.validate2AF(code, secretKey) ){
			System.exit(0);
		}
		System.out.println("2AF is correct");
		///////////////////////////////////////////////////////////////////////////////////////////////////////
		
		// STORAGE
		// scan drop-directory regularly, add new files to metadata storage, ignore existing files
		
		
		// metaStorage.erase(); // erase storage for testing
		// createRamDisk(); // creates a ramdisk
		// readAllFromFS(); // reads all files from stegfs
		
		while (1==1) { // daemon to run in background, TODO: change to while(keyfile pendrive is connected)
			scanDirectory(stegFolder);
			TimeUnit.SECONDS.sleep(10);
		}
	
	}
		
		
		

}
