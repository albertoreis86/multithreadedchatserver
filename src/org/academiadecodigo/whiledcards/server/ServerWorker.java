package org.academiadecodigo.whiledcards.server;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class ServerWorker implements Runnable {
    Socket clientSocket;

    SimpleDateFormat sdf = new SimpleDateFormat(" hh:mm"); //REPRESENT THE TIME OF THE MESSAGE


    public ServerWorker(Socket clientSocket) {
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
        OutputStream outputStream = clientSocket.getOutputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String nick = getNickName(outputStream, reader);

        String line;
        while ((line = reader.readLine()) != null) {

            if ("/quit".equalsIgnoreCase(line)) {
                outputStream.write("You have quit this chat\n".getBytes());
                break;
            }
            if ("/list".equalsIgnoreCase(line)) {
                outputStream.write("it will apear a list here...\n".getBytes());
                continue;
            }
            if ("/alias".equalsIgnoreCase(line)) {
                outputStream.write("select a new nickname: ".getBytes());
                nick = reader.readLine();
                outputStream.write(("your nickname was changed to ->" + nick + "\n").getBytes());
                continue;
            }
            String message = nick + "--> " + line + sdf.format(new Date()) + "\n"; //why need a breakline by the end?
            outputStream.write(message.getBytes());
        }

        clientSocket.close();
    }


    private String getNickName(OutputStream outputStream, BufferedReader reader) throws IOException {

        //Vector<String> users = new Vector<>();
        //users.add(getNickName(outputStream, reader));

        String userName;
        outputStream.write("type a nickname: ".getBytes());
        userName = reader.readLine();
        System.out.println(userName + "was added to chat");
        return userName;
    }
}
