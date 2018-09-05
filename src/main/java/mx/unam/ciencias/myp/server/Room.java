package mx.unam.ciencias.myp.server;

import java.util.ArrayList;

public class Room {

    private String nameRoom;
    private ServerThread owner;
    private ArrayList<String>  guests;
    private ArrayList<ServerThread> connectedGuests;


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

    public String addToRoom(ServerThread serverThread) {
        if(!connectedGuests.contains(serverThread)) {
            connectedGuests.add(serverThread);
            return "...SUCCESFULLY JOINED TO ROOM";
        }else {
            return "...ALREADY EXISTS IN ROOM";
        }
    }

    public void sendMessageToGuests(String sender, String message) {
        for (int i=0; i<connectedGuests.size(); i++) {
            ServerThread actual = this.connectedGuests.get(i);
            if (!actual.getUser().getName().equals(sender)) {
                connectedGuests.get(i).send("..." + nameRoom + "-" + sender + ": " + message);
            }
        }
    }

    public String getNameRoom() {
        return  this.nameRoom;
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
