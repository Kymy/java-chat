package mx.unam.ciencias.myp.server;

public class User {

    private String name;
    private UserStatus status;

    public User() {}

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public UserStatus getStatus() {
        return status;
    }

    public String toString() {
        return name + ": " + status;
    }

}
