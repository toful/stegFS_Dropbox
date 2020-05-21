//package stegfs_dropbox;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.spec.SecretKeySpec;

import stegfs_dropbox.metadata;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;


/**
 * Metadata storage
 * Stores metadata objects in a hashmap: key[filename], value[metadata]
 */
public class metaStorage implements java.io.Serializable {
	
	
	private static final long serialVersionUID = 1L;
	
	// Main metadata storage to hold metadata objects
	static Map<String, metadata> metaStorage = new HashMap<String, metadata>();
	
	// Sub-layer metadata storages
	static Map<String, metadata> layer0Storage = new HashMap<String, metadata>();
	static Map<String, metadata> layer1Storage = new HashMap<String, metadata>();
	static Map<String, metadata> layer2Storage = new HashMap<String, metadata>();
	
	
	metaStorage(){
		Map<String, metadata> metaStorage = new HashMap<String, metadata>();
		this.metaStorage = metaStorage;
	}
	
	
	
	/**
     * Get the full metadata storage
     * @return metaStorage
     */
    public static List<String> getAllFiles() {
    	List<String> list = new ArrayList<String>(metaStorage.keySet());
            return list;
    }


    /**
     * Write to metadata storage at key [filename], value: metadata
     * @return database
     */
	public static void add (String filename, metadata data) {
		metaStorage.put(filename, data);
	}

	
	/**
     * Get the salt of a specific file from metadata storage
     * @return salt
     */
	public static String getSalt(String filename) {
		
		String salt = metaStorage.get(filename).getSalt();
	
		return salt;
	}
	
	
	/**
     	* Check if a key (filename) is present in the metadata storage
     	* @return containsKey
     	*/
	public static boolean contains(String filename) {
		if (metaStorage.containsKey(filename)){
			return true;
		}
		else{
			return false;
		}
	}
	 

	/**
     	* Get metadata to a corresponding filename
     	* @return metadata
     	*/
	public static metadata get(String filename) {
		return  metaStorage.get(filename);
		
	}
	
	
	/**
     	* Remove a single dataset from metadata storage
     	*
     	*/
	public static void delete(String filename) {
		metaStorage.remove(filename);
	}
	
	
	/**
     	* Permanently erase the full metadata storage
     	* Empty storage is then saved to disk
    	*/
	public static void erase() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IOException {
		metaStorage = new HashMap<String, metadata>();
		//saveEncrypted("C:/KEYSTORE/metaStorage.db");
		saveEncrypted( mainApp.file_stegMetaStorage );
	}
	
	
	/**
	* Switch storage layers by linking a sub-layer to metaStorage
	*
	*/
	public static void switchLayer(String accessToken) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IOException, ClassNotFoundException, BadPaddingException {
		
		loadDecrypt(mainApp.file_stegMetaStorage);
		String accessTokenL1 = mainApp.accessTokenL1;
		
		// switch to layer 1
		if (accessToken.equals(mainApp.accessTokenL1)) {
			layer0Storage = metaStorage;
			metaStorage = layer1Storage;
			System.out.println("Switched to layer 1");
			saveEncrypted( mainApp.file_stegMetaStorage );
		}
		// switch to layer 2
		else if (accessToken.equals(mainApp.accessTokenL2)) {
			layer0Storage = metaStorage;
			metaStorage = layer2Storage;
			System.out.println("Switched to layer 2");
			saveEncrypted( mainApp.file_stegMetaStorage );
		}
		
		// switch to default layer 0
		else {
			metaStorage = layer0Storage;
			System.out.println("Switched to layer 0");
			saveEncrypted( mainApp.file_stegMetaStorage );
		}
	}
	
	 /**
	 * Encrypt (AES) and save the metadata storage to disk
	 * 
	 * @param path where the file should be stored
	*/
	 public static void saveEncrypted(String path) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
		 System.out.println("Saving encrypted metadata storage to " + path);
		 // encrypt the metaStorage
		 FileOutputStream fileOut = new FileOutputStream(path);
		 encrypt( (Serializable) metaStorage, fileOut);
	 }
 
 
 	 /**
	 * Encrypt (AES) and save the metadata storage including all layers to disk
	 * 
	 * @param path where the file should be stored
	*/
	 public static void saveEncrypted(String path) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
		 System.out.println("Saving encrypted metadata storage to " + path);
		 // encrypt the metaStorage
		 FileOutputStream fileOut = new FileOutputStream(path);
		 encrypt( (Serializable) metaStorage, (Serializable) layer0Storage, (Serializable) layer1Storage, (Serializable) layer2Storage, fileOut);
	 }
 
 
 
 	/**
	 * AES encryption
	 * 
	*/
	public static void encrypt(Serializable metaStorage, Serializable layer0Storage, Serializable layer1Storage, Serializable layer2Storage, OutputStream ostream) 
			throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {

		byte[] key = mainApp.authToken.substring(0, 32).getBytes(); // to match the max AES key length of 32 bytes
		String algorithm = "AES";

		try {
			 SecretKeySpec sks = new SecretKeySpec(key, algorithm);

			 // AES ECB mode with PKCS5
			 Cipher cipher = Cipher.getInstance(algorithm);
			 cipher.init(Cipher.ENCRYPT_MODE, sks);
			 // encrypt metaStorage, layer0, layer1, layer2
			 SealedObject sealedObject = new SealedObject(metaStorage, cipher);
			 SealedObject sealedObject0 = new SealedObject(layer0Storage, cipher);
			 SealedObject sealedObject1 = new SealedObject(layer1Storage, cipher);
			 SealedObject sealedObject2= new SealedObject(layer2Storage, cipher);

			 // write to disk
			 CipherOutputStream cos = new CipherOutputStream(ostream, cipher);
			 ObjectOutputStream outputStream = new ObjectOutputStream(cos);
			 outputStream.writeObject(sealedObject);
			 outputStream.writeObject(sealedObject0);
			 outputStream.writeObject(sealedObject1);
			 outputStream.writeObject(sealedObject2);
			 outputStream.close();

		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
	 	}
	}


	 /**
	 * Read and decrypt (AES) the metadata storage from disk
	 *
	*/
	public static void loadDecrypt (String path) throws IOException, ClassNotFoundException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException {
		System.out.println("Loading and decrypting metadata storage from " + path);
		FileInputStream fileIn = new FileInputStream(path);// Read serial file.
		decrypt(fileIn);
	}


	/**
	 * AES decryption
	 * 
	*/
	@SuppressWarnings("unchecked")
	public static void decrypt(InputStream istream) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, ClassNotFoundException, BadPaddingException {

		byte[] key = mainApp.authToken.substring(0, 32).getBytes(); // to match the max AES key length of 32 bytes
		String algorithm = "AES";

		// AES ECB mode with PKCS5
		SecretKeySpec sks = new SecretKeySpec(key, algorithm);
		Cipher cipher = Cipher.getInstance(algorithm);
		cipher.init(Cipher.DECRYPT_MODE, sks);

		CipherInputStream cipherInputStream = new CipherInputStream(istream, cipher);
		ObjectInputStream inputStream = new ObjectInputStream(cipherInputStream);
		SealedObject sealedObject;
		SealedObject sealedObject0;
		SealedObject sealedObject1;
		SealedObject sealedObject2;

		try {
			//write decrypted input to metaStorage, layer0, layer1, layer2
			sealedObject = (SealedObject) inputStream.readObject();
			sealedObject0 = (SealedObject) inputStream.readObject();
			sealedObject1 = (SealedObject) inputStream.readObject();
			sealedObject2 = (SealedObject) inputStream.readObject();
			metaStorage = (Map<String, metadata>) sealedObject.getObject(cipher);
			layer0Storage = (Map<String, metadata>) sealedObject0.getObject(cipher);
			layer1Storage = (Map<String, metadata>) sealedObject1.getObject(cipher);
			layer2Storage = (Map<String, metadata>) sealedObject2.getObject(cipher);
			inputStream.close();
			System.out.println("Loading and decrypting metadata storage done");

		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		}
	}

}

