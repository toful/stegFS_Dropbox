import javax.crypto.BadPaddingException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import javax.swing.text.DefaultCaret;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;


public class mainApp extends JFrame{

	 /*
    static String keyFile = "file:///home/toful/Dropbox/Uni/Master/PrivacyProtection/stegFS_Dropbox/test/keyfile.txt";
    static String metaStorageDB = "/home/toful/Dropbox/Uni/Master/PrivacyProtection/stegFS_Dropbox/test/metaStorage.db";
    static String StegDropFolder = "/home/toful/Dropbox/Uni/Master/PrivacyProtection/stegFS_Dropbox/test/stegdrop/";
    static String googleAuthKey = "/home/toful/Dropbox/Uni/Master/PrivacyProtection/stegFS_Dropbox/test/GA_2AF_SK.key";
    static String stegFSPartition = "/mnt/stegfs-2/"; // adjust this to your partition
    */
   

	static String keyFile = "/media/privacyprotection/PENDRIVE/key.file";
	static String metaStorageDB = "/media/privacyprotection/PENDRIVE/metaStorage.db";
	static String StegDropFolder = "/mnt/StegDrop/";
	static String googleAuthKey = "//media/privacyprotection/PENDRIVE/GA_2AF_SK.key";
	static String stegFSPartition = "/mnt/stegfs-2/";	


    static String authToken ="";
    static String layerAuth;
    static int currentLayer = 0;
    static boolean terminate = false;
 


    public static void main(String args[])  throws Exception {
        mainApp frame = new mainApp( );
        frame.setVisible(true);

    }

    public mainApp(){
        setTitle( "StegDrop" );
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800,800);
        getContentPane().setLayout(null);

        JPanel p1 = new JPanel();
        p1.setBounds(0,0,800,35);
        JLabel l1 = new JLabel("Keyfile:");
        JButton b1 = new JButton("search");
        JTextField t1 = new JTextField(35);
        p1.add( l1 );
        p1.add( t1 );
        p1.add( b1 );

        b1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setCurrentDirectory( new java.io.File(".") );
                chooser.setDialogTitle("Select the keyfile path");
                if (chooser.showOpenDialog(chooser) == JFileChooser.APPROVE_OPTION) {
                    t1.setText( chooser.getSelectedFile().getAbsolutePath() );
                }
            }
        });

        JPanel p2 = new JPanel();
        p2.setBounds(0,35,800,35);
        JLabel l2 = new JLabel("Metadata Storage file:");
        JButton b2 = new JButton("search");
        JTextField t2 = new JTextField(25);
        p2.add( l2 );
        p2.add( t2 );
        p2.add( b2 );

        b2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setCurrentDirectory( new java.io.File(".") );
                chooser.setDialogTitle("Select the keyfile path");
                if (chooser.showOpenDialog(chooser) == JFileChooser.APPROVE_OPTION) {
                    t2.setText( chooser.getSelectedFile().getAbsolutePath() );
                }
            }
        });

        JPanel p3 = new JPanel();
        p3.setBounds(0,70,800,35);
        JLabel l3 = new JLabel("User password: ");
        JTextField t3 = new JPasswordField(37);
        p3.add( l3 );
        p3.add( t3 );
        
        JPanel p6 = new JPanel();
        p6.setBounds(0,105,800,35);
        JLabel l6 = new JLabel("Storage layer:   ");
        JTextField t6 = new JPasswordField(37);
        p6.add( l6 );
        p6.add( t6 );

        JPanel p4 = new JPanel();
        p4.setBounds(0,160,800,35);
        JButton b3 = new JButton("Run StegDrop");
        p4.add( b3 );
        

        b3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    keyFile = t1.getText();
                    String urikeyFile = "file://" + t1.getText();
                    metaStorageDB = t2.getText();
                    
                    // key-file authentication (2FA)
                    URI uri = new URI(urikeyFile);
                    String key = Auth.getKeyFile( uri );
                    
                    // get password
                    layerAuth = Auth.sha256(t6.getText());
                    String password = Auth.hashPassword(t3.getText());
   
                    // calculate authentication token
                    authToken = Auth.calculateAuthToken(password ,key);
                   
                    
                    runStegFS();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });
        
        
        
        JPanel p5 = new JPanel();
        p5.setBounds(0,200,800,35);
        JButton b5 = new JButton("Exit");
        p5.add( b5 );
        
        b5.addActionListener(new ActionListener() {
        	 public void actionPerformed(ActionEvent e) {
        		 terminate = true;
             }
       
        });

        
        JPanel p7 = new JPanel();
        p7.setBounds(0,300,800,600);
        JTextArea textArea = new JTextArea(45, 55);
        
        
        DefaultCaret caret = (DefaultCaret)textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    
        JScrollPane scrollpane = new JScrollPane(textArea);
        scrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        p7.add(scrollpane);
        
     
        /*
        PrintStream printStream = new PrintStream(new CustomOutputStream(textArea));
        System.setOut(printStream);
        System.setErr(printStream);
        */
        
        p7.add(textArea);


        getContentPane().add(p1);
        getContentPane().add(p2);
        getContentPane().add(p3);
        getContentPane().add(p4);
        getContentPane().add(p5);
        getContentPane().add(p6);
        getContentPane().add(p7);
    }

    public void runStegFS() throws Exception {
        
        System.out.println("STEGDROP - Starting");
        //metaStorage.erase(); // RESET
        new StegFS_Thread().start();
    }


}
