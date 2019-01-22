package mx.unam.ciencias.myp.server;

public class User {

    private String name;
    private UserStatus status;

    public User(String name) {
        this.name = name;
        status = UserStatus.ACTIVE;
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

    public int hashCode() {
        return name.hashCode();
    }
}
