package mx.unam.ciencias.myp.server;

public class Message {

    private MessageType type;
    private String userName;
    private String roomName;
    private String state;
    private String messageContent;

    public Message(String line) {
        String[] parts = line.split(" ");
        if (parts.length == 0)
            parts = new String[]{ "INVALID" };
        switch (parts[0]) {
        case "IDENTIFY":          defineIdentify(parts);          break;
        case "STATUS":            defineStatus(parts);            break;
        case "USERS":             defineUsers(parts);             break;
        case "MESSAGE":           defineMessage(parts);           break;
        case "PUBLICMESSAGE":     definePublicMessage(parts);     break;
        case "CREATEROOM":        defineCreateRoom(parts);        break;
        case "INVITE":            defineInvite(parts);            break;
        case "JOINROOM":          defineJoinRoom(parts);          break;
        case "ROOMESSAGE":        defineRoomMessage(parts);       break;
        case "LEAVEROOM":         defineLeaveRoom(parts);         break;
        case "DISCONNECT":        defineDisconnect(parts);        break;
        case "NEWUSER":           defineNewUser(parts);           break;
        case "MESSAGEFROM":       defineMessageFrom(parts);       break;
        case "PUBLICMESSAGEFROM": definePublicMessageFrom(parts); break;
        case "JOINEDROOM":        defineJoinedFrom(parts);        break;
        case "ROOMESSAGEFROM":    defineRoomMessageFrom(parts);   break;
        case "DISCONNECTED":      defineDisconnected(parts);      break;
        case "WARNING":           defineWarning(parts);           break;
        case "ERROR":             defineError(parts);             break;
        default:                  defineInvalid(parts[0]);        break;
        }
    }

    public MessageType getType() {
        return type;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public String getUserName() {
        return userName;
    }

    public String getRoomName() {
        return roomName;
    }

    private void defineIdentify(String[] parts) {
        if (parts.length != 2) {
            type = MessageType.INVALID;
            messageContent = "IDENTIFY has 1 parameter";
        } else {
            type = MessageType.IDENTIFY;
            userName = parts[1];
        }
    }

    private void defineStatus(String[] parts) {
        if (parts.length != 2) {
            type = MessageType.INVALID;
            messageContent = "STATUS has 1 parameter";
        } else {
            type = MessageType.STATUS;
            messageContent = parts[1];
        }
    }

    private void defineUsers(String[] parts) {
        if (parts.length != 2) {
            type = MessageType.INVALID;
            messageContent = "USERS has no parameters";
        } else {
            type = MessageType.USERS;
        }
    }

    private void defineMessage(String[] parts) {
        if (parts.length < 3) {
            type = MessageType.INVALID;
            messageContent = "MESSAGE has 2 or more parameters";
        } else {
            type = MessageType.MESSAGE;
            userName = parts[1];
            messageContent = rebuildMessage(parts, 2);
        }
    }

    private void definePublicMessage(String[] parts) {
        if (parts.length < 2) {
            type = MessageType.INVALID;
            messageContent = "PUBLICMESSAGE has 2 or more parameters";
        } else {
            type = MessageType.PUBLICMESSAGE;
            messageContent = rebuildMessage(parts, 1);
        }
    }

    private void defineCreateRoom(String[] parts) {
        if (parts.length != 2) {
            type = MessageType.INVALID;
            messageContent = "CREATEROOM has 1 parameter";
        } else {
            type = MessageType.CREATEROOM;
            roomName = parts[1];
        }
    }

    private void defineInvite(String[] parts) {
        if (parts.length != 3) {
            type = MessageType.INVALID;
            messageContent = "INVITE has 2 parameters";
        } else {
            type = MessageType.INVITE;
            userName = parts[1];
            roomName = parts[2];
            messageContent = rebuildMessage(parts, 2);
        }
    }

    private void defineJoinRoom(String[] parts) {
        if (parts.length != 2) {
            type = MessageType.INVALID;
            messageContent = "JOINROOM has 1 parameter";
        } else {
            type = MessageType.JOINROOM;
            roomName = parts[1];
        }
    }

    private void defineRoomMessage(String[] parts) {
        if (parts.length < 3) {
            type = MessageType.INVALID;
            messageContent = "ROOMESSAGE has 2 or more parameters";
        } else {
            type = MessageType.ROOMESSAGE;
            roomName = parts[1];
            messageContent = rebuildMessage(parts, 2);
        }
    }

    private void defineLeaveRoom(String[] parts) {
        if (parts.length != 2) {
            type = MessageType.INVALID;
            messageContent = "LEAVEROOM has 1 parameter";
        } else {
            type = MessageType.LEAVEROOM;
            roomName = parts[1];
        }
    }

    private void defineDisconnect(String[] parts) {
        if (parts.length != 1) {
            type = MessageType.INVALID;
            messageContent = "DISCONNECT has no parameters";
        } else {
            type = MessageType.DISCONNECT;
        }
    }

    private void defineNewUser(String[] parts) {
        if (parts.length != 2) {
            type = MessageType.INVALID;
            messageContent = "NEWUSER has 1 parameter";
        } else {
            type = MessageType.NEWUSER;
            userName = parts[1];
        }
    }

    private void defineMessageFrom(String[] parts) {
        if (parts.length < 3) {
            type = MessageType.INVALID;
            messageContent = "MESSAGEFROM has 2 or more parameters";
        } else {
            type = MessageType.MESSAGEFROM;
            userName = parts[1];
            messageContent = rebuildMessage(parts, 2);
        }
    }

    private void definePublicMessageFrom(String[] parts) {
        if (parts.length < 3) {
            type = MessageType.INVALID;
            messageContent = "PUBLICMESSAGEFROM has 2 or more parameters";
        } else {
            type = MessageType.PUBLICMESSAGEFROM;
            userName = parts[1];
            messageContent = rebuildMessage(parts, 2);
        }
    }

    private void defineJoinedFrom(String[] parts) {
        if (parts.length != 3) {
            type = MessageType.INVALID;
            messageContent = "JOINEDFROM has 2 parameters";
        } else {
            type = MessageType.JOINEDROOM;
            userName = parts[1];
            roomName = parts[2];
        }
    }

    private void defineRoomMessageFrom(String[] parts) {
        if (parts.length < 4) {
            type = MessageType.INVALID;
            messageContent = "MESSAGEFROM has 3 or more parameters";
        } else {
            type = MessageType.MESSAGEFROM;
            userName = parts[1];
            roomName = parts[2];
            messageContent = rebuildMessage(parts, 3);
        }
    }

    private void defineDisconnected(String[] parts) {
        if (parts.length != 2) {
            type = MessageType.INVALID;
            messageContent = "DISCONNECT has 1 parameter";
        } else {
            type = MessageType.DISCONNECT;
            userName = parts[1];
        }
    }

    private void defineWarning(String[] parts) {
        if (parts.length < 2) {
            type = MessageType.INVALID;
            messageContent = "WARNING has 1 or more parameters";
        } else {
            type = MessageType.WARNING;
            messageContent = rebuildMessage(parts, 1);
        }
    }

    private void defineError(String[] parts) {
        if (parts.length < 2) {
            type = MessageType.INVALID;
            messageContent = "ERROR has 1 or more parameters";
        } else {
            type = MessageType.ERROR;
            messageContent = rebuildMessage(parts, 1);
        }
    }

    private void defineInvalid(String message) {
        type = MessageType.INVALID;
        messageContent = "Message not recognized: " + message;
    }

    private String rebuildMessage(String[] parts, int start) {
        if (start >= parts.length)
            return "";
        String r = parts[start];
        for (int i = start; i < parts.length; i++)
            r += " " + parts[i];
        return r;
    }
}
