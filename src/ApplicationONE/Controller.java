package ApplicationONE;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Controller {

    private boolean rightPath = false;
    private boolean rightFormat = false;
    private int type = 0;
    private String path = null;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextArea statusOutput;

    @FXML
    private TextField filesPath;

    @FXML
    private ComboBox<String> formatType;

    @FXML
    private Button runFileFormatter;

    @FXML
    private Button setPath;

    @FXML
    void initialize() {

        setPath.setOnAction(event -> {
            path = getPath();
            filesPath.setText(path);
        });

        runFileFormatter.setOnAction(event -> {
            lockInterface();
            try {
                openSocketAndSendSetupData();
                connectToSocketAndGetFilesLocation();
            } catch (Exception e) {
                unlockInterface();
            }
        });

        formatType.getItems().addAll(
                "Remove all \'Space\'",
                "Make all words with \'d\' underline",
                "Make all words with \'a\' bold",
                "Delete all words with \'e\'",
                "If word starts with capital char make it underline"
        );
        formatType.setOnAction(event -> {
            type = getFileFormatCase();
        });

    }

    private String getPath() {
        String path = null;
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int response = fileChooser.showOpenDialog(null);
        if (response == JFileChooser.APPROVE_OPTION) {
            path = fileChooser.getSelectedFile().toString();
            rightPath = true;
        } else {
            rightPath = false;
        }
        checkRunButtonAccess();
        return path;
    }

    private void checkRunButtonAccess() {
        if (rightPath && rightFormat) {
            runFileFormatter.setDisable(false);
        } else {
            runFileFormatter.setDisable(true);
        }
    }

    private int getFileFormatCase() {
        int format = 0;
        switch (formatType.getValue()) {
            case "Remove all \'Space\'":
                format = 1;
                break;

            case "Make all words with \'d\' underline":
                format = 2;
                break;

            case "Make all words with \'a\' bold":
                format = 3;
                break;

            case "Delete all words with \'e\'":
                format = 4;
                break;

            case "If word starts with capital char make it underline":
                format = 5;
                break;
        }
        if (format == 0) {
            rightFormat = false;
        } else {
            rightFormat = true;
        }
        checkRunButtonAccess();
        return format;
    }

    private void lockInterface() {
        runFileFormatter.setDisable(true);
        setPath.setDisable(true);
        formatType.setDisable(true);
    }

    private void unlockInterface() {
        runFileFormatter.setDisable(false);
        setPath.setDisable(false);
        formatType.setDisable(false);
    }

    private void openSocketAndSendSetupData() throws Exception {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(4567);
        } catch (IOException e) {
            statusOutput.appendText("ERROR: Could not listen on port: 4567");
//            System.exit(-1);
            return;
        }

        Socket clientSocket = null;
        try {
            runSecondApp();
            clientSocket = serverSocket.accept();
        } catch (IOException e) {
            statusOutput.appendText("ERROR: Accept socket failed: 4567.");
//            System.exit(-1);
            return;
        }


        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String inputLine, outputLine;

        out.println("Trying to launch second application...");

        while ((inputLine = in.readLine()) != null) {
            if (inputLine.equals("type")) {
                out.println(type);
            } else if (inputLine.equals("path")) {
                out.println(path);
            } else if (inputLine.equals("quit")) {
                out.println(inputLine);
                break;
            } else {
                out.println("response");
                statusOutput.appendText(inputLine);
            }
        }

        out.close();
        in.close();
        serverSocket.close();
        clientSocket.close();
    }

    private void connectToSocketAndGetFilesLocation() throws Exception {
        Socket socket = new Socket("localhost", 4568);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String fromServer;

        while ((fromServer = in.readLine()) != null) {
            if (fromServer.equals("quit")) {
                out.println("quit");
                break;
            }
            statusOutput.appendText(fromServer + "\r\n");
            out.println("");
        }

        unlockInterface();
        out.close();
        in.close();
        socket.close();
    }

    private void runSecondApp() throws Exception {
        File exeFile = new File("AppTwo.exe");
        File jarFile = new File("AppTwo.jar");
        File newExeFile;

        boolean isAppTwoExeFileExists = exeFile.exists();
        boolean isAppTwoJarFileExists = jarFile.exists();

        if (!isAppTwoExeFileExists && !isAppTwoJarFileExists) {
            JOptionPane.showMessageDialog(null, "ERROR: couldn\'t find executable Application TWO.\n Please, choose AppTwo.exe file.");
            newExeFile = getAppTwoExeFile();
            Runtime.getRuntime().exec(newExeFile.getAbsolutePath(), null, new File(newExeFile.getAbsolutePath()).getParentFile());
        } else if (isAppTwoJarFileExists) {
            Process proc = Runtime.getRuntime().exec("java -jar AppTwo.jar");
        } else {
            Runtime.getRuntime().exec(exeFile.getAbsolutePath(), null, new File(exeFile.getAbsolutePath()).getParentFile());
        }
    }

    private File getAppTwoExeFile(){
        String path = null;
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Executable Application TWO file", "exe");
        fileChooser.setFileFilter(filter);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int response = fileChooser.showOpenDialog(null);
        if (response == JFileChooser.APPROVE_OPTION) {
            path = fileChooser.getSelectedFile().toString();
        } else {
            JOptionPane.showMessageDialog(null, "Program will be closed.");
            System.exit(0);
        }
        return new File(path);
    }

}
