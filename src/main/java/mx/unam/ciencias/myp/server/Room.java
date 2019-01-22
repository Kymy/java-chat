package mx.unam.ciencias.myp.server;

import java.util.*;

public class Room {

    private String name;
    private User owner;
    private Set<User> invited;
    private Set<User> guests;

    public Room(String name, User owner) {
        this.name = name;
        this.owner = owner;
        this.invited = new HashSet<User>();
        this.guests = new HashSet<User>();
        this.guests.add(owner);
    }

    public String getName() {
        return name;
    }

    public void guestInvited(User guest) {
        invited.add(guest);
    }

    public void guestJoined(User guest) {
        if (!invited.contains(guest))
            return;
        invited.remove(guest);
        guests.add(guest);
    }

    public void guestUninvited(User guest) {
        invited.remove(guest);
    }

    public void guestLeft(User guest) {
        guests.remove(guest);
    }

    public Iterable<User> guests() {
        return guests;
    }

    public User getOwner() {
        return owner;
    }

    public boolean isInvited(User user) {
        return invited.contains(user);
    }

    public boolean isGuest(User user) {
        return guests.contains(user);
    }
}
