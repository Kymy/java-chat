package mx.unam.ciencias.myp.server;

import java.net.*;
import java.io.*;

public abstract class MessageProcessor {

    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    protected User user;

    public MessageProcessor(Socket socket)
        throws IOException {
        this.socket = socket;
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
            socket.close();
        } catch (IOException ioe) {
            handleMessage(new Message("ERROR error while closing connection"));
        }
    }

    public abstract void handleMessage(Message message);

    public void processMessages() {
        String line;
        try {
            while ((line = in.readLine()) != null) {
                handleMessage(new Message(line));
            }
        } catch (Exception ioe) {
            handleMessage(new Message("ERROR error while processing messages"));
        }
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    protected void send(String message) {
        try {
            out.write(message);
            out.newLine();
            out.flush();
        } catch (IOException ioe) {
            handleMessage(new Message("ERROR error while sending message"));
        }
    }
}
