package mx.unam.ciencias.myp.server;

import java.io.*;
import java.net.*;

public abstract class ServerMessageProcessor
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

    public void sendMessageFrom(User user, String message) {
        send(MessageType.MESSAGEFROM + " " +
             user.getName() + " " + message);
    }

    public void sendPublicMessageFrom(User user, String message) {
        send(MessageType.PUBLICMESSAGEFROM + " " +
             user.getName() + " " + message);
    }

    public void sendJoinedRoom(User user, Room room) {
        send(MessageType.JOINEDROOM + " " + user.getName() + " " +
             room.getName());
    }

    public void sendRoomMessageFrom(User user, Room room, String message) {
        send(MessageType.ROOMESSAGEFROM + " " + user.getName() + " " +
             room.getName() + " " + message);
    }

    public void sendDisconnected(User user) {
        send(MessageType.DISCONNECTED + " " + user.getName());
    }
}
