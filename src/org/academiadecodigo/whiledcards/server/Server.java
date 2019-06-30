package org.academiadecodigo.whiledcards.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
    private final int PORT;

    private SimpleDateFormat sdf = new SimpleDateFormat(" HH:mm"); //REPRESENT THE TIME OF THE MESSAGE

    public Vector<ServerWorker> getWorkerVector() {
        return workerVector;
    }

    private Vector<ServerWorker> workerVector = new Vector<>();

    public Server(int port) {
        this.PORT = port;
    }

    private ExecutorService cachedPool = Executors.newCachedThreadPool();

    @Override
    public void run() {
        try {

            ServerSocket serverSocket = new ServerSocket(PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.print((clientSocket.isConnected() ? "Connection Established " : "Not able to Connect"));
                System.out.println(clientSocket);

                ServerWorker serverWorker = new ServerWorker(this, clientSocket);
                cachedPool.submit(serverWorker);
                workerVector.add(serverWorker);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeWorker(ServerWorker serverWorker) {
        String logoutMsg = "logout: " + serverWorker.getUsername() + " - " + sdf.format(new Date()) + "\n";
        System.out.println(logoutMsg);

        workerVector.remove(serverWorker);
    }
}
