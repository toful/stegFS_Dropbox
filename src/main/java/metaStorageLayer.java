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
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;


/**
 * Layered metadata storage
 * Stores metadata objects in a hashmap: key[filename], value[metadata]
 */
public class metaStorageLayer implements java.io.Serializable {
	
	
	private static final long serialVersionUID = 1L;
	
	// Hashmap to hold metadata objects
	static Map<String, metadata> metaStorage = new ConcurrentHashMap<String, metadata>();
	
	
	metaStorageLayer(){
		Map<String, metadata> metaStorage = new HashMap<String, metadata>();
		this.metaStorage = metaStorage;
	}
	
	
	
	/**
     * Get the full metadata of a layer
     * @return metaStorage
     */
    public static List<String> getAllFiles() {
    	List<String> list = new ArrayList<String>(metaStorage.keySet());
            return list;
    }
	


    /**
     * Write to metadata storage layer at key [filename], value: metadata
     * @return database
     */
	public static void add (String filename, metadata data) {
		metaStorage.put(filename, data);
		
	}
	
	
	/**
     * Get the salt of a specific file from a metadata storage layer
     * @return salt
     */
	public static String getSalt(String filename) {
		
		String salt = metaStorage.get(filename).getSalt();
	
		return salt;
	}

	
	
	  /**
     * Check if a key (filename) is present in a metadata storage layer
     * @return containsKey
     */
	public static boolean contains(String filename) {
		if (metaStorage.containsKey(filename)){
			return true;}
		else 
			{return false;}	
	}
	
	 /**
     * Get metadata to a corresponding filename
     * @return metadata
     */
	public static metadata get(String filename) {
		return  metaStorage.get(filename);
		
		
	}
	 

	
	/**
     * Remove a single dataset from metadata storage layer
     *
     */
	public static void delete(String filename) {
		metaStorage.remove(filename);
		
	}
	
	
	
	
	
	




}

