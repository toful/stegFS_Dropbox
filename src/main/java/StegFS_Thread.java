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
        while (1==1) { // TODO: change to while(keyfile pendrive is connected) to implement kill-switch functionality

            try {
                fileOperations.scanDirectory(StegFS_GUI.stegFolder);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}
