package org.academiadecodigo.whiledcards.client;

import java.io.*;
import java.net.Socket;

public class Client {

    private final int PORT;
    private final String servername;
    private Socket socket;
    private InputStream serverIn;
    private OutputStream serverOut;
    private BufferedReader bufferedIn;

    public Client(String servername, int port) {
        this.servername = servername;
        this.PORT = port;
    }

    public static void main(String[] args) throws IOException {
        Client client = new Client("localhost", 8081);
        if (!client.connect()) {
            System.err.println("Connection Failed");
        } else {
            System.out.println("Connection successful");
            if (client.login("gg", "ggdg")){
                System.out.println("login success");
            }else {
                System.err.println("login failed");
            }

        }
    }

    private boolean login(String username, String password) throws IOException {

        String cmd = "login " + username + " " + password + "\n";
        serverOut.write(cmd.getBytes());

        String response = bufferedIn.readLine();
        System.out.println("Response: " + response);

        return response.equals("Login successful");


    }

    private boolean connect() {
        try {
            socket = new Socket(servername, PORT);
            System.out.println("client port is: " + socket.getLocalPort());
            serverOut = socket.getOutputStream();
            serverIn = socket.getInputStream();
            bufferedIn = new BufferedReader(new InputStreamReader(serverIn));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
