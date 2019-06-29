package org.academiadecodigo.whiledcards.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class Server implements Runnable {

    private final int PORT;
    private SimpleDateFormat sdf = new SimpleDateFormat(" HH:mm"); //REPRESENT THE TIME OF THE MESSAGE

    private Vector<ServerWorker2> workerVector = new Vector<>();


    public Server(int port) {
        this.PORT = port;
    }

    public Vector<ServerWorker2> getWorkerVector() {
        return workerVector;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.print((clientSocket.isConnected() ? "Connection Established " : "Not able to Connect"));
                System.out.println(clientSocket);
                ServerWorker2 serverWorker = new ServerWorker2(this, clientSocket);
                workerVector.add(serverWorker);

                Thread serverWorkerThread = new Thread(serverWorker);
                serverWorkerThread.start();

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeWorker(ServerWorker2 serverWorker2) {
        String logoutMsg = "logout: " + serverWorker2.getUsername() + " - " + sdf.format(new Date()) + "\n";
        System.out.println(logoutMsg); //-- this must be written to a logfile

        workerVector.remove(serverWorker2); //it will remove this serverworker from that "list"
    }
}
