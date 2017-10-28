package jsice.network.multishooting.server.controller;

import javafx.scene.shape.Rectangle;
import jsice.network.multishooting.common.models.Bullet;
import jsice.network.multishooting.common.models.Tank;
import jsice.network.multishooting.common.models.GameEntity;
import jsice.network.multishooting.server.model.PlayerInfo;
import jsice.network.multishooting.server.net.Server;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Random;

public class GameManager extends Thread {

    private Server server;

    private ArrayList<PlayerInfo> playerInfos;
    private ArrayList<GameEntity> gameEntities;
    private ArrayList<Tank> tanks;
    private ArrayList<Bullet> bullets;

    public GameManager() {
        this.playerInfos = new ArrayList<>();
        this.gameEntities = new ArrayList<>();
        this.tanks = new ArrayList<>();
        this.bullets = new ArrayList<>();
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public ArrayList<PlayerInfo> getPlayerInfos() {
        return playerInfos;
    }

    public PlayerInfo getPlayerInfo(InetAddress ip, int port) {
        for (PlayerInfo playerInfo: playerInfos) {
            if (ip.equals(playerInfo.getIp()) && port == playerInfo.getPort()) return playerInfo;
        }
        return null;
    }

    public boolean addPlayer(InetAddress ip, int port, String name, Tank tank) {
        PlayerInfo playerInfo = new PlayerInfo(ip, port, name, tank);
        if (playerInfos.contains(playerInfo)) return false;
        tank.setName(name);
        playerInfos.add(playerInfo);
        tanks.add(tank);
        return true;
    }

    private void shootBulletFrom(Tank tank) {
        Bullet bullet = new Bullet(tank);
        bullets.add(bullet);
    }

    public String getRandomFreeLocation() {
        boolean done = false;
        double randomX = -10000;
        double randomY = -10000;
        int round = 0;
        Random rand = new Random();
        int max = 250;
        int min = -250;
        double width = 60;
        double height = 60;
        while (!done) {
            randomX = rand.nextInt((max - min) + 1) + min;
            randomY = rand.nextInt((max - min) + 1) + min;
            Rectangle playerBox = new Rectangle(randomX, randomY, width, height);
            for (GameEntity entity: gameEntities) {
                if (playerBox.intersects(entity.getShape().getLayoutBounds())) {
                    done = true;
                    break;
                }
            }
            if (round > 40) break;
            round++;
        }

        return String.format("%f %f", randomX, randomY);
    }

    public ArrayList<Bullet> getBullets() {
        return bullets;
    }

    public ArrayList<Tank> getTanks() {
        return tanks;
    }

    private PlayerInfo getPlayerInfoFromTank(Tank t) {
        for (PlayerInfo playerInfo: playerInfos) {
            if (playerInfo.getTank() == t) return playerInfo;
        }
        return null;
    }

    @Override
    public void run() {
        while (true) {
            for (Bullet bullet: bullets) {
                bullet.move();
            }
            for (PlayerInfo playerInfo: playerInfos) {
                Tank t = playerInfo.getTank();
                int action = playerInfo.getAction();
                if ((action & 1) != 0) t.rotateLeft();
                if ((action & 2) != 0) t.rotateRight();
                if ((action & 4) != 0) t.move(0, -1);
                if ((action & 8) != 0) t.move(0, 1);
                if ((action & 16) != 0) shootBulletFrom(t);
                playerInfo.setAction(0);
            }

            try {
                server.sendUpdateObjects();
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (Bullet bullet: bullets) {
                if (!bullet.isHit()) {
                    for (Tank tank : tanks) {
                        if (tank != bullet.getTank()) {
                            if (bullet.getShape().intersects(tank.getShape().getLayoutBounds())) {
                                bullet.setHit(true);
                                tank.setHp(tank.getHp() - 1);
                                if (tank.getHp() == 0) {
                                    Tank killerTank = bullet.getTank();
                                    killerTank.setScore(killerTank.getScore() + 10);
                                    PlayerInfo dead = getPlayerInfoFromTank(tank);
                                    PlayerInfo kill = getPlayerInfoFromTank(killerTank);
                                    try {
                                        server.sendYouDead(dead);
                                        playerInfos.remove(dead);
                                        server.sendYouKill(kill);
                                        killerTank.setHp(Math.min(killerTank.getHp() + 1, killerTank.getMaxHp()));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
                if (!bullet.isHit()) {
                    for (Bullet b : bullets) {
                        if (b != bullet) {
                            if (bullet.getShape().intersects(b.getShape().getLayoutBounds())) {
                                bullet.setHit(true);
                                b.setHit(true);
                                break;
                            }
                        }
                    }
                }
            }
            for (int i = bullets.size() - 1; i >= 0; i--) {
                if (bullets.get(i).isHit()) {
                    bullets.remove(i);
                }
            }
            for (int i = tanks.size() - 1; i >= 0; i--) {
                if (tanks.get(i).getHp() == 0) {
                    tanks.remove(i);
                }
            }
            try {
                sleep(1000/60);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
