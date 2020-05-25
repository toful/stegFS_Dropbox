import javax.crypto.BadPaddingException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

public class StegFS_Thread extends Thread{

    public StegFS_Thread() throws Exception {

    }

    public void run() {
    	
    	//create RAM disk
    	//callBash.createRamDisk();
    	
    	// switch to the requested sub-layer
    	try {
 			metaStorage.switchLayer(mainApp.layerAuth);
 		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | ClassNotFoundException
 				| BadPaddingException | IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
    	
    	// fetch all files from steganographic file system
        try {
            fileOperations.importFromStegFs();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
       

        // StegDrop daemon
        while (fileOperations.heartbeat(mainApp.keyFile) == true) {

            try {
                fileOperations.scanDirectory(mainApp.StegDropFolder);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // self destroy
        try {
			fileOperations.suicide();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }


}
