package mx.unam.ciencias.myp.server;

import java.net.*;
import java.io.*;
import java.util.*;

public class Server implements Runnable {

    private ServerSocket server = null; //Socket associated mx.unam.ciencias.myp.server
    private Thread thread = null;
    ArrayList<ServerThread> listClients = new ArrayList<ServerThread>();
    ArrayList<ServerThread> identifiedClients = new ArrayList<ServerThread>();


    public Server(int port) {
        try {
            server = new ServerSocket(port);
            System.out.println("...SERVER OK");
            start();
        } catch (IOException ioe) {
            System.out.println("...ERROR: " + port + ": "
                    + ioe.getMessage());
        }
    }

    public void start() {
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }

    public void stop() {
        if (thread != null) {
            thread.stop();
            thread = null;
        }
    }

    public void run() {
        while (thread != null) {
            try {
                addThread(server.accept());
            } catch (IOException ioe) {
                System.out.println("...ERROR: " + ioe);
                stop();
            }
        }
    }

    private void addThread(Socket socket) {
        ServerThread newClient = new ServerThread(this, socket);
        listClients.add(newClient);
        System.out.println("...CLIENT ACCEPTED");
        try {
            newClient.open();
            newClient.start();
        } catch (IOException ioe) {
            System.out.println("...ERROR TO OPEN THREAD: " + ioe);
        }
    }

    public static void main(String args[]) {
        Server server = null;
        if (args.length != 1)
            System.out.println("java Server port");
        else
            server = new Server(Integer.parseInt(args[0]));
    }

    public synchronized void handle(ServerThread serverThread, Object inputObject) {
        Message message = (Message)inputObject;
        switch(message.getType()) {
            case CONNECT:
                identifyUser(serverThread, message);
                break;
            case USERS:
                showUsers(serverThread);
                break;
            case DISCONNECT:
                disconnectUser(serverThread);
                break;
            case STATUS:
                setUserStatus(serverThread, message);
                break;
            case MESSAGE:
                sendMessageToUser(serverThread, message);
                break;
        }
    }

    public void identifyUser(ServerThread serverThread, Message message) {
        String userName = message.getMessage();
        if (isValidUserName(userName)) {
            User user = new User();
            user.setName(message.getMessage());
            serverThread.setUser(user);
            identifiedClients.add(serverThread);
            serverThread.send("...SUCCESSFUL IDENTIFICATION");
        } else {
            serverThread.send("...USERNAME NOT AVAILABLE");
        }

    }

    public boolean isValidUserName(String message) {
        for(int i=0; i<identifiedClients.size(); i++) {
            if (identifiedClients.get(i).getUser().getName().equals(message)) {
                return false;
            }
        }
        return true;
    }

    public void showUsers(ServerThread serverThread) {
        String users = "";
        for(int i=0; i<identifiedClients.size(); i++) {
            users += identifiedClients.get(i).getUser().getName() + " ";
        }
        serverThread.send(users);
    }

    public void disconnectUser(ServerThread serverThread) {
        listClients.remove(serverThread);
        identifiedClients.remove(serverThread);
        closeClient(serverThread);
    }

    public void closeClient(ServerThread serverThread) {
        try {
            serverThread.close();
        } catch (IOException ioe) {
            System.out.println("...ERROR " + ioe);
        }
        serverThread.stop();
        System.out.println("...DISCONNECTED " + serverThread.getID());
    }


    public void setUserStatus(ServerThread serverThread, Message message) {
        User user = serverThread.getUser();
        String status = message.getMessage();
        switch (status) {
            case "ACTIVE":
                user.setStatus(UserStatus.ACTIVE);
                break;
            case "AWAY":
                user.setStatus(UserStatus.AWAY);
                break;
            case "BUSY":
                user.setStatus(UserStatus.BUSY);
                break;
            default:
                user.setStatus(UserStatus.ACTIVE);
                break;
        }
        String msg = user.getName() + " " + user.getStatus();
        sendMessageToIdentifiedClients(msg);
    }

    public void sendMessageToIdentifiedClients(String message) {
        for (int i = 0; i < identifiedClients.size(); i++) {
            identifiedClients.get(i).send(message);
        }
    }

    public void sendMessageToUser(ServerThread serverThread, Message message) {
        String toWhom = message.getToWhom();
        String msg = message.getMessage();
        ServerThread serverToWhom = findServerByUser(toWhom);
        String toSend = serverThread.getUser().getName() + " " + msg;
        serverToWhom.send(toSend);
    }

    public ServerThread findServerByUser(String username) {
        String name;
        ServerThread actualServer = null;
        for (int i = 0; i < identifiedClients.size(); i++) {
            actualServer = identifiedClients.get(i);
            name = actualServer.getUser().getName();
            if (username.equals(name)) {
                return actualServer;
            }
        }
        return actualServer;
    }

    private ServerThread findClient(int ID) {
        for (int i = 0; i < listClients.size(); i++) {
            if (listClients.get(i).getID() == ID)
                return listClients.get(i);
        }
        return null;
    }

    private int findIndexClient(int ID) {
        for (int i = 0; i < listClients.size(); i++) {
            if (listClients.get(i).getID() == ID)
                return i;
        }
        return -1;
    }

    public synchronized void remove(int ID) {
        ServerThread clientToRemove = findClient(ID);
        if (clientToRemove != null) {
            int index = findIndexClient(ID);
            System.out.println("...REMOVING CLIENT: " + ID);
            listClients.remove(index);
            try {
                clientToRemove.close();
            } catch (IOException ioe) {
                System.out.println("...ERROR: " + ioe);
            }
            clientToRemove.stop();
        }
    }

}
