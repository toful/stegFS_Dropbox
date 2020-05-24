import javax.crypto.BadPaddingException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;


public class StegFS_GUI extends JFrame{

    static String file_2AF = "file:///home/toful/Dropbox/Uni/Master/PrivacyProtection/stegFS_Dropbox/test/keyfile.txt";
    static String file_stegMetaStorage = "/home/toful/Dropbox/Uni/Master/PrivacyProtection/stegFS_Dropbox/test/metaStorage.db";
    static String stegFolder = "/home/toful/Dropbox/Uni/Master/PrivacyProtection/stegFS_Dropbox/test/stegdrop/";
    static String googleAuth_2AF = "/home/toful/Dropbox/Uni/Master/PrivacyProtection/stegFS_Dropbox/test/GA_2AF_SK.key";
    static String stegFSPartition = "/mnt/stegfs-2/"; // adjust this to your partition

    static String authToken ="";
    static int currentLayer = 0;
    static  String accessTokenL1 = "1"; //TODO: generate access token from user input + auth token
    static  String accessTokenL2 = "2";


    /*public static void main(String args[])  throws Exception {
        StegFS_GUI frame = new StegFS_GUI( );
        frame.setVisible(true);

    }*/

    public StegFS_GUI(){
        setTitle( "StegFS" );
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600,600);
        getContentPane().setLayout(null);

        JPanel p1 = new JPanel();
        p1.setBounds(0,0,600,35);
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
        p2.setBounds(0,35,600,35);
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
        p3.setBounds(0,70,600,35);
        JLabel l3 = new JLabel("User's password:");
        JTextField t3 = new JPasswordField(37);
        p3.add( l3 );
        p3.add( t3 );

        JPanel p4 = new JPanel();
        p4.setBounds(0,105,600,35);
        JButton b3 = new JButton("Run StegDrop");
        p4.add( b3 );

        b3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    file_2AF = "file://" + t1.getText();
                    file_stegMetaStorage = t2.getText();
                    // key-file authentication
                    URI uri = new URI( file_2AF );
                    String key = Auth.getKeyFile( uri );
                    // 2FA authentication
                    authToken = Auth.calculateAuthToken( t3.getText() ,key);



                    runStegFS();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });

        JPanel p5 = new JPanel();
        p5.setBounds(0,140,600,400);
        JTextArea textArea = new JTextArea(50, 30);
        PrintStream printStream = new PrintStream(new CustomOutputStream(textArea));
        System.setOut(printStream);
        System.setErr(printStream);
        p5.add( textArea );


        getContentPane().add(p1);
        getContentPane().add(p2);
        getContentPane().add(p3);
        getContentPane().add(p4);
        getContentPane().add(p5);
    }

    public void runStegFS() throws Exception {
        ///////////////////////////////Temporal
        mainApp.authToken = authToken;
        mainApp.file_2AF = file_2AF;
        mainApp.file_stegMetaStorage = file_stegMetaStorage;
        mainApp.stegFolder = stegFolder;
        mainApp.googleAuth_2AF = googleAuth_2AF;
        mainApp.stegFSPartition = stegFSPartition; // adjust this to your partition
        ////////////////////////////////
        System.out.println( "System is running");
        new StegFS_Thread().start();
    }


}
