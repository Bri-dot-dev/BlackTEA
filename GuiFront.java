package Brian.TeaCBC;

import Brian.TeaCBC.GuiFront;
import javax.swing.ImageIcon;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.Random;
import java.util.Arrays;

import static javax.swing.JPanel.*;

public class GuiFront {


    public static void main(String[] args) {

        UIManager.put("OptionPane.okButtonText", "Continue");
        // Application variables
        Random rand = new Random();
        rand.setSeed(1);
        int [] IV = {rand.nextInt(), rand.nextInt()};
        System.out.println("IV = " + Arrays.toString(IV));
        String[] inputFileStr = new String[1];
        String[] hashStr = new String[1];
        //Code for custom splash screen
        var splash = new SplashScreen();
        splash.show(2000);
        splash.hide();
        // Window layout
        JPanel panel = new JPanel();
        JFrame frame = new JFrame("Black TEA File Protection");
        JOptionPane.showMessageDialog(frame,
                        "Welcome to Black TEA v1.0.5",
                        "Welcome",
                        JOptionPane.PLAIN_MESSAGE);
        frame.setLocationRelativeTo(frame);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Black TEA v1.0.5", TitledBorder.LEFT,
                TitledBorder.TOP));
        frame.setSize(500, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel);
        panel.setLayout(null);

        // Instruction menu format
        JLabel labelStart = new JLabel("To start, select file for encryption/decryption, then enter password.");
        labelStart.setBounds(10, 15, 500, 25);
        panel.add(labelStart);
        Font f = new Font("Sanserif", Font.ITALIC, 12);
        labelStart.setFont (f);

        // Add File Button Properties
        JLabel label = new JLabel("Selected file: ");
        label.setBounds(10, 50, 265, 25);
        panel.add(label);
        JButton buttonAddFile = new JButton("Choose file");
        buttonAddFile.setBounds(280, 50, 105, 25);
        panel.add(buttonAddFile);

        // Add File Action Functions
        buttonAddFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Open File");
                int option = fileChooser.showOpenDialog(frame);
                if(option == JFileChooser.APPROVE_OPTION){
                    File file = fileChooser.getSelectedFile();
                    JOptionPane.showMessageDialog(frame,
                            "File '" + file.getName() + "' chosen",
                            "Black TEA",
                            JOptionPane.PLAIN_MESSAGE);
                    System.out.println("file path: " + file.getPath());
                    label.setText("File Selected:     " + file.getName());
                    // Set input file path variable
                    try {
                        inputFileStr[0] = fileToStr(file.getPath());
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }else{
                    label.setText("Open command canceled");
                }
            }
        });
        //Generate Hash Button / User Input Text Box Properties
        JLabel labelFile = new JLabel("Enter password to be hashed:");
        labelFile.setBounds(10, 100, 225, 25);
        panel.add(labelFile);

        JTextField userText = new JTextField(20);
        userText.setBounds(235, 100, 150, 25);
        panel.add(userText);

        JButton buttonHash = new JButton("Generate MD5");
        buttonHash.setBounds(235, 130, 150, 25 );
        panel.add(buttonHash);

        //Generate Hash Action Function
        buttonHash.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent eventHash) {
                String button_text = userText.getText();
                MD5 md5Test = new MD5();
                // System.out.println("button_text : " + button_text);
                String theHash = md5Test.getMd5(button_text);
                hashStr[0] = theHash;
                JOptionPane.showMessageDialog(frame,
                        "File password set to " + "'" + userText.getText() + "'",
                        "Black TEA",
                        JOptionPane.PLAIN_MESSAGE);
            }
        });
        
        // Encrypt file button
        JButton encryptButton = new JButton("Encrypt file");
        encryptButton.setBounds(235, 180, 150,25 );
        panel.add(encryptButton);

        //Encrypt Action Function
        encryptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent encrypt) {
                if ((inputFileStr[0] == null) || (hashStr[0] == null)) {
                    JOptionPane.showMessageDialog(panel, "Make sure file is selected and password is set",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    throw new IllegalArgumentException("File and hash must first be set");
                }

                TeaCBCMode teaCbcMode = new TeaCBCMode();
                teaCbcMode.setKey(hashStr[0]);
                String encryptedFileStr = teaCbcMode.stringEncrypt(inputFileStr[0], IV);

                // Write to file
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Choose where to save encrypted file");
                fileChooser.setApproveButtonText("Save");
                int option = fileChooser.showOpenDialog(frame);
                if(option == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    try {
                        writeToFile(encryptedFileStr, file.getPath());
                    } catch (IOException ioe){
                        ioe.printStackTrace();
                    }

                    // Show pop up for confirmation of file saved
                    JOptionPane.showMessageDialog(frame, "Encrypted file saved to " + file.getPath());
                }
            }

        });

        // Decrypt file button
        JButton decryptButton = new JButton("Decrypt file");
        decryptButton.setBounds(235, 210, 150,25 );
        panel.add(decryptButton);

        // Decrypt Action Function
        decryptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent decrypt) {
                if ((inputFileStr[0] == null) || (hashStr[0] == null)) {
                    JOptionPane.showMessageDialog(panel, "Make sure file is selected and password is set",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    throw new IllegalArgumentException("File and hash must be set first!");
                }

                TeaCBCMode teaCbcMode = new TeaCBCMode();
                teaCbcMode.setKey(hashStr[0]);
                String decryptFileStr = teaCbcMode.stringDecrypt(inputFileStr[0], IV);

                // Write to file
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Choose where to save decrypted file");
                fileChooser.setApproveButtonText("Save");
                int option = fileChooser.showOpenDialog(frame);
                if(option == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    try {
                        writeToFile(decryptFileStr, file.getPath());
                    } catch (IOException ioe){
                        ioe.printStackTrace();
                    }

                    // Show pop up for confirmation of file saved
                    JOptionPane.showMessageDialog(frame, "Decrypted file saved to " + file.getPath());
                }
            }
        });

        // Set frame as visible
        frame.setVisible(true);
    }

    // Converts input file to string format using UTF8 char format
    private static String fileToStr(String path) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF8"));
        String fileStr = "";
        int r;
        while ((r = reader.read()) != -1) {
            fileStr += (char) r;
        }
        return fileStr;
    }

    // Writes input data to file using the same UTF8 format
    private static void writeToFile(String str, String fileName) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8));
        writer.write(str);
        writer.close();
    }
// Code here and below for setting splash screen
    public static class SplashScreen {
        private final JWindow window;
        private long startTime;
        private int minimumMilliseconds;

        public SplashScreen() {
            window = new JWindow();
            var image = new ImageIcon("G:\\Classes\\CCSU Spring 20\\CS492\\TeaCBCFinalProject\\splashscreen.png");
            window.getContentPane().add(new JLabel("", image, SwingConstants.CENTER));
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            window.setBounds((int) ((screenSize.getWidth() - image.getIconWidth()) / 2),
                    (int) ((screenSize.getHeight() - image.getIconHeight()) / 2),
                    image.getIconWidth(), image.getIconHeight());
        }

        public void show(int minimumMilliseconds) {
            this.minimumMilliseconds = minimumMilliseconds;
            window.setVisible(true);
            startTime = System.currentTimeMillis();
        }

        public void hide() {
            long elapsedTime = System.currentTimeMillis() - startTime;
            try {
                Thread.sleep(Math.max(minimumMilliseconds - elapsedTime, 0));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            window.setVisible(false);
        }
    }
}
