package mx.unam.ciencias.myp.chat;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import mx.unam.ciencias.myp.server.Message;
import mx.unam.ciencias.myp.server.MessageType;



public class Chat {

    private Socket socket;
    private Scanner scanner;
    private BufferedWriter out;
    private ChatThread client;
    private boolean keepRunning;

    public Chat(String serverName, int serverPort) {
        System.err.println("...STARTING CONNECTION");
        try {
            socket = new Socket(serverName, serverPort);
            System.err.println("...CONNECTED");
        } catch (UnknownHostException uhe) {
            System.err.println("...IP ADDRESS UNKNOW: " + uhe.getMessage());
        } catch (IOException ioe) {
            System.err.println("...ERROR: " + ioe.getMessage());
        }
    }

    public void start() {
        client = new ChatThread(this, socket);
        client.start();
        scanner = new Scanner(System.in);
        try {
            out =
                new BufferedWriter(
                    new OutputStreamWriter(
                        socket.getOutputStream()));
            keepRunning = true;
            while (keepRunning) {
                String line = scanner.nextLine();
                Message message = new Message(line);
                out.write(line);
                out.newLine();
                out.flush();
                if (message.getType() == MessageType.DISCONNECT) {
                    stop();
                }

            }
        } catch (IOException ioe) {
            System.err.println("...ERROR " + ioe.getMessage());
        }
    }

    public void stop() {
        if (client != null)
            client.close();
        keepRunning = false;
    }

    public static void main(String args[]) {
        Chat client = null;
        if (args.length != 2) {
            System.err.println("java ChatClient host port");
        } else {
            client = new Chat(args[0], Integer.parseInt(args[1]));
            client.start();
        }
    }
}
