package mx.unam.ciencias.myp.chat;

import java.net.*;
import java.io.*;


public class ChatThread extends Thread {

    private Socket socket;
    private Chat client;
    private BufferedReader in;


    public ChatThread(Chat client, Socket socket) {
        this.client = client;
        this.socket = socket;
        open();
    }

    public void open() {
        try {
            in =
                new BufferedReader(
                    new InputStreamReader(
                        socket.getInputStream()));
        } catch (IOException ioe) {
            System.out.println("...ERROR: " + ioe);
            client.stop();
        }
    }

    public void close() {
        try {
            if (in != null)
                in.close();
        } catch ( IOException ioe) {
            System.out.println("...ERROR: " + ioe);
        }
    }

    public void run() {
        String line;
        try {
            while ((line = in.readLine()) != null)
                System.out.println(line);
        } catch (Exception ioe) {
            System.out.println("...DISCONNECTED");
            client.stop();
        }
    }
}
