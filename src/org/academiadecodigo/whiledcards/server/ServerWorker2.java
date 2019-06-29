package org.academiadecodigo.whiledcards.server;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Vector;


/**
 * @author albertoreis
 */
public class ServerWorker2 implements Runnable {

    private final Server server;
    private final Socket clientSocket;
    private SimpleDateFormat sdf = new SimpleDateFormat(" hh:mm"); //REPRESENT THE TIME OF THE MESSAGE
    private String username = null;
    private OutputStream outputStream;


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
        this.outputStream = clientSocket.getOutputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));


        String line;

        while ((line = reader.readLine()) != null) {
            //from apache commons lang to help with tokens reading
            String[] tokens = StringUtils.split(line); //(splits " " )
            String cmd;

            if (tokens != null && tokens.length > 0) {
                cmd = tokens[0];
                if ("logout".equalsIgnoreCase(cmd)||"quit".equalsIgnoreCase(cmd)) {
                    handleLogout();
                    outputStream.write("You have quit this chat\n".getBytes());
                    break;
                } else if ("login".equalsIgnoreCase(cmd)) {
                    handleLogin(outputStream, tokens);
                } else {
                    String msg = "unknown command " + cmd + "\n";
                    outputStream.write(msg.getBytes());
                }
            }


        }

        clientSocket.close();
    }

    private void handleLogout() throws IOException {
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
            if ((username.equalsIgnoreCase("durukaa") && password.equals("macaco")) ||
                    (username.equalsIgnoreCase("alberto") && password.equals("albertao"))) {
                String msg = "Login successful\n";
                outputStream.write(msg.getBytes());
                this.username = username;
                System.out.println(username + " has logged in");


                Vector<ServerWorker2> workerVector = server.getWorkerVector();
                //send the current user all other online logins
                for (ServerWorker2 worker : workerVector
                ) {
                    if (worker.getUsername() != null) {
                        if (!username.equals(worker.getUsername())) {
                            String mesg2 = "online users: " + worker.getUsername() + "\n";
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
                String msg = username.equalsIgnoreCase("durukaa") ? "Invalid Password" : "Invalid username";
                msg = msg + " please try again\n";
                outputStream.write(msg.getBytes());
            }
        }
    }

    private void send(String messga) throws IOException {
        if (username != null) outputStream.write(messga.getBytes());

    }


}
