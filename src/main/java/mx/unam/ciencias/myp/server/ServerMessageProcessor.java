package mx.unam.ciencias.myp.server;

import java.io.*;
import java.net.*;
import java.util.*;

public class ServerMessageProcessor
    extends MessageProcessor {

    private Server server;

    public ServerMessageProcessor(Server server, Socket socket)
        throws IOException {
        super(socket);
        this.server = server;
    }

    public boolean isIdentified() {
        return user != null;
    }

    public void handleMessage(Message message) {
        server.handle(this, message);
    }

    public void sendNewUser(User user) {
        send(MessageType.NEWUSER + " " + user.getName());
    }

    public void sendUserList(Collection<User> users) {
        String ul = "";
        Iterator<User> it = users.iterator();
        while (it.hasNext()) {
            ul += it.next().getName();
            ul += it.hasNext() ? "," : "";
        }
        send(MessageType.USERLIST + " " + ul);
    }

    public void sendMessageFrom(User user, String message) {
        send(MessageType.MESSAGEFROM + " " +
             user.getName() + " " + message);
    }

    public void sendPublicMessageFrom(User user, String message) {
        send(MessageType.PUBLICMESSAGEFROM + " " +
             user.getName() + " " + message);
    }

    public void sendInviteFrom(User user, Room room) {
        send(MessageType.INVITEFROM + " " +
             user.getName() + " " + room.getName());
    }

    public void sendJoinedRoom(User user, Room room) {
        send(MessageType.JOINEDFROM + " " + user.getName() + " " +
             room.getName());
    }

    public void sendRoomMessageFrom(User user, Room room, String message) {
        send(MessageType.ROOMESSAGEFROM + " " + user.getName() + " " +
             room.getName() + " " + message);
    }

    public void sendLeftRoom(User user, Room room) {
        send(MessageType.LEFTROOM + " " + user.getName() + " " +
             room.getName());
    }

    public void sendDisconnected(User user) {
        send(MessageType.DISCONNECTED + " " + user.getName());
    }
}
