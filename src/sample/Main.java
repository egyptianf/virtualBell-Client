package sample;

import com.dustinredmond.fxtrayicon.FXTrayIcon;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
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
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("sample.fxml")));
        window = primaryStage;
        primaryStage.setTitle("Virtual Bell");



        main = new Thread(new Runnable() {
            @Override
            public void run() {
                // code goes here.
               ChatClientEndpoint.main(null);
            }
        });
        main.start();


        FXTrayIcon fxTrayIcon = new FXTrayIcon(primaryStage, getClass().getResource("red-circle.png"));
        fxTrayIcon.show();
        primaryStage.setOnCloseRequest(e -> this.minimize(primaryStage));
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> {
            try {
                this.closeProgram(fxTrayIcon);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
        fxTrayIcon.addMenuItem(exitItem);



        primaryStage.setScene(new Scene(root, 300, 120));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

    private void minimize(Stage window){
        System.out.println("Minimized!");
        window.close();
    }
    private void closeProgram(FXTrayIcon icon) throws IOException {
        try {
            for (Session sess : ChatClientEndpoint.mySession.getOpenSessions()) {
                try {
                    sess.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }catch (NullPointerException ne){
            System.out.println("NO CONNECTIONS YET!");
            window.close();
            icon.hide();
            Platform.exit();
        }catch (IllegalStateException ise)
        {
            System.out.println("Connection has been terminated: Illegal State");
            window.close();
            icon.hide();
            Platform.exit();
        }
        window.close();
        icon.hide();
        Platform.exit();
    }
    @Override
    public void stop() {
        main.interrupt();
        window.close();
    }

}
