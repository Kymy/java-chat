package mx.unam.ciencias.myp.server;

import java.util.ArrayList;

public class Room {

    private String nameRoom;
    private ServerThread owner; // the server thread that contains the reference to the user
    private ArrayList<String>  guests;
    private ArrayList<ServerThread> connectedGuests; // all the connected threads to the room

    /**
     * Builds the room from its name and the owner
     * also initialize properties
     * @param name room name
     * @param owner owner thread
     */
    public Room(String name, ServerThread owner) {
        this.nameRoom = name;
        this.owner = owner;
        this.guests = new ArrayList<String>();
        this.connectedGuests = new ArrayList<ServerThread>();
        this.connectedGuests.add(owner);
    }

    public void inviteGuest(String guestName) {
        if (!guests.contains((guestName))) {
            guests.add(guestName);
        }
    }

    /**
     * Add user to connected user list
     * @param serverThread server thread that contains the reference to the user
     * @return a string that indicates whether it joined or if it was already joined
     */
    public String addToRoom(ServerThread serverThread) {
        if(!connectedGuests.contains(serverThread)) {
            connectedGuests.add(serverThread);
            return "...JOINED_TO_ROOM";
        }else {
            return "...ALREADY_IN_ROOM";
        }
    }

    public void sendMessageToGuests(String sender, String message) {
        for (int i=0; i<connectedGuests.size(); i++) {
            ServerThread actual = this.connectedGuests.get(i);
            if (!actual.getUser().getName().equals(sender)) {
                connectedGuests.get(i).send("...ROOM_MSG_FROM " + nameRoom + "-" + sender + ": " + message);
            }
        }
    }

    public ServerThread getOwner() {
        return this.owner;
    }

    public boolean isUserInvited(String username) {
        for (int i=0; i<guests.size(); i++) {
            if (guests.get(i).equals(username)) {
                return true;
            }
        }
        return false;
    }

    public boolean isTheOwner(String username) {
        return owner.getUser().getName().equals(username);
    }

}
