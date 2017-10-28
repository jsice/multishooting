package jsice.network.multishooting.server.model;

import jsice.network.multishooting.common.models.Tank;

import java.net.InetAddress;

public class PlayerInfo {

    private InetAddress ip;
    private int port;
    private String name;
    private Tank tank;
    private int action;

    public PlayerInfo(InetAddress ip, int port, String name, Tank tank) {
        this.ip = ip;
        this.port = port;
        this.name = name;
        this.tank = tank;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PlayerInfo) {
            return ((PlayerInfo) obj).name.equals(name);
        }
        return false;
    }

    public InetAddress getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getName() {
        return name;
    }

    public Tank getTank() {
        return tank;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public int getAction() {
        return action;
    }
}
