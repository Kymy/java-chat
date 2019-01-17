package mx.unam.ciencias.myp.server;

import java.net.*;
import java.io.*;
import java.util.*;

public class Server {

    private boolean keepRunning; //says if we want to continue with the infinite loop that waits for connections
    private ServerSocket server; //Socket associated mx.unam.ciencias.myp.server
    private ArrayList<ServerThread> listClients = new ArrayList<ServerThread>();
    private ArrayList<ServerThread> identifiedClients = new ArrayList<ServerThread>();
    private HashMap<String, Room> rooms = new HashMap<String, Room>();


    public Server(int port) {
        try {
            server = new ServerSocket(port);
            System.err.println("...SERVER OK");
            start();
        } catch (IOException ioe) {
            System.err.println("...ERROR: " + port + ": "
                    + ioe.getMessage());
        }
    }

    public void start() {
        keepRunning = true;
        while (keepRunning) {
            try {
                addServerThread(server.accept());
            } catch (IOException ioe) {
                System.err.println("...ERROR: " + ioe);
                stop();
            }
        }
    }

    public void stop() {
        keepRunning = false;
    }

    /**
     * Create a new client and add it to list of clients
     * @param socket the socket from which the communication was requested
     * and starts the new server thread
     */
    private void addServerThread(Socket socket) {
        ServerThread client = new ServerThread(this, socket);
        listClients.add(client);
        System.out.println("...CLIENT ACCEPTED");
        try {
            client.open();
            client.start();
        } catch (IOException ioe) {
            System.err.println("...ERROR TO OPEN THREAD: " + ioe);
        }
    }

    public static void main(String args[]) {
        Server server = null;
        if (args.length != 1)
            System.err.println("java Server port");
        else
            server = new Server(Integer.parseInt(args[0]));
    }

    /**
     * Makes the corresponding action depending of the message type
     * @param serverThread the serverThread that is requesting the action
     * @param msg contains the message from serverThread
     */
    public synchronized void handle(ServerThread serverThread,
                                    String msg) {
        Message message = new Message(msg);
        switch(message.getType()) {
            case IDENTIFY:
                identifyUser(serverThread, message);
                break;
            case USERS:
                System.err.println("USERS");
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
                        serverThread.send("...INVALID_STATUS\n...VALID_STATUS_ARE: ACTIVE, AWAY, BUSY");
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
                        serverThread.send("...ROOM_NAME_ALREADY_IN_USE");
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
            case JOINROOM:
                if (serverThread.isIdentified()) {
                    joinUserToRoom(serverThread, message);
                } else {
                    serverThread.send("...MUST IDENTIFY FIRST\n...TO IDENTIFY: IDENTIFYUSERNAME");
                }
                break;
            case ROOMESSAGE:
                if (serverThread.isIdentified()) {
                    sendMessageToRoom(serverThread, message);
                } else {
                    serverThread.send("...MUST IDENTIFY FIRST\n...TO IDENTIFY: IDENTIFYUSERNAME");
                }
                break;
            case INVALID:
                    serverThread.send("...INVALID MESSAGE\n...VALID MESSAGES ARE:\n...IDENTIFY username"+
                    "\n...STATUS userStatus = {ACTIVE, AWAY, BUSY}"+ "\n...MESSAGE username messageContent" +
                    "\n...PUBLICMESSAGE messageContent" + "\n...CREATEROOM roomname" + "\n...INVITE roomname user1 user2..."
                            + "\n...JOINROOM roomname"
                            + "\n...ROOMESSAGE roomname messageContent"
                            + "\n...DISCONNECT");
                break;
        }
    }

    public void identifyUser(ServerThread serverThread, Message message) {
        String userName = message.getMessage();
        if (serverThread.getUser() != null) {
            serverThread.send("...ALREADY_IDENTIFIED");
        } else if (isValidUserName(userName)) {
            User user = new User();
            user.setName(message.getMessage());
            serverThread.setUser(user);
            identifiedClients.add(serverThread);
            serverThread.send("...SUCCESSFULLY_IDENTIFIED");
            String msgToAll = "...USER_CONNECTED " + userName;
            sendMessageToIdentifiedClients(serverThread, msgToAll);
        } else {
            serverThread.send("...USERNAME_NOT_AVAILABLE");
        }
    }

    public void sendMessageToRoom(ServerThread serverThread, Message message) {
        String roomName = message.getToWhom();
        if (roomNameExists(roomName)) {
            Room room = rooms.get(roomName);
            String username = serverThread.getUser().getName();
            if (room.isUserInvited(username) || room.isTheOwner(username)) {
                room.sendMessageToGuests(username, message.getMessage());
                serverThread.send("...ROOM_MESSAGE_SENT");
            } else {
                serverThread.send("...YOU_ARE_NOT_PART_OF_THE_ROOM");
            }
        } else {
            serverThread.send("...ROOM NOT EXISTS");
        }
    }

    public void createNewRoom(ServerThread serverThread, Message message) {
        String roomName = message.getMessage();
        Room newRoom = new Room(roomName, serverThread);
        rooms.put(roomName, newRoom);
        serverThread.send("...ROOM_CREATED");
    }

    public void joinUserToRoom(ServerThread serverThread, Message message) {
        String roomName = message.getMessage();
        if (roomNameExists(roomName)) {
            Room room = rooms.get(roomName);
            String username = serverThread.getUser().getName();
            if (room.isUserInvited(username)) {
                String addTo = room.addToRoom(serverThread);
                serverThread.send(addTo);
                room.sendMessageToGuests(username, "JOINED");
            } else {
                serverThread.send("...YOU_ARE_NOT_INVITED_TO_ROOM " + roomName);
            }
        } else {
            serverThread.send("...ROOM_DOES_NOT_EXIST");
        }

    }

    public void inviteUsersToRoom(ServerThread serverThread, Message message) {
        String roomName = message.getToWhom();
        if (roomNameExists(roomName)) {
            if (isRoomOwnerByUser(roomName, serverThread)) {
                Room room = this.rooms.get(roomName);
                String ownerName = serverThread.getUser().getName();
                String [] users = message.getMessage().split(" ");
                for (int i=0; i<users.length; i++) {
                    String userInvited = users[i];
                    room.inviteGuest(userInvited);
                    String msg = "...INVITATION_TO_JOIN " + roomName + " ROOM_BY " + ownerName +
                            "\n...TO JOIN: JOINROOM " + roomName;
                    sendInvitationToUser(serverThread, userInvited, msg);
                }
            } else {
                serverThread.send("...YOU_ARE_NOT_THE_OWNER_OF_THE_ROOM");
            }

        } else {
            serverThread.send("...ROOM_DOES_NOT_EXIST");
        }
    }

    public void sendInvitationToUser(ServerThread serverThread, String toWhom, String message) {
        ServerThread serverToWhom = findServerByUser(toWhom);
        if (serverToWhom == null) {
            serverThread.send("...USER_NOT_FOUND " + toWhom);
        } else {
            serverToWhom.send(message);
            serverThread.send("...INVITATION_SENT_TO " + serverToWhom.getUser().getName());
        }
    }

    public boolean isRoomOwnerByUser(String roomName, ServerThread serverThread) {
        Room room = this.rooms.get(roomName);
        ServerThread owner = room.getOwner();
        if (serverThread == owner) {
            return true;
        }
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
        sendMessageToIdentifiedClients(serverThread, "...DISCONNECTED_USER " + serverThread.getUser().getName());
        listClients.remove(serverThread);
        identifiedClients.remove(serverThread);
        closeClient(serverThread);
    }

    public void closeClient(ServerThread serverThread) {
        serverThread.close();
        System.err.println("...DISCONNECTED " + serverThread.getID());
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
        serverThread.send("...STATUS_CHANGED " + status);
        String msg = "...STATUS_CHANGED_FROM "  + user.getName() + " TO " + status;
        sendMessageToIdentifiedClients(serverThread, msg);
        return true;
    }

    public void sendMessageToIdentifiedClients(ServerThread serverThread, String message) {
        for (int i = 0; i < identifiedClients.size(); i++) {
            if (!serverThread.getUser().getName().equals(identifiedClients.get(i).getUser().getName())){
                identifiedClients.get(i).send(message);
            }
        }
    }

    public void sendMessageToUser(ServerThread serverThread, Message message) {
        String toWhom = message.getToWhom();
        String msg = message.getMessage();
        ServerThread serverToWhom = findServerByUser(toWhom);
        if (serverToWhom == null) {
            serverThread.send("...USER_NOT_FOUND " + toWhom);
        } else {
            String msgToSend = "...MESSAGE_FROM " +serverThread.getUser().getName() + ": " + msg;
            serverToWhom.send(msgToSend);
            serverThread.send("...MESSAGE_SENT");
        }
    }

    public void sendPublicMessage(ServerThread serverThread, Message message) {
        String username = serverThread.getUser().getName();
        serverThread.send("...PUBLIC_MESSAGE_SENT");
        String msg = "...PUBLIC_MESSAGE_FROM " + username + ": " + message.getMessage();
        sendMessageToIdentifiedClients(serverThread, msg);
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


    public synchronized void remove(int ID) {
        ServerThread clientToRemove = findClient(ID);
        if (clientToRemove != null) {
            System.err.println("...REMOVING CLIENT: " + ID);
            listClients.remove(clientToRemove);
            clientToRemove.close();
        }
    }
}
