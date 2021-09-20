package sample;

import javafx.beans.property.SimpleStringProperty;
import org.glassfish.tyrus.client.ClientManager;

import javax.sound.sampled.*;
import javax.websocket.*;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

@ClientEndpoint
public class ChatClientEndpoint {
    Thread music;
    Thread prevPlay;
    private static CountDownLatch latch;
    public static Session mySession;
    public static SimpleStringProperty statusProperty = new SimpleStringProperty("disconnected");


    @OnOpen
    public void onOpen(Session session) {
        System.out.println ("--- Connected " + session.getId());
        System.out.println(java.time.LocalTime.now());
        statusProperty.set("Connected");

        try {
            session.getBasicRemote().sendText("start");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @OnMessage
    public String onMessage(String message, Session session) {
        //BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
        try {
            System.out.println ("--- HAHAHAH Received " + message);

            if ("CALL".equals(message)) {
                runBell();
            }
            return "OK";
            /*
            String userInput = bufferRead.readLine();
            return userInput;
             */
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void runBell() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        // Run any sound
        /*
        final Runnable runnable =
                (Runnable) Toolkit.getDefaultToolkit().getDesktopProperty("win.sound.exclamation");


        if (runnable != null) runnable.run();
         */

        Clip clip;
        try {
            AudioInputStream input= AudioSystem.getAudioInputStream(new File("bellSound.wav"));
            clip=AudioSystem.getClip();
            clip.open(input);
            clip.start();

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }

    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("Session " + session.getId() +
                " closed because " + closeReason);
        System.out.println(java.time.LocalTime.now());


        statusProperty.set("Disconnected");
        try {
            for (Session sess : mySession.getOpenSessions()) {
                try {
                    //When my session is closed, close all other sessions
                    if(sess != mySession)
                        sess.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }catch (NullPointerException ne){
            System.out.println("NO CONNECTIONS YET!");
        }
        latch.countDown();
    }


    private static String getIP(){
        String domain = null;
        try {
            BufferedReader brTest = new BufferedReader(new FileReader("domain.txt"));
            domain = brTest.readLine();

        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return domain;
    }

    public static void main(String[] args) {
        latch = new CountDownLatch(1);
        ClientManager client = ClientManager.createClient();
        //client.setDefaultMaxSessionIdleTimeout(-10);
        try {
            URI uri = new URI("ws://"+getIP()+":8025/folder/app");
            mySession = client.connectToServer(ChatClientEndpoint.class, uri);
            // Will send a message every 10 minutes, to avoid making the session idle, thus avoiding closing it
            long MINUTES = 10;
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() { // Function runs every MINUTES minutes.
                    // Run the code you want here
                    try {
                        mySession.getBasicRemote().sendText("KeepSession");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, 0, 1000 * 60 * MINUTES);
            latch.await();
        } catch (DeploymentException | URISyntaxException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}