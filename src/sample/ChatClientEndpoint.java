package sample;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.glassfish.tyrus.client.ClientManager;

import javax.sound.sampled.*;
import javax.websocket.*;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

@ClientEndpoint
public class ChatClientEndpoint  {
    Thread music;
    Thread prevPlay;
    private static CountDownLatch latch;
    public static Session mySession = null;
    public static Queue<Session> openSessions = new LinkedList<>();
    public static SimpleStringProperty statusProperty = new SimpleStringProperty("DISCONNECTED");


    @OnOpen
    public void onOpen(Session session) {
        System.out.println ("--- Connected " + session.getId());
        System.out.println(java.time.LocalTime.now());
        mySession = session;
        openSessions.add(session);
        try {
            session.getBasicRemote().sendText("start");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                statusProperty.set("Connected");
            }
        });

    }

    @OnMessage
    public String onMessage(String message, Session session) {
        try {
            System.out.println ("--- HAHAHAH Received " + message);

            if ("CALL".equals(message)) {
                runBell();
            }
            return "OK";
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

        openSessions.remove(session);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                statusProperty.set("Disconnected");
            }
        });
        mySession = null;
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


            //mySession = client.connectToServer(ChatClientEndpoint.class, uri);

            int SECS = 3;
            // reconnection logic in a thread
            Timer reconnection = new Timer();
            reconnection.schedule(new TimerTask() {
                @Override
                public void run() {
                        try {
                            if(mySession == null) {
                                System.out.println("mySession is null");
                                mySession = client.connectToServer(ChatClientEndpoint.class, uri);
                            }
                            else{
                                System.out.println("mySession is not null");
                                if(!mySession.isOpen()){
                                    System.out.println("And my session is closed!");
                                    mySession.close();
                                    mySession = client.connectToServer(ChatClientEndpoint.class, uri);
                                }
                            }
                            System.out.println("Number of open sessions: " + openSessions.size());
                        } catch (DeploymentException | IOException e) {
                            System.out.println("Deployment or IO Exception");
                            e.printStackTrace();
                        }
                    }

            }, 0, 1000 * SECS );


            // Will send a message every 10 minutes, to avoid making the session idle, thus avoiding closing it
            long MINUTES = 10;
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() { // Function runs every MINUTES minutes.
                    // Run the code you want here
                    try {
                        if(mySession != null && mySession.isOpen())
                            mySession.getBasicRemote().sendText("KeepSession");
                    } catch ( IOException e) {
                        e.printStackTrace();
                    }
                }
            }, 0, 1000 * 60 * MINUTES);
            latch.await();
        }  catch (URISyntaxException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}