package jsice.network.multishooting.server.controller;

import javafx.scene.shape.Rectangle;
import jsice.network.multishooting.common.models.Bullet;
import jsice.network.multishooting.common.models.Tank;
import jsice.network.multishooting.common.models.GameEntity;
import jsice.network.multishooting.common.models.Wall;
import jsice.network.multishooting.server.model.PlayerInfo;
import jsice.network.multishooting.server.net.Server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.*;

public class GameManager extends Thread {

    private Server server;

    private ArrayList<PlayerInfo> playerInfos;
    private ArrayList<Tank> tanks;
    private ArrayList<Bullet> bullets;
    private ArrayList<Wall> walls;
    private int minX;
    private int maxX;
    private int minY;
    private int maxY;
    private String topScoreText;

    private String mapInfo;

    public GameManager() {
        this.playerInfos = new ArrayList<>();
        this.tanks = new ArrayList<>();
        this.bullets = new ArrayList<>();
        this.walls = new ArrayList<>();

        try {
            loadMapInfo();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        double width = 60;
        double height = 60;
        while (!done) {
            randomX = rand.nextInt((maxX - minX) + 1) + minX;
            randomY = rand.nextInt((maxY - minY) + 1) + minY;
            System.out.printf("%d: %f %f\n",round, randomX, randomY);
            Rectangle playerBox = new Rectangle(randomX, randomY, width, height);
            ArrayList<GameEntity> gameEntities = new ArrayList<>();
            gameEntities.addAll(tanks);
            gameEntities.addAll(bullets);
            gameEntities.addAll(walls);
            if (gameEntities.size() == 0) done = true;
            for (GameEntity entity: gameEntities) {
                if (playerBox.intersects(entity.getShape().getLayoutBounds())) {
                    done = false;
                    break;
                } else {
                    done = true;
                }
            }
            if (round > 40) return "null null";
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

    private void loadMapInfo() throws IOException {
        FileReader fileReader = new FileReader(getClass().getResource("/maps/map.txt").getFile());
        BufferedReader bf = new BufferedReader(fileReader);
        String info = "";
        String[] firstline = bf.readLine().split(" ");
        int row = Integer.parseInt(firstline[0]);
        int col = Integer.parseInt(firstline[1]);
        int size = Integer.parseInt(firstline[2]);
        maxY = (row * size)/2;
        minY = -maxY;
        maxX = (col * size)/2;
        minX = -maxX;
        info += String.format("%d %d\n%d %d %d\n", minX, minY, row, col, size);
        for (int i = 0; i < row; i++) {
            String line = bf.readLine();
            info += line;
            if (i != row - 1) info += "\n";
            for (int j = 0; j < col; j++) {
                char c = line.charAt(j);
                if (c == 'w') {
                    Wall w = new Wall(minX + j*size + size/2, minY + i*size + size/2, size);
                    walls.add(w);
                }
            }
        }
        bf.close();
        mapInfo = info;

    }

    public String getMapInfo() {
        return mapInfo;
    }

    public ArrayList<Wall> getWalls() {
        return walls;
    }

    public void calculateTopScore() {
        ArrayList<PlayerInfo> playerInfos = new ArrayList<>(this.playerInfos);
        Collections.sort(playerInfos, new Comparator<PlayerInfo>() {
            @Override
            public int compare(PlayerInfo o1, PlayerInfo o2) {
                if (o1.getScore() > o2.getScore()) return -1;
                if (o1.getScore() < o2.getScore()) return 1;
                return 0;
            }
        });
        String top = "";
        int size = Math.min(5, playerInfos.size());
        for (int i = 0; i < size; i++) {
            top += (i+1) + ". " + playerInfos.get(i).getName();
            if (i != size - 1) top += "\n";
        }
        topScoreText = top;
    }

    public String getTopScoreText() {
        return topScoreText;
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
                for (Wall w: walls) {
                    if (t.getShape().intersects(w.getShape().getLayoutBounds())) {
                        if ((action & 4) != 0) t.move(0, 1);
                        if ((action & 8) != 0) t.move(0, -1);
                        break;
                    }
                }
                playerInfo.setAction(0);
            }

            try {
                server.sendUpdateObjects();
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (Bullet bullet: bullets) {
                if (!bullet.isHit()) {
                    for (Wall w: walls) {
                        if (bullet.getShape().intersects(w.getShape().getLayoutBounds())) {
                            bullet.setHit(true);
                            break;
                        }
                    }
                }
                if (!bullet.isHit()) {
                    for (Tank tank : tanks) {
                        if (tank != bullet.getTank()) {
                            if (bullet.getShape().intersects(tank.getShape().getLayoutBounds())) {
                                bullet.setHit(true);
                                tank.setHp(tank.getHp() - 1);
                                if (tank.getHp() == 0) {
                                    Tank killerTank = bullet.getTank();
                                    PlayerInfo dead = getPlayerInfoFromTank(tank);
                                    PlayerInfo killer = getPlayerInfoFromTank(killerTank);
                                    killer.setScore(killer.getScore() + 10);
                                    try {
                                        server.sendYouDead(dead);
                                        playerInfos.remove(dead);
                                        server.sendYouKill(killer);
                                        killerTank.setHp(Math.min(killerTank.getHp() + 1, killerTank.getMaxHp()));
                                        calculateTopScore();
                                        server.sendTopScore();
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
