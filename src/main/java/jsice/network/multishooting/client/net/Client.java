package jsice.network.multishooting.client.net;

import jsice.network.multishooting.client.MainClient;
import jsice.network.multishooting.common.models.GameEntity;
import jsice.network.multishooting.common.models.Tank;
import jsice.network.multishooting.common.models.Wall;
import jsice.network.multishooting.common.net.ClientMessage;
import jsice.network.multishooting.common.net.ClientMessageType;
import jsice.network.multishooting.common.net.ServerMessage;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Client extends Thread {

    private MainClient main;

    private DatagramSocket socket;
    private InetAddress serverIP;
    private int serverPort;
    private byte[] buffer = new byte[4096];

    public Client(String host, int port) throws SocketException, UnknownHostException {
        this.socket = new DatagramSocket();
        this.serverIP = InetAddress.getByName(host);
        this.serverPort = port;
    }

    public ServerMessage receive() throws IOException, ClassNotFoundException {
        DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
        socket.receive(receivedPacket);
        return byteArrayToServerMessage(receivedPacket.getData());
    }

    public ServerMessage byteArrayToServerMessage(byte[] bam) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bam));
        return (ServerMessage)ois.readObject();

    }

    public void send(ClientMessage message) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(message);
        byte[] byteArrayMessage = bos.toByteArray();
        DatagramPacket sentPacket = new DatagramPacket(byteArrayMessage, byteArrayMessage.length, serverIP, serverPort);
        socket.send(sentPacket);
    }

    public void setMain(MainClient main) {
        this.main = main;
    }

    @Override
    public void run() {
        while (true) {
            try {
                ServerMessage message = receive();
                System.out.println("From server: " + message);
                switch (message.getType()) {
                    case GameStart: {
                        main.startGame();
                        break;
                    }
                    case MapInfo: {
                        String mapInfo = (String) message.getData();
                        main.loadMap(mapInfo);
                        break;
                    }
                    case TopScore: {
                        String topScore = (String) message.getData();
                        main.setTopScoreMessage(topScore);
                        break;
                    }
                    case YouDead: {
                        main.endGame();
                        break;
                    }
                    case YouKill: {
                        int score = (int) message.getData();
                        main.setScore(score);
                        break;
                    }
                    case UpdateObject: {
                        ArrayList<GameEntity> entities = (ArrayList<GameEntity>) message.getData();
                        main.setEntities(entities);
                        break;
                    }
                    case UpdateYourObject: {
                        Tank t = (Tank) message.getData();
                        main.setPlayer(t);
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

}
