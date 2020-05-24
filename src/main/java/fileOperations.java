import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.NoSuchPaddingException;

public class fileOperations {
	
	
	
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
		//	System.out.println("STEGDROP - Found " + files.length + " new file(s)");
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
	  
	  @param file The path to the directory
	 * 
	*/
	public static void processFile(File file) throws Exception {
		
		// check if a file is already stored in metaStorage, add if not
		metaStorage.loadDecrypt(mainApp.file_stegMetaStorage );
		
		if (metaStorage.contains(file.getName())){
		
		}
		else {
			// generate a random salt, add it to the metadata store, encrypt the metadata store , generate a per-file authenticator from (authToken XOR salt), then write the file to stegFS
			String salt = Auth.getRandomSalt();
			metaStorage.add(file.getName(), new metadata(salt));
			System.out.println("STEGDROP - file " + file.getName() + " added to metadata storage");
			
			// update metadata storage to disk
			metaStorage.saveEncrypted(mainApp.file_stegMetaStorage );
			
			//write the file to stegfs
			//stegfs write filename:
			
			String passPerFile = Auth.calcPassPerFile(mainApp.authToken, salt);
			callBash.writeToStegFS(file.getName() + ":" + mainApp.authToken); //TODO: change to passperfile (static token used for testing)
			System.out.println("STEGDROP - file" + file.getName() + " saved to steganographic storage. Secret: " + passPerFile );
			System.out.println();
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
	public static void importFromStegFs() throws InvalidKeyException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, IOException {
		
		// decrypt the metadata storage and get a list of all files
		System.out.println("STEGDROP - Decrypting metadata storage (" + mainApp.file_stegMetaStorage + ")" );
		metaStorage.loadDecrypt(mainApp.file_stegMetaStorage );
		
		System.out.println("STEGDROP - Reading all files from steganographic storage \n");
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
			callBash.readFromStegFS(filename + ":" + mainApp.authToken);
			
		}
		System.out.println("\n");
		
	}

}
