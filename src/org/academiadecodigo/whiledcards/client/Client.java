package org.academiadecodigo.whiledcards.client;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.Vector;

public class Client {

    public static void main(String[] args) throws IOException {
        Client client = new Client("localhost", 8081);
        client.start();
    }

    private final int PORT;
    private final String servername;
    private Socket socket;
    private InputStream serverIn;
    private OutputStream serverOut;

    private Scanner scanner = new Scanner(System.in);

    private BufferedReader bufferedIn;

    private Vector<ClientStatusListener> clientStatuses = new Vector<>();
    private Vector<MessageListener> messageListeners = new Vector<>();


    public Client(String servername, int port) {
        this.servername = servername;
        this.PORT = port;
    }

    private void start() throws IOException {

        addClientStatusListener(new ClientStatusListener() {
            @Override
            public void onLine(String username) {
                System.out.println("ONLINE " + username);
            }

            @Override
            public void offLine(String username) {
                System.out.println("OFFLINE " + username);
            }
        });

        addMessageListener(new MessageListener() {
            @Override
            public void onMessage(String fromUser, String content) {
                System.out.println("@" + fromUser + " --> " + content);
            }
        });

        if (!connect()) {
            System.err.println("Connection Failed");
        } else {
            System.out.println("Connection Established");
            String username, password;
            System.out.println("username: " + (username = scanner.nextLine()) + " password: " + (password = scanner.nextLine()));
            if (login(username, password)) {

                startMessageReader();
                System.out.println("type an username to send a private " + "message or type all to send to everyone online ");
                while (true) {
                    String dest, content;

                    System.out.println("MessageTo: " + (dest = scanner.next()) + " message: " + (content = scanner.next()));

                    message(dest, content);
                }

            } else {
                System.err.println("login failed");
            }
            //logout();
        }
    }

    private void message(String sendTo, String content) throws IOException {
        String cmd = "msg " + sendTo + ' ' + content + "\n";
        serverOut.write(cmd.getBytes());
    }


    private void startMessageReader() {
        Thread readerThread = new Thread() {
            @Override
            public void run() {
                readMessageLoop();
            }
        };
        readerThread.start();
    }

    private void readMessageLoop() {
        String line;
        try {
            while ((line = bufferedIn.readLine()) != null) {
                String[] tokens = StringUtils.split(line); //(splits " " )
                String cmd;

                if (tokens != null && tokens.length > 0) {
                    cmd = tokens[0];
                    if (cmd.equals("online")) {
                        handleOnline(tokens);
                    } else if (cmd.equals("offline")) {
                        handleOffline(tokens);
                    } else if ("msg".equalsIgnoreCase(cmd)) {
                        String[] tokenMsg = StringUtils.split(line, null, 3);
                        handleMessage(tokenMsg);

                    }
                }


            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void handleMessage(String[] tokenMsg) {
        String username = tokenMsg[1];
        String content = tokenMsg[2];

        for (MessageListener listener : messageListeners) {
            listener.onMessage(username, content);
        }

    }

    private void handleOffline(String[] tokens) {
        String login = tokens[1];
        for (ClientStatusListener status : clientStatuses) {
            status.offLine(login);

        }
    }

    private void handleOnline(String[] tokens) {
        String login = tokens[1];
        for (ClientStatusListener status : clientStatuses) {
            status.onLine(login);

        }
    }

    private boolean login(String username, String password) throws IOException {

        String cmd = "login " + username + " " + password + "\n";
        serverOut.write(cmd.getBytes());

        String response = bufferedIn.readLine();
        System.out.println("Server response: " + response);

        return response.equals("Login successful");
    }

    private void logout() throws IOException {
        String cmd = "logout\n";
        serverOut.write(cmd.getBytes());

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

    public void addClientStatusListener(ClientStatusListener clientStatusListener) {
        clientStatuses.add(clientStatusListener);
    }

    public void removeClientStatusListener(ClientStatusListener clientStatusListener) {
        clientStatuses.remove(clientStatusListener);

    }

    public void addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
    }

    public void removeMessageListener(MessageListener listener) {
        messageListeners.remove(listener);
    }
}
