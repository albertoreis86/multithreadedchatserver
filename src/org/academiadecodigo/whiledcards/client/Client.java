package org.academiadecodigo.whiledcards.client;

import javax.sound.sampled.Clip;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

public class Client {

    private final int PORT;
    private final String servername;
    private Socket socket;
    private InputStream serverIn;
    private OutputStream serverOut;

    public Client(String servername, int port) {
        this.servername = servername;
        this.PORT = port;
    }

    public static void main(String[] args) {
        Client client = new Client("localhost", 8081);
        if (!client.connect()) {
            System.err.println("Connection Failed");
        } else System.out.printf("Connection success");
    }

    private boolean connect() {
        try {
            socket = new Socket(servername, PORT);
            serverOut = socket.getOutputStream();
            serverIn = socket.getInputStream();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
