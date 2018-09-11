package mx.unam.ciencias.myp.server;

import java.net.*;
import java.io.*;

public class ServerThread extends Thread {

    private Server server;
    private Socket socket;
    private int ID;
    private BufferedReader in;
    private BufferedWriter out;
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
        in =
            new BufferedReader(
                new InputStreamReader(
                    socket.getInputStream()));
        out =
            new BufferedWriter(
                new OutputStreamWriter(
                    socket.getOutputStream()));
    }

    public void close() {
        try {
            if (socket != null)
                socket.close();
        } catch (IOException ioe) {
            System.err.println("...ERROR CLOSING SERVER THREAD.");
        }
    }

    @Override public void run() {
        String line;
        try {
            while ((line = in.readLine()) != null)
                server.handle(this, line);
        } catch (Exception ioe) {
            System.err.println("...ERROR READING: " + ioe.getMessage());
            server.remove(ID);
        }
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public boolean isIdentified() {
        boolean isIdentified = false;
        if (this.user != null) {
            isIdentified =  this.user.getName() != null;
        }
        return isIdentified;
    }

    public void send(String msg) {
        try {
            out.write(msg);
            out.newLine();
            out.flush();
        } catch (IOException ioe) {
            System.err.println(ID + "...ERROR: " + ioe.getMessage());
            server.remove(ID);
            close();
        }
    }
}
