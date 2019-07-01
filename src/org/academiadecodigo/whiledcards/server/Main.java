package org.academiadecodigo.whiledcards.server;

public class Main {

    public static void main(String[] args) {

        int port = 8081;

        Server server = new Server(port);

        new Thread(server,"server").start();


    }

}
