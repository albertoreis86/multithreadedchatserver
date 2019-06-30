package org.academiadecodigo.whiledcards.server;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;


/**
 * @author albertoreis
 */
public class ServerWorker implements Runnable {

    private final Server server;
    private final Socket clientSocket;
    private SimpleDateFormat sdf = new SimpleDateFormat(" HH:mm"); //REPRESENT THE TIME OF THE MESSAGE
    private String username = null;
    private OutputStream outputStream;


    public ServerWorker(Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;

    }

    @Override
    public void run() {

        try {
            handleClientSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClientSocket() throws IOException {

        InputStream inputStream = clientSocket.getInputStream();
        outputStream = clientSocket.getOutputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line;

        String instructions = "/login - set up a nickname\n" + "/logout - closes the connection\n" + "/msg all will send a message to all online users\n" +
                "/msg username - will send a private message for that username\n/instructions - will show these instructions again\n";

        String welcomeMsg="Yo! if you are new type /instructions for instructions.\n";
        outputStream.write(welcomeMsg.getBytes());

        while ((line = reader.readLine()) != null) {

            String[] tokens = StringUtils.split(line); //(splits " " ) //from apache commons lang to help with tokens reading
            String cmd;

            if (tokens != null && tokens.length > 0) {
                cmd = tokens[0];

                if ("logout".equalsIgnoreCase(cmd) || "quit".equalsIgnoreCase(cmd)) {
                    outputStream.write("You have quit this chat\n".getBytes());
                    handleLogout();
                    break;
                } else if ("login".equalsIgnoreCase(cmd)) {
                    handleLogin(outputStream, tokens);
                } else if ("msg".equalsIgnoreCase(cmd)) {
                    String[] tokenMsg = StringUtils.split(line, null, 3);
                    //it will only split untill the 2nd position,
                    // so the tokenmsg[2] will be the entire message and will not split messagebody
                    handleMessages(tokenMsg);
                } else if ("/instructions".equals(cmd)){
                    outputStream.write(instructions.getBytes());

                } else{
                    String msg = "unknown command " + cmd + "\n";
                    outputStream.write(msg.getBytes());

                }
            }

        }

        clientSocket.close();
    }


    private String getTime() {
        return " -> " + sdf.format(new Date());
    }

    //format: "msg" "username" content
    private void handleMessages(String[] tokens) throws IOException {
        String sendTo = tokens[1];
        String content = tokens[2];

        Vector<ServerWorker> workerVector = server.getWorkerVector();

        if (sendTo.equals("all")) {
            for (ServerWorker serverWorker : workerVector) {
                String outMsg = "msg " + username + " " + content + getTime() + "\n";
                serverWorker.send(outMsg);
            }
            return;
        }

        for (ServerWorker worker : workerVector) {
            if (worker.getUsername().equals(sendTo)) {
                String outMsg = "msg " + username + " " + content + getTime() + "\n";
                //text format ex. - @username -> content
                worker.send(outMsg);
            }
        }
    }


    private void handleLogout() throws IOException {
        server.removeWorker(this);

        Vector<ServerWorker> workerVector = server.getWorkerVector();
        //send  other online users current status
        broadcastStatus(username, workerVector, "offline");
        clientSocket.close();

    }

    public String getUsername() {
        return username;
    }

    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException {
        if (tokens.length == 2) {
            String username = tokens[1];
            Vector<ServerWorker> workerVector = server.getWorkerVector();

            for (ServerWorker worker : workerVector) {
                if (!username.equals(worker.getUsername())) {
                    String msg = "Login successful" + getTime() + "\n";
                    outputStream.write(msg.getBytes());
                    this.username = username;
                    System.out.println(username + " logged in - " + sdf.format(new Date()));
                }
            }
            getOnlineUsers(username, workerVector);
            broadcastStatus(username, workerVector, "online");

        } else {
            String msg = "Try again bitch.\n";
            outputStream.write(msg.getBytes());
            System.err.println("Login Unsuccessful " + username);
        }
    }

    private void broadcastStatus(String username, Vector<ServerWorker> workerVector, String status) throws IOException {
        String onlineMsg = status + " " + username + "\n";
        for (ServerWorker worker : workerVector) {//this is to avoid receiving message from himself
            if (!username.equals(worker.getUsername())) {
                worker.send(onlineMsg);
            }
        }
    }

    private void getOnlineUsers(String username, Vector<ServerWorker> workerVector) throws IOException {
        for (ServerWorker worker : workerVector) {
            if (worker.getUsername() != null && (!username.equals(worker.getUsername()))) {
                String msg2 = "online " + worker.getUsername() + "\n";
                send(msg2);
            }
        }
    }

    private void send(String msg) throws IOException {
        if (username != null) {
            outputStream.write(msg.getBytes());
        }

    }


}
