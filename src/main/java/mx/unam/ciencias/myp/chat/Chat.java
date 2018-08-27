package mx.unam.ciencias.myp.chat;

import java.net.*;
import java.io.*;
import java.util.*;
import mx.unam.ciencias.myp.server.Message;
import mx.unam.ciencias.myp.server.MessageType;



public class Chat implements Runnable {

    private Socket socket = null;
    private DataInputStream console = null;
    private ObjectOutputStream streamOut = null;
    private Thread thread = null;
    private ChatThread client = null;
    public PriorityQueue<String> messages;


    public Chat(String serverName, int serverPort) {
        System.out.println("...STARTING CONNECTION");
        try {
            socket = new Socket(serverName, serverPort);
            messages = new PriorityQueue<String>();
            System.out.println("...CONNECTED");
            start();

        } catch (UnknownHostException uhe) {
            System.out.println("...IP ADDRESS UNKNOW: " + uhe.getMessage());
        } catch (IOException ioe) {
            System.out.println("...ERROR: " + ioe.getMessage());
        }
    }


    public void run() {
        Object message;
        while (thread !=null) {
            try {
                message = console.readLine();
                Message msg = createMessage((String) message);
                if (msg != null ) {
                    streamOut.writeObject(msg);
                    streamOut.flush();
                }
            } catch (IOException ioe) {
                System.out.println("...ERROR TO SEND: " + ioe.getMessage());
                stop();
            }

        }

    }

    public Message createMessage(String input) {
        String [] parts = input.split(" ");
        Message msg = new Message();
        switch (parts[0]) {
            case "CONNECT":
                try {
                    msg.setType(MessageType.CONNECT);
                    msg.setMessage(parts[1]);
                } catch (ArrayIndexOutOfBoundsException exception) {
                    System.out.println("CONNECT USERNAME");
                    return null;
                }
                break;
            case "USERS":
                msg.setType(MessageType.USERS);
                break;
            case "DISCONNECT":
                msg.setType(MessageType.DISCONNECT);
                break;
            case "STATUS":
                try {
                    msg.setType(MessageType.STATUS);
                    msg.setMessage(parts[1]);
                } catch (ArrayIndexOutOfBoundsException exception) {
                    System.out.println("STATUS ACTIVE/BUSY/AWAY");
                    return null;
                }
                break;
            case "MESSAGE":
                try {
                    msg.setType(MessageType.MESSAGE);
                    msg.setToWhom(parts[1]);
                    String message = "";
                    for (int i=2; i<parts.length; i++) {
                        message += parts[i] + " ";
                    }
                    msg.setMessage(message);
                } catch(ArrayIndexOutOfBoundsException exception) {
                    System.out.println("MESSAGE USER MESSAGE_CONTENT");
                    return null;
                }

                break;
            case "PUBLICMESSAGE":
                try {
                    msg.setType(MessageType.PUBLICMESSAGE);
                    String message = "";
                    for (int i=1; i<parts.length; i++) {
                        message += parts[i] + " ";
                    }
                    msg.setMessage(message);
                } catch(ArrayIndexOutOfBoundsException exception) {
                    System.out.println("PUBLICMESSAGE MESSAGE_CONTENT");
                    return null;
                }

                break;
            default:
                msg.setType(MessageType.INVALID);
                break;
        }
        return msg;
    }

    public void handle(Object input) {
        String message = (String)input;
        messages.add(message);
        System.out.println(message);
    }

    public void start() throws IOException {
        console = new DataInputStream(System.in);
        streamOut = new ObjectOutputStream(socket.getOutputStream());

        if (thread == null) {
            client = new ChatThread(this, socket);
            thread = new Thread(this);
            thread.start();
        }
    }

    public void stop() {
        if (thread != null) {
            thread.stop();
            thread = null;
        }
        try {
            if(console != null)
                console.close();
            if(streamOut != null)
                streamOut.close();
            if(socket != null)
                socket.close();
        } catch (IOException ioe) {
            System.out.println("...ERROR TO CLOSE");
        }

        client.close();
        client.stop();
    }

    public static void main(String args[]) {
        Chat client = null;
        if (args.length != 2)
            System.out.println("java ChatClient host port");
        else
            client = new Chat(args[0], Integer.parseInt(args[1]));
    }

}