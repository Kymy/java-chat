package mx.unam.ciencias.myp.server;

import java.net.*;
import java.io.*;

public abstract class MessageProcessor {

    private static int idCounter;

    private int id;
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private boolean connected;
    protected User user;

    public MessageProcessor(Socket socket)
        throws IOException {
        this.socket = socket;
        connected = true;
        in =
            new BufferedReader(
                new InputStreamReader(
                    socket.getInputStream()));
        out =
            new BufferedWriter(
                new OutputStreamWriter(
                    socket.getOutputStream()));
        id = ++idCounter;
    }

    public int getId() {
        return id;
    }

    public boolean isConnected() {
        return connected;
    }

    public void close() {
        connected = false;
        try {
            socket.close();
        } catch (IOException ioe) {
            handleMessage(new Message("ERROR Cannot close connection"));
        }
    }

    public abstract void handleMessage(Message message);

    public void processMessages() {
        handleMessage(new Message("INFO Processing started"));
        String line;
        try {
            while ((line = in.readLine()) != null)
                handleMessage(new Message(line));
        } catch (Exception ioe) {
            if (connected) {
                String m = "ERROR Error while processing messages";
                handleMessage(new Message(m));
            }
        }
        handleMessage(new Message("INFO Processing stopped"));
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void sendError(String message) {
        send(MessageType.ERROR + " " + message);
    }

    public void sendWarning(String message) {
        send(MessageType.WARNING + " " + message);
    }

    public void sendInfo(String message) {
        send(MessageType.INFO + " " + message);
    }

    protected void send(String message) {
        try {
            out.write(message);
            out.newLine();
            out.flush();
        } catch (IOException ioe) {
            handleMessage(new Message("ERROR Cannot send message"));
        }
    }
}
