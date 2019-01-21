package mx.unam.ciencias.myp.server;

import java.net.*;
import java.io.*;
import java.util.*;

public class Server {

    private boolean keepRunning;
    private ServerSocket server;
    private Map<String, User> users;
    private Map<String, Room> rooms;
    private Set<User> identified;
    private Map<User, ServerMessageProcessor> clients;

    public Server(int port) throws IOException {
        users = new HashMap<String, User>();
        rooms = new HashMap<String, Room>();
        identified = new HashSet<User>();
        clients = new HashMap<User, ServerMessageProcessor>();
        server = new ServerSocket(port);
    }

    public void start() {
        System.err.println("Server started...");
        keepRunning = true;
        while (keepRunning) {
            try {
                createMessageProcessor(server.accept());
            } catch (IOException ioe) {
                System.err.println("Error accepting a connection");
            }
        }
    }

    public void stop() {
        keepRunning = false;
        try {
            for (ServerMessageProcessor client : clients.values())
                client.close();
            server.close();
        } catch (IOException ioe) {}
    }

    private void createMessageProcessor(Socket socket) throws IOException {
        ServerMessageProcessor client;
        client = new ServerMessageProcessor(this, socket);
        Thread thread = new Thread(() -> client.processMessages());
        thread.start();
    }

    public synchronized void handle(ServerMessageProcessor processor,
                                    Message message) {
        MessageType type = message.getType();
        if (type != MessageType.IDENTIFY   &&
            type != MessageType.DISCONNECT &&
            type != MessageType.ERROR      &&
            type != MessageType.WARNING    &&
            type != MessageType.INFO       &&
            !processor.isIdentified()) {
            processor.sendWarning("Must identify first");
            return;
        }
        switch(type) {
        case IDENTIFY:      handleIdentify(processor, message);      break;
        case STATUS:        handleStatus(processor, message);        break;
        case USERS:         processor.sendUserList(identified);      break;
        case MESSAGE:       handleMessage(processor, message);       break;
        case PUBLICMESSAGE: handlePublicMessage(processor, message); break;
        case CREATEROOM:    handleCreateRoom(processor, message);    break;
        case INVITE:        handleInvite(processor, message);        break;
        case JOINROOM:      handleJoinRoom(processor, message);      break;
        case ROOMESSAGE:    handleRoomMessage(processor, message);   break;
        case LEAVEROOM:     handleLeaveRoom(processor, message);     break;
        case DISCONNECT:    handleDisconnect(processor, message);    break;

        case ERROR:         handleError(processor, message);         break;
        case WARNING:       handleWarning(processor, message);       break;
        case INFO:          handleInfo(processor, message);          break;

        case NEWUSER:
        case USERLIST:
        case MESSAGEFROM:
        case PUBLICMESSAGEFROM:
        case INVITEFROM:
        case JOINEDFROM:
        case ROOMESSAGEFROM:
        case LEFTROOM:
        case DISCONNECTED:
            System.err.println("Invalid message from " + processor.getId() +
                               ": " + type);
            processor.sendWarning("Invalid message " + type);
            break;

        case INVALID:
            System.err.println("Invalid message from " + processor.getId() +
                               ": " + message.getMessageContent());
            processor.sendWarning(message.getMessageContent());
            break;
        }
    }

    private void handleIdentify(ServerMessageProcessor processor,
                                Message message) {
        String userName = message.getUserName();
        if (users.containsKey(userName)) {
            processor.sendWarning("User " + userName +
                                  " already identified");
            return;
        }
        User user = new User(userName);
        processor.setUser(user);
        users.put(userName, user);
        clients.put(user, processor);
        identified.add(user);
        processor.sendInfo("User " + userName + " succesfully identified");
        for (User u : identified) {
            if (u == user)
                continue;
            ServerMessageProcessor p = clients.get(u);
            p.sendNewUser(user);
        }
    }

    private void handleStatus(ServerMessageProcessor processor,
                              Message message) {
        UserStatus status = UserStatus.getUserStatus(message.getStatus());
        if (status == null) {
            processor.sendWarning("Invalid status");
        } else {
            User user = processor.getUser();
            user.setStatus(status);
            processor.sendInfo("Status changed successfully");
        }
    }

    private void handleMessage(ServerMessageProcessor processor,
                               Message message) {
        User user = processor.getUser();
        String whomName = message.getUserName();
        if (!users.containsKey(whomName)) {
            processor.sendWarning("User " + whomName + " is invalid");
            return;
        }
        User whom = users.get(whomName);
        ServerMessageProcessor p = clients.get(whom);
        p.sendMessageFrom(user, message.getMessageContent());
    }

    private void handlePublicMessage(ServerMessageProcessor processor,
                                    Message message) {
        User user = processor.getUser();
        for (User u : identified) {
            if (u == user)
                continue;
            ServerMessageProcessor p = clients.get(u);
            p.sendPublicMessageFrom(user, message.getMessageContent());
        }
    }

    private void handleCreateRoom(ServerMessageProcessor processor,
                                  Message message) {
        String roomName = message.getRoomName();
        if (rooms.containsKey(roomName)) {
            processor.sendWarning("Room exists already");
            return;
        }
        User user = processor.getUser();
        Room room = new Room(roomName, user);
        rooms.put(roomName, room);
        processor.sendInfo("Room " + roomName + " succesfully created");
    }

    private void handleInvite(ServerMessageProcessor processor,
                              Message message) {
        User user = processor.getUser();
        String roomName = message.getRoomName();
        if (!rooms.containsKey(roomName)) {
            processor.sendWarning("Room " + roomName + " doesn't exists");
            return;
        }
        Room room = rooms.get(roomName);
        String[] userNames = message.getMessageContent().split(",");
        Set<User> invitedUsers = new HashSet<User>();
        for (String userName : userNames) {
            if (!users.containsKey(userName)) {
                processor.sendWarning("User " + userName + " is invalid");
                return;
            }
            if (userName.equals(user.getName())) {
                processor.sendWarning("A user cannot invite himself");
                return;
            }
            User invited = users.get(userName);
            if (room.isInvited(invited) || room.isGuest(invited)) {
                processor.sendWarning("The user " + userName +
                                      " is already a member of the room" +
                                      " or invited");
                return;
            }
            invitedUsers.add(invited);
        }
        for (User invited : invitedUsers) {
            ServerMessageProcessor p = clients.get(invited);
            room.guestInvited(invited);
            p.sendInviteFrom(user, room);
        }
        processor.sendInfo("Invites sent succesfully");
    }

    private void handleJoinRoom(ServerMessageProcessor processor,
                               Message message) {
        String roomName = message.getRoomName();
        if (!rooms.containsKey(roomName)) {
            processor.sendWarning("Room " + roomName + " doesn't exists");
            return;
        }
        Room room = rooms.get(roomName);
        User user = processor.getUser();
        if (room.isGuest(user)) {
            processor.sendWarning("User already a guest in room " + roomName);
            return;
        }
        if (!room.isInvited(user)) {
            processor.sendWarning("User is not invited to room " + roomName);
            return;
        }
        room.guestJoined(user);
        processor.sendInfo("Joined to room " + roomName + " successfully");
        for (User guest : room.guests()) {
            if (user == guest)
                continue;
            ServerMessageProcessor p = clients.get(guest);
            p.sendJoinedRoom(user, room);
        }
    }

    private void handleRoomMessage(ServerMessageProcessor processor,
                                   Message message) {
        User user = processor.getUser();
        String roomName = message.getRoomName();
        if (!rooms.containsKey(roomName)) {
            processor.sendWarning("Room " + roomName + " doesn't exists");
            return;
        }
        Room room = rooms.get(roomName);
        if (!room.isGuest(user)) {
            processor.sendWarning("User is not guest in room " + roomName);
            return;
        }
        for (User guest : room.guests()) {
            if (user == guest)
                continue;
            ServerMessageProcessor p = clients.get(guest);
            p.sendRoomMessageFrom(user, room, message.getMessageContent());
        }
    }

    private void handleLeaveRoom(ServerMessageProcessor processor,
                                 Message message) {
        User user = processor.getUser();
        String roomName = message.getRoomName();
        if (!rooms.containsKey(roomName)) {
            processor.sendWarning("Room " + roomName + " doesn't exists");
            return;
        }
        Room room = rooms.get(roomName);
        if (!room.isGuest(user)) {
            processor.sendWarning("User is not guest in room " + roomName);
            return;
        }
        leftRoom(user, room);
        processor.sendInfo("Left room " + roomName + " successfully");
    }

    private void handleDisconnect(ServerMessageProcessor processor,
                                  Message message) {
        processor.close();
        if (!processor.isIdentified())
            return;
        User user = processor.getUser();
        clients.remove(user);
        users.remove(user);
        identified.remove(user);
        for (Room room : rooms.values()) {
            if (room.isGuest(user))
                leftRoom(user, room);
            if (room.isInvited(user))
                room.guestUninvited(user);
        }
        for (User u : identified) {
            ServerMessageProcessor p = clients.get(u);
            p.sendDisconnected(user);
        }
    }

    private void handleError(ServerMessageProcessor processor,
                             Message message) {
        System.err.println("Error from " + processor.getId() + ": " +
                           message.getMessageContent());
        if (processor.isConnected()) {
            System.err.println("Disconnecting " + processor.getId() + "... ");
            processor.close();
        }
    }
    
    private void handleWarning(ServerMessageProcessor processor,
                               Message message) {
        System.err.println("Warning from " + processor.getId() + ": " +
                           message.getMessageContent());
    }

    private void handleInfo(ServerMessageProcessor processor,
                            Message message) {
        System.err.println("Message from " + processor.getId() + ": " +
                           message.getMessageContent());
    }

    private void leftRoom(User user, Room room) {
        room.guestLeft(user);
        for (User guest : room.guests()) {
            ServerMessageProcessor p = clients.get(guest);
            p.sendLeftRoom(user, room);
        }
    }
}
