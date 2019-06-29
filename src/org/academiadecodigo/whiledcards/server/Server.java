package org.academiadecodigo.whiledcards.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server implements Runnable {

    private final int PORT;

    private Vector<ServerWorker2> workerVector = new Vector<>();


    public Server(int port) {
        this.PORT=port;
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
                System.out.println((clientSocket.isConnected() ? "Connection Established" : "Not able to Connect"));
                ServerWorker2 serverWorker = new ServerWorker2(this, clientSocket);

                workerVector.add(serverWorker);

                //addin to a list each connection

                Thread serverWorkerThread = new Thread(serverWorker);
                serverWorkerThread.start();

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
