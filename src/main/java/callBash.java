// package stegfs_dropbox;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Relay to call bash commands
 * 
 */

public class callBash {
	
	   
	
	/** Issue a string of commands to the bash
     */
    public static ArrayList<String> command(final String cmdline) {
        try {
            Process process = 
                new ProcessBuilder(new String[] {"bash", "-c", cmdline})
                    .redirectErrorStream(true)
                    .start();

            ArrayList<String> output = new ArrayList<String>();
            BufferedReader br = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
            String line = null;
            while ( (line = br.readLine()) != null )
                output.add(line);

            // return if something was done
            if (0 != process.waitFor())
                return null;

            return output;

        } catch (Exception e) {
          System.out.println("Error");

            return null;
        }
    }


    
    /**
     * Create a RAM disk, mounted to StegDrop folder
     * 
     */
	public static void createRamDisk() {
			
			// create a ramdisk and mount it to the stegdrop folder
			command("mount -t ramfs ramfs " + mainApp.StegDropFolder);
		}
	
	
	 /**
     * Erase content on the ram disk using the Gutmann method
     * BE CAREFUL, IT FULLY DESTROYS stegFolder ;-)
     */
	public static void destroyRamDisk() {
			
			// Gutmann file erase: find all content on /mnt/ramdisk, force to write 50 times randomly, then all zeros
			// http://manpages.ubuntu.com/manpages/trusty/man1/shred.1.html
			command("find " + mainApp.StegDropFolder + " -type f -print0 | xargs -0 shred -fuz n- 50 -u;"
					+ "rm -r " + mainApp.StegDropFolder +"/*"); // remove empty sub-directories
		}
	
	
	 /**
     * Write a file to steganographic filesystem
     * file_auth must be in the format of String (file:authentication)
     */
	public static void writeToStegFS(String file_auth) {
			
			String filename = file_auth.split(":")[0];
			command("cp " + mainApp.StegDropFolder + filename + " " + mainApp.stegFSPartition + file_auth);
		//	System.out.println("Write to stegfs: " + file_auth);
				
		}
	
	
	/**
     * Read a file from steganographic filesystem
     * file_auth must be in the format of String (file:authentication)
     */
	public static void readFromStegFS(String file_auth) {
		
			String filename = file_auth.split(":")[0];
			command("cp " + mainApp.stegFSPartition + file_auth + " "+ mainApp.StegDropFolder + filename);
			System.out.println("STEGDROP - " + filename + " \t \t fetched from steganographic file system and dropped to " + mainApp.StegDropFolder);
				
		}
	
	

	

	
	 
	


}
