package mx.unam.ciencias.myp.server;

import java.net.*;
import java.io.*;
import java.util.*;

public class ServerApp {

    private static void error(String message) {
        System.err.println(message);
        System.exit(1);
    }

    public static void main(String args[]) {
        if (args.length != 1)
            error("Use: java Server port");
        int port = -1;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException nfe) {
            error("Invalid port " + args[0]);
        }
        if (port < 1024 || port > 65535)
            error("Invalid port " + port);
        Server server = null;
        try {
            server = new Server(port);
        } catch (IOException ioe) {
            error("Could not start server on port " + port);
        }
        server.start();
    }
}
