package mx.unam.ciencias.myp.server;

import java.io.Serializable;
public class Message implements Serializable{

    private MessageType type;
    private String message = "";
    private String toWhom;

    public Message() { }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType _type) {
        type = _type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String msg) {
        message = msg;
    }

    public void setToWhom(String msg) {
        toWhom = msg;
    }

    public String getToWhom() {
        return toWhom;
    }

    public void getMessage(String msg) {
        message = msg;
    }

    public String toString() {
        return type + " " + getMessage();
    }

}