import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URL;
import java.util.Scanner;

/**
 * GUI for the StegFS functionalities implemented
 *
 */


public class StegFS_GUI_init extends JFrame{

    static String googleAuth_2AF = "/home/toful/Dropbox/Uni/Master/PrivacyProtection/stegFS_Dropbox/test/GA_2AF_SK.key";

    public static void main(String args[]){
        StegFS_GUI_init frame = new StegFS_GUI_init( );
        frame.setVisible(true);
    }

    /**
     * Log in/Sign in frame
     */
    public StegFS_GUI_init(){
        setTitle("StegFS");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300,200);

        JPanel p1 = new JPanel();
        JLabel label = new JLabel("Welcome to StegFS GUI");
        p1.add( label );
        p1.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        JPanel p2 = new JPanel();
        JLabel label2 = new JLabel("Input Google Auth token:");
        JTextField text = new JPasswordField(6);
        p2.add( label2 );
        p2.add( text );
        p2.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        JPanel p3 = new JPanel();
        JButton button1 = new JButton("Log in");
        JButton button2 = new JButton("Sign in");
        p3.add(button1);
        p3.add(button2);

        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String secretKey="";
                try(BufferedReader bufferedReader = new BufferedReader(new FileReader( googleAuth_2AF ))) {
                    secretKey = bufferedReader.readLine();
                } catch (FileNotFoundException ex) {
                    // Exception handling
                } catch (IOException ex) {
                    // Exception handling
                }
                int code ;
                try {
                    code = Integer.parseInt(text.getText());
                    if( !Auth.validate2AF(code, secretKey) ){
                        JOptionPane.showMessageDialog(new JFrame(), "Incorrect Token ", "ERROR",
                                JOptionPane.ERROR_MESSAGE);
                        //System.exit(0);
                    }
                    else {
                        dispose();
                        mainApp f = new mainApp();
                        f.setVisible(true);
                    }
                }
                catch (NumberFormatException ex){}
            }
        });

        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Check if file already exists?
                try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter( googleAuth_2AF ))) {
                    GoogleAuthenticatorKey secretKey = Auth.generate2AF();
                    GoogleAuthenticatorQRGenerator qr = new GoogleAuthenticatorQRGenerator();
                    ImageIcon icon = new ImageIcon( ImageIO.read(new URL( qr.getOtpAuthURL("SegoFS", "StefoFS_User", secretKey) )));
                    JLabel lbl = new JLabel( "Scan the QR or copy the following code in your Google Authenticator app: " + secretKey.getKey(), icon, JLabel.CENTER );
                    lbl.setVerticalTextPosition(JLabel.TOP);
                    lbl.setHorizontalTextPosition(JLabel.CENTER);
                    JOptionPane.showMessageDialog(null, lbl, "Google Auth Token",
                            JOptionPane.PLAIN_MESSAGE, null);
                    bufferedWriter.write(secretKey.getKey());
                } catch (IOException ex) {
                    // Exception handling
                }
            }
        });

        getContentPane().add(BorderLayout.NORTH, p1);
        getContentPane().add(BorderLayout.CENTER, p2);
        getContentPane().add(BorderLayout.SOUTH, p3);
        //setVisible(true);
    }

}
