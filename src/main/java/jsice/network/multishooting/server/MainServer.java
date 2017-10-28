package jsice.network.multishooting.server;


import jsice.network.multishooting.server.net.Server;

import java.net.SocketException;
import java.net.UnknownHostException;

public class MainServer {
    public static void main(String[] args) {
        int port = 13500;
        try {
            new Server(port).start();
            System.out.println("Server has started..");
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
