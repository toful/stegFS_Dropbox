//package stegfs_dropbox;



import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
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

/**
 * Metadata storage
 * Stores metaStorage layers objects in a hashmap: key[SHA256(password], value[metaStorageLayer]
 */
public class storage implements java.io.Serializable {
	
	
	private static final long serialVersionUID = 1L;
	
	// Hashmap to hold metaStorage layers
	static Map<String, metaStorageLayer> storage = new ConcurrentHashMap<String, metaStorageLayer>();
	
	
	storage(){
		Map<String, metaStorageLayer> storage = new HashMap<String, metaStorageLayer>();
		this.storage = storage;
	}
	
	
	 /**
     * Get the metadata storage layer corresponding to a hashed password
     * @return metadata
     */
	public static metaStorageLayer get(String hash) {
		return  storage.get(hash);
		
		
	}
	
	/**
     * Permanently erase the full storage
	 * Empty storage is then saved to disk
     */
	public static void erase() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IOException {
		storage = new ConcurrentHashMap<String, metaStorageLayer>();
		saveEncrypted("/mnt/share/storage.db");
		
	}
	
	
	 /**
	 * Encrypt (AES) and save  storage to disk
	 * 
	 * @param path where the file should be stored
	*/
public static void saveEncrypted(String path) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
	 System.out.println("Saving encrypted metadata storage to " + path);
    
	 // encrypt the metaStorage
	 FileOutputStream fileOut = new FileOutputStream(path);
	 encrypt((Serializable) storage, fileOut);
}



/**
	 * AES encryption
	 * 
	*/
public static void encrypt(Serializable storage, OutputStream ostream) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {

	byte[] key = mainApp.authToken.substring(0, 32).getBytes(); // to match the max AES key length of 32 bytes
	String algorithm = "AES";
	
	try {
    
    SecretKeySpec sks = new SecretKeySpec(key, algorithm);

    // AES ECB mode with PKCS5
    Cipher cipher = Cipher.getInstance(algorithm);
    cipher.init(Cipher.ENCRYPT_MODE, sks);
    SealedObject sealedObject = new SealedObject(storage, cipher);

    // write to disk
    CipherOutputStream cos = new CipherOutputStream(ostream, cipher);
    ObjectOutputStream outputStream = new ObjectOutputStream(cos);
    outputStream.writeObject(sealedObject);
    outputStream.close();

	} catch (IllegalBlockSizeException e) {
    e.printStackTrace();
}
}



/**
	 * Read and decrypt (AES) storage from disk
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
   

   
   try {
   	 //write decrypted input to metaStorage
       sealedObject = (SealedObject) inputStream.readObject();
       storage = (Map<String, metaStorageLayer>) sealedObject.getObject(cipher);
       inputStream.close();
       System.out.println("Loading and decrypting metadata storage done");
   
  
   } catch (IllegalBlockSizeException e) {
       e.printStackTrace();
     
   }
  
}

}