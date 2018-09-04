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
    }

    public void inviteGuest(String guestName) {
        if (!guests.contains((guestName))) {
            guests.add(guestName);
        }
    }

    public String getNameRoom() {
        return  this.nameRoom;
    }

    public ServerThread getOwner() {
        return this.owner;
    }


}
