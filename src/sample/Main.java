package sample;

import com.dustinredmond.fxtrayicon.FXTrayIcon;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import javax.sound.sampled.*;
import javax.websocket.Session;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class Main extends Application {
    Thread main;
    Stage window;
    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("sample.fxml")));
        Parent root = loader.load();
        SampleController myController = loader.getController();
        if("Connected".equals(myController.connected.getText()))
            myController.connected.setTextFill(Color.GREEN);

        window = primaryStage;
        window.setTitle("Virtual Bell");



        main = new Thread(new Runnable() {
            @Override
            public void run() {
                // code goes here.
               ChatClientEndpoint.main(null);
            }
        });
        main.start();

        /*Trying Platform.runLater() function
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                // code goes here.
                ChatClientEndpoint.main(null);
            }
        });
         */


        FXTrayIcon fxTrayIcon = new FXTrayIcon(window, getClass().getResource("red-circle.png"));
        fxTrayIcon.show();
        window.setOnCloseRequest(e -> this.minimize(window));
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> {
            try {
                this.closeProgram(fxTrayIcon, main);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
        fxTrayIcon.addMenuItem(exitItem);



        window.setScene(new Scene(root, 300, 120));
        window.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

    private void minimize(Stage window){
        System.out.println("Minimized!");
        window.close();
    }
    private void closeProgram(FXTrayIcon icon, Thread mainThread) throws IOException {
        try {
            ChatClientEndpoint.mySession.close();
            System.out.println("Entered closeProgram() function");
            window.close();
            mainThread.interrupt();
            icon.hide();
            Platform.exit();
            System.exit(0);

        }catch (NullPointerException ne){
            System.out.println("NO CONNECTIONS YET!");
            window.close();
            mainThread.interrupt();
            icon.hide();
            Platform.exit();
            System.exit(0);
        }catch (IllegalStateException ise)
        {
            System.out.println("Connection has been terminated: Illegal State");
            window.close();
            mainThread.interrupt();
            icon.hide();
            Platform.exit();
            System.exit(0);

        }
        window.close();
        mainThread.interrupt();
        icon.hide();
        Platform.exit();
        System.exit(0);

    }
    @Override
    public void stop() {
        //main.interrupt();
        window.close();
    }

}
