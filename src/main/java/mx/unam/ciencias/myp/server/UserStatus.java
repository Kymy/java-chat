package mx.unam.ciencias.myp.server;

public enum UserStatus {
    ACTIVE, AWAY, BUSY;

    public static UserStatus getUserStatus(String status) {
        switch (status) {
        case "ACTIVE": return ACTIVE;
        case "AWAY":   return AWAY;
        case "BUSY":   return BUSY;
        default:       return null;
        }
    }
}
