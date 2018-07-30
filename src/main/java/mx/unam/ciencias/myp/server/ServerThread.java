package mx.unam.ciencias.myp.server;

import java.net.*;
import java.io.*;

public class ServerThread extends Thread {

    private Server server;
    private Socket socket;
    private int ID;
    private ObjectInputStream streamIn = null;
    private ObjectOutputStream streamOut = null;
    private User user;

    public ServerThread(Server _server, Socket _socket) {
        super();
        server = _server;
        socket = _socket;
        ID = socket.getPort();
    }

    public int getID() {
        return ID;
    }

    public void open() throws IOException {
        streamIn = new ObjectInputStream(socket.getInputStream());
        streamOut = new ObjectOutputStream(socket.getOutputStream());
    }

    public void close() throws IOException {
        if (socket != null)
            socket.close();
        if (streamIn != null)
            streamIn.close();
        if (streamOut != null)
            streamOut.close();
    }

    public void run() {
        while (true) {
            try {
                server.handle(this, streamIn.readObject());
            } catch (Exception ioe) {
                System.out.println("...ERROR READING " + ioe.getMessage());
                server.remove(ID);
                stop();
            }
        }
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }


    public void send(String msg) {
        try {
            streamOut.writeObject(msg);
            streamOut.flush();
        } catch (IOException ioe) {
            System.out.println(ID + "...ERROR " + ioe.getMessage());
            server.remove(ID);
            stop();
        }
    }

}