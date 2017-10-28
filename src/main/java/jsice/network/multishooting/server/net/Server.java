package jsice.network.multishooting.server.net;

import jsice.network.multishooting.common.models.GameEntity;
import jsice.network.multishooting.common.models.Tank;
import jsice.network.multishooting.common.net.ClientMessage;
import jsice.network.multishooting.common.net.ServerMessage;
import jsice.network.multishooting.common.net.ServerMessageType;
import jsice.network.multishooting.server.controller.GameManager;
import jsice.network.multishooting.server.model.PlayerInfo;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Server extends Thread {

    private DatagramSocket socket;
    private byte[] buffer = new byte[4096];

    private GameManager gameManager;

    public Server(int port) throws SocketException, UnknownHostException {
        this.socket = new DatagramSocket(port);
        this.gameManager = new GameManager();
        this.gameManager.setServer(this);
        this.gameManager.start();
    }

    private DatagramPacket receive() throws IOException, ClassNotFoundException {
        DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
        socket.receive(receivedPacket);
        return receivedPacket;
    }

    private ClientMessage byteArrayToClientMessage(byte[] bam) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bam));
        return (ClientMessage)ois.readObject();

    }

    private void send(ServerMessage message, InetAddress receiverIP, int receiverPort) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(message);
        byte[] byteArrayMessage = bos.toByteArray();
        DatagramPacket sentPacket = new DatagramPacket(byteArrayMessage, byteArrayMessage.length, receiverIP, receiverPort);
        socket.send(sentPacket);
    }

    public void sendUpdateObjects() throws IOException {
        for (PlayerInfo playerInfo: gameManager.getPlayerInfos()) {
            InetAddress ip = playerInfo.getIp();
            int port = playerInfo.getPort();
            Tank player = playerInfo.getTank();
            ServerMessage serverMessage = new ServerMessage(ServerMessageType.UpdateYourObject, 301);
            serverMessage.setData(player);
            send(serverMessage, ip, port);

            ArrayList<GameEntity> otherEntities = new ArrayList<>();
            otherEntities.addAll(gameManager.getTanks());
            otherEntities.remove(player);
            otherEntities.addAll(gameManager.getBullets());
            serverMessage = new ServerMessage(ServerMessageType.UpdateObject, 302);
            serverMessage.setData(otherEntities);
            send(serverMessage, ip, port);
        }
    }

    public void sendYouDead(PlayerInfo playerInfo) throws IOException {
        InetAddress ip = playerInfo.getIp();
        int port = playerInfo.getPort();
        ServerMessage serverMessage = new ServerMessage(ServerMessageType.YouDead, 304);
        send(serverMessage, ip, port);
    }

    public void sendYouKill(PlayerInfo playerInfo) throws IOException {
        InetAddress ip = playerInfo.getIp();
        int port = playerInfo.getPort();
        ServerMessage serverMessage = new ServerMessage(ServerMessageType.YouKill, 303);
        send(serverMessage, ip, port);
    }

    @Override
    public void run() {
        while (true) {
            try {
                DatagramPacket receivedPacket = receive();
                InetAddress senderIP = receivedPacket.getAddress();
                int senderPort = receivedPacket.getPort();
                ClientMessage message = byteArrayToClientMessage(receivedPacket.getData());
                System.out.println("From: " + senderIP+":"+ senderPort + " Message: " + message);
                switch (message.getType()) {
                    case Play: {
                        String name = (String) message.getData();
                        String[] location = gameManager.getRandomFreeLocation().split(" ");
                        Tank tank = new Tank(Double.parseDouble(location[0]), Double.parseDouble(location[1]));
                        if (location[0].equals("-10000")) {
                            send(new ServerMessage(ServerMessageType.NoLocation, 402), senderIP, senderPort);
                        } else if (gameManager.addPlayer(senderIP, senderPort, name, tank)) {
                            send(new ServerMessage(ServerMessageType.GameStart, 200), senderIP, senderPort);
                            sendUpdateObjects();
                        } else {
                            send(new ServerMessage(ServerMessageType.DuplicateNameExists, 401), senderIP, senderPort);
                        }
                        break;
                    }
                    case Action: {
                        Integer keyPressed = (Integer) message.getData();
                        PlayerInfo playerInfo = gameManager.getPlayerInfo(senderIP, senderPort);
                        if (playerInfo != null)
                            playerInfo.setAction(keyPressed);

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
