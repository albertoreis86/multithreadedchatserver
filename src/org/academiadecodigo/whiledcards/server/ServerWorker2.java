package org.academiadecodigo.whiledcards.server;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Vector;


/**
 * @author albertoreis
 */
public class ServerWorker2 implements Runnable {

    private final Server server;
    private final Socket clientSocket;
    private SimpleDateFormat sdf = new SimpleDateFormat(" HH:mm"); //REPRESENT THE TIME OF THE MESSAGE
    private String username = null;
    private OutputStream outputStream;
    private HashSet<String> channelset = new HashSet<>();


    public ServerWorker2(Server server, Socket clientSocket) {
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
        //BLOQUEADO AQUI
        while ((line = reader.readLine()) != null) {
            //from apache commons lang to help with tokens reading
            String[] tokens = StringUtils.split(line); //(splits " " )
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
                } else if ("join".equals(cmd)) {
                    handleJoin(tokens);

                } else if ("leave".equals(cmd)) {
                    handleLeave(tokens);
                } else {
                    String msg = "unknown command " + cmd + "\n";
                    outputStream.write(msg.getBytes());

                }
            }


        }

        clientSocket.close();
    }

    private void handleLeave(String[] tokens) {
        if (tokens.length > 1) {
            String channel = tokens[1];
            channelset.remove(channel);
            //it will remove the channel, not leave it
        }
    }

    boolean isChannelMember(String topic) {
        return channelset.contains(topic);
    }

    private void handleJoin(String[] tokens) {
        if (tokens.length > 1) {
            String channel = tokens[1];
            channelset.add(channel);

        }
    }

    //format: "msg" "username" content
    //format: "msg" "#channel" content

    private void handleMessages(String[] tokens) throws IOException {
        String sendTo = tokens[1];
        String content = tokens[2];

        boolean ischannel = sendTo.split("")[0].equals("#");


        Vector<ServerWorker2> workerVector = server.getWorkerVector();

        for (ServerWorker2 worker : workerVector) {
            if (ischannel) {
                if (worker.isChannelMember(sendTo)) {
                    //message + channel + content
                    String outMsg = sendTo + " @" + username + " -> " + content + "\n";
                    //text format ex. - #channel @username -> content
                    worker.send(outMsg);
                }

            } else {
                if (worker.getUsername().equals(sendTo)) {
                    String outMsg = "@" + username + " -> " + content + "\n";
                    //text format ex. - @username -> content
                    worker.send(outMsg);
                }
            }
        }


    }

    private void handleLogout() throws IOException {
        server.removeWorker(this);

        Vector<ServerWorker2> workerVector = server.getWorkerVector();
        //send  other online users current status
        String onlineMsg = username + " is Offline\n";
        for (ServerWorker2 worker : workerVector) {//this is to avoid receiving message from himself
            if (!username.equals(worker.getUsername())) {
                worker.send(onlineMsg);
            }
        }
        clientSocket.close();

    }

    public String getUsername() {
        return username;
    }

    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException {
        if (tokens.length == 3) {
            String username = tokens[1];
            String password = tokens[2];
            if ((username.equalsIgnoreCase("dudu") && password.equals("macaco")) ||
                    (username.equalsIgnoreCase("alberto") && password.equals("albertao")) ||
                    (username.equalsIgnoreCase("gg") && password.equals("ggg"))) {
                String msg = "Login successful\n";
                outputStream.write(msg.getBytes());
                this.username = username;
                System.out.println(username + " logged in - " + sdf.format(new Date()));


                Vector<ServerWorker2> workerVector = server.getWorkerVector();
                //send the current user all other online logins
                for (ServerWorker2 worker : workerVector
                ) {
                    if (worker.getUsername() != null) {
                        if (!username.equals(worker.getUsername())) {
                            String mesg2 = worker.getUsername() + " is online \n";
                            send(mesg2);
                        }
                    }
                }
                //send  other online users current status
                String onlineMsg = username + " is online\n";
                for (ServerWorker2 worker : workerVector) {//this is to avoid receiving message from himself
                    if (!username.equals(worker.getUsername())) {
                        worker.send(onlineMsg);
                    }
                }

            } else {
               // String msg = username.equalsIgnoreCase("durukaa") ? "Invalid Password" : "Invalid username";
                String msg = "Login Failed please try again\n";
                outputStream.write(msg.getBytes());
                System.err.println("Login Unsuccessful " + username);
            }
        }
    }

    private void send(String messga) throws IOException {
        System.out.println(messga);
        if (username != null){
            outputStream.write(messga.getBytes());
        }

    }


}
