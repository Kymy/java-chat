package mx.unam.ciencias.myp.server;

import java.net.*;
import java.io.*;
import java.util.*;

public class Server implements Runnable {

    private ServerSocket server = null; //Socket associated mx.unam.ciencias.myp.server
    private Thread thread = null;
    ArrayList<ServerThread> listClients = new ArrayList<ServerThread>();
    ArrayList<ServerThread> identifiedClients = new ArrayList<ServerThread>();
    HashMap<String, Room> rooms = new HashMap<String, Room>();


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
            case IDENTIFY:
                identifyUser(serverThread, message);
                break;
            case USERS:
                System.out.println("USERS");
                if (serverThread.isIdentified()) {
                    showUsers(serverThread);
                } else {
                    serverThread.send("...MUST IDENTIFY FIRST\n...TO IDENTIFY: IDENTIFY USERNAME");
                }
                break;
            case DISCONNECT:
                disconnectUser(serverThread);
                break;
            case STATUS:
                if (serverThread.isIdentified()) {
                    if (!setUserStatus(serverThread, message)) {
                        serverThread.send("...INVALID STATUS\n...POSSIBLE STATUS ARE: ACTIVE, AWAY, BUSY");
                    }
                } else {
                    serverThread.send("...MUST IDENTIFY FIRST\n...TO IDENTIFY: IDENTIFY USERNAME");
                }
                break;
            case MESSAGE:
                if (serverThread.isIdentified()) {
                    sendMessageToUser(serverThread, message);
                } else {
                    serverThread.send("...MUST IDENTIFY FIRST\n...TO IDENTIFY: IDENTIFY USERNAME");
                }
                break;
            case PUBLICMESSAGE:
                if (serverThread.isIdentified()) {
                    sendPublicMessage(serverThread, message);
                } else {
                    serverThread.send("...MUST IDENTIFY FIRST\n...TO IDENTIFY: IDENTIFYUSERNAME");
                }
                break;
            case CREATEROOM:
                if (serverThread.isIdentified()) {
                    if (!roomNameExists(message.getMessage())) {
                        createNewRoom(serverThread, message);
                    } else {
                        serverThread.send("...ROOM NAME ALREADY IN USE");
                    }
                } else {
                    serverThread.send("...MUST IDENTIFY FIRST\n...TO IDENTIFY: IDENTIFYUSERNAME");
                }
                break;
            case INVITE:
                if (serverThread.isIdentified()) {
                   inviteUsersToRoom(serverThread, message);
                } else {
                    serverThread.send("...MUST IDENTIFY FIRST\n...TO IDENTIFY: IDENTIFYUSERNAME");
                }
                break;
            case INVALID:
                    serverThread.send("...INVALID MESSAGE\n...VALID MESSAGES ARE:\n...IDENTIFY username"+
                    "\n...STATUS userStatus = {ACTIVE, AWAY, BUSY}"+ "\n...MESSAGE username messageContent" +
                    "\n...PUBLICMESSAGE messageContent" + "\n...CREATEROOM roomname" + "\n...DISCONNECT");
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

    public void createNewRoom(ServerThread serverThread, Message message) {
        String roomName = message.getMessage();
        System.out.println("Se creará una sala con nombre " + roomName);
        Room newRoom = new Room(roomName, serverThread);
        rooms.put(roomName, newRoom);
        serverThread.send("...ROOM CREATED");
    }

    public void inviteUsersToRoom(ServerThread serverThread, Message message) {
        String roomName = message.getToWhom();
        if (roomNameExists(roomName) && isRoomOwnerByUser(roomName, serverThread)) {
            String ownerName = serverThread.getUser().getName();
            String [] users = message.getMessage().split(" ");
            for (int i=0; i<users.length; i++) {
                String userInvited = users[i];
                String msg = "...INVITATION TO JOIN " + roomName + " ROOM BY " + ownerName +
                        "\n...TO JOIN: JOIN " + roomName;
                sendInvitationToUser(serverThread, userInvited, msg);
            }
        } else {
            serverThread.send("...ROOM NOT EXIST OR YOU ARE NOT THE OWNER");
        }

    }

    public void sendInvitationToUser(ServerThread serverThread, String toWhom, String message) {
        ServerThread serverToWhom = findServerByUser(toWhom);
        if (serverToWhom == null) {
            serverThread.send("...USER " + toWhom + " NOT FOUND");
        } else {
            serverToWhom.send(message);
            serverThread.send("...INVITATION SENT TO " + serverToWhom.getUser().getName());
        }
    }

    public boolean isRoomOwnerByUser(String roomName, ServerThread serverThread) {
        Room room = this.rooms.get(roomName);
        ServerThread owner = room.getOwner();
        if (serverThread == owner) {
            System.out.println("Es el owner");
            return true;
        }
        System.out.println("NO es el owner");
        return false;
    }

    public boolean roomNameExists(String message) {
       Room exists = this.rooms.get(message);
       if (exists == null) {
           return false;
       }
       return true;
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

    public boolean isValidStatus(String status) {
        return (status.equals("ACTIVE") || status.equals("AWAY") || status.equals("BUSY"));
    }


    public boolean setUserStatus(ServerThread serverThread, Message message) {
        User user = serverThread.getUser();
        String status = message.getMessage();
        if (!isValidStatus((status))) {
            return false;
        }
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
        return true;
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
        if (serverToWhom == null) {
            serverThread.send("...USER " + toWhom + " NOT FOUND");
        } else {
            String toSend = serverThread.getUser().getName() + ": " + msg;
            serverToWhom.send(toSend);
            serverThread.send("...MESSAGE SENT");
        }
    }

    public void sendPublicMessage(ServerThread serverThread, Message message) {
        String msg = message.getMessage();
        sendMessageToIdentifiedClients(msg);
    }

    public ServerThread findServerByUser(String username) {
        String name;
        ServerThread actualServer;
        for (int i = 0; i < identifiedClients.size(); i++) {
            actualServer = identifiedClients.get(i);
            name = actualServer.getUser().getName();
            if (username.equals(name)) {
                return actualServer;
            }
        }
        return null;
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
