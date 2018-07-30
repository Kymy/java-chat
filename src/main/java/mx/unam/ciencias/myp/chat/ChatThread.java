package mx.unam.ciencias.myp.chat;

import java.net.*;
import java.io.*;


public class ChatThread extends Thread {

    private Socket socket;
    private Chat client;
    private ObjectInputStream streamIn = null;


    public ChatThread(Chat _client, Socket _socket) {
        client = _client;
        socket = _socket;
        open();
        start();
    }

    public void open() {
        try {
            streamIn = new ObjectInputStream(socket.getInputStream());
        } catch (IOException ioe) {
            System.out.println("...ERROR: " + ioe);
            client.stop();
        }
    }

    public void close() {
        try {
            if (streamIn != null)
                streamIn.close();
        } catch ( IOException ioe) {
            System.out.println("...ERROR: " + ioe);
        }
    }

    public void run() {
        while (true) {
            try {
                client.handle(streamIn.readObject());
            } catch (Exception ioe) {
                System.out.println("...ERROR: " + ioe.getMessage());
                client.stop();
            }
        }
    }


}