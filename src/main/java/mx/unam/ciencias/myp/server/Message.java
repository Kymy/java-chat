package mx.unam.ciencias.myp.server;

public class Message {

    private MessageType type;
    private String message = "";
    private String toWhom;

    public Message(String line) {
        String[] parts = line.split(" ");
        switch (parts[0]) {
        case "IDENTIFY":
            if (parts.length < 2) {
                setType(MessageType.INVALID);
                System.err.println("IDENTIFY USERNAME");
            } else {
                setType(MessageType.IDENTIFY);
                setMessage(parts[1]);
            }
            break;
        case "USERS":
            setType(MessageType.USERS);
            break;
        case "DISCONNECT":
            setType(MessageType.DISCONNECT);
            break;
        case "STATUS":
            if (parts.length < 2) {
                setType(MessageType.INVALID);
                System.err.println("STATUS ACTIVE/BUSY/AWAY");
            } else {
                setType(MessageType.STATUS);
                setMessage(parts[1]);
            }
            break;
        case "MESSAGE":
            if (parts.length < 3) {
                setType(MessageType.INVALID);
                System.err.println("MESSAGE USERNAME MESSAGE_CONTENT");
            } else {
                setType(MessageType.MESSAGE);
                setToWhom(parts[1]);
                setMessage(concatMessage(parts, 2));
            }
            break;
        case "PUBLICMESSAGE":
            if (parts.length < 2) {
                setType(MessageType.INVALID);
                System.err.println("PUBLICMESSAGE MESSAGE_CONTENT");
            } else {
                setType(MessageType.PUBLICMESSAGE);
                setMessage(concatMessage(parts, 1));
            }
            break;
        case "CREATEROOM":
            if (parts.length < 2) {
                setType(MessageType.INVALID);
                System.err.println("CREATEROOM ROOM_NAME");
            } else {
                setType(MessageType.CREATEROOM);
                setMessage(parts[1]);
            }
            break;
        case "INVITE":
            if (parts.length < 3) {
                setType(MessageType.INVALID);
                System.err.println("INVITE ROOMNAME USER1 USER2...");
            } else {
                setType(MessageType.INVITE);
                setToWhom(parts[1]);
                setMessage(concatMessage(parts, 2));
            }
            break;
        case "JOINROOM":
            if (parts.length < 2) {
                setType(MessageType.INVALID);
                System.err.println("JOINROOM ROOM_NAME");
            } else {
                setType(MessageType.JOINROOM);
                setMessage(parts[1]);
            }
            break;
        case "ROOMESSAGE":
            if (parts.length < 3) {
                setType(MessageType.INVALID);
                System.err.println("ROOMESSAGE ROOM_NAME MESSAGE_CONTENT");
            } else {
                setType(MessageType.ROOMESSAGE);
                setToWhom(parts[1]);
                setMessage(concatMessage(parts, 2));
            }
            break;
        default:
            setType(MessageType.INVALID);
            break;
        }
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setToWhom(String toWhom) {
        this.toWhom = toWhom;
    }

    public String getToWhom() {
        return toWhom;
    }

    public String toString() {
        String r = type.toString();
        switch (type) {
        case USERS:
        case DISCONNECT:
            break;
        case IDENTIFY:
        case STATUS:
        case PUBLICMESSAGE:
        case CREATEROOM:
        case JOINROOM:
            r += " " + message;
            break;
        case MESSAGE:
        case INVITE:
        case ROOMESSAGE:
            r += " " + toWhom + " " + message;
            break;
        default:
            System.err.println("Invalid message.");
            break;
        }
        return r;
    }

    private String concatMessage(String[] parts, int start) {
        if (start >= parts.length)
            return "";
        String r = parts[start];
        for (int i = start + 1; i < parts.length; i++)
            r += " " + parts[i];
        return r;
    }
}
