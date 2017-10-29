package jsice.network.multishooting.client;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.transform.Affine;
import javafx.stage.Stage;
import jsice.network.multishooting.client.controller.MainViewController;
import jsice.network.multishooting.common.models.Bullet;
import jsice.network.multishooting.client.net.Client;
import jsice.network.multishooting.common.models.GameEntity;
import jsice.network.multishooting.common.models.Tank;
import jsice.network.multishooting.common.models.Wall;
import jsice.network.multishooting.common.net.ClientMessage;
import jsice.network.multishooting.common.net.ClientMessageType;

import java.io.IOException;
import java.util.ArrayList;

public class MainClient extends Application {

    private final int WINDOW_WIDTH = 800;
    private final int WINDOW_HEIGHT = 600;

    private Paint playerColor = Color.BLUE;
    private Paint enemyColor = Color.GREEN;
    private Paint bulletColor = Color.YELLOW;
    private Paint wallColor = Color.BLACK;

    private Tank player;
    private ArrayList<GameEntity> entities;
    private ArrayList<Wall> walls = new ArrayList<>();
    private int score = 0;
    private String topScoreMessage = "";

    private Scene mainScene, gameScene;
    private AnimationTimer animator;
    private GraphicsContext gc;

    private Client client;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        client = new Client("localhost", 13500);
        client.setMain(this);
        client.start();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        mainScene = new Scene(loader.load());
        MainViewController mainViewController = loader.getController();
        mainViewController.setMain(this);


        primaryStage.setTitle("shoot'em");
        primaryStage.setScene(mainScene);
        primaryStage.setResizable(false);
        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> System.exit(0));
    }

    private void setupGameScene() {
        Group root = new Group();
        Canvas canvas = new Canvas(WINDOW_WIDTH, WINDOW_HEIGHT);
        root.getChildren().add(canvas);
        gameScene = new Scene(root);
        gc = canvas.getGraphicsContext2D();

        entities = new ArrayList<>();

        final int[] keyPressed = {0};

        gameScene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                KeyCode key = event.getCode();
                switch (key) {
                    case A: keyPressed[0] |= 1; break;
                    case D: keyPressed[0] |= 2; break;
                    case W: keyPressed[0] |= 4; break;
                    case S: keyPressed[0] |= 8; break;
                    case SPACE: keyPressed[0] |= 16; break;
                }
            }
        });

        gameScene.setOnKeyReleased (new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                KeyCode key = event.getCode();
                switch (key) {
                    case A: keyPressed[0] &= (1<<5)-1 - (1<<0); break;
                    case D: keyPressed[0] &= (1<<5)-1 - (1<<1); break;
                    case W: keyPressed[0] &= (1<<5)-1 - (1<<2); break;
                    case S: keyPressed[0] &= (1<<5)-1 - (1<<3); break;
                    case SPACE: keyPressed[0] &= (1<<5)-1 - (1<<4); break;
                }
            }
        });

        animator = new AnimationTimer() {
            long start = 1L;
            @Override
            public void handle(long now) {
                handleInput(keyPressed[0], now);
                updateAndDraw();
            }

            private void handleInput(int keyPressed, long now) {
                if ((now - start) / 1000000000.0 > 0.5) {
                    if ((keyPressed & 16) != 0) {
                        start = now;
                    }
                } else {
                    keyPressed &= (1<<5)-1 - (1<<4);
                }
                try {
                    if (keyPressed!=0) client.send(new ClientMessage(ClientMessageType.Action, keyPressed));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public void updateAndDraw() {
        gc.setTransform(new Affine());
        gc.setTransform(1, 0, 0, 1, WINDOW_WIDTH/2-player.getX(), WINDOW_HEIGHT/2-player.getY());
        gc.clearRect(player.getX()-WINDOW_WIDTH/2 , player.getY()-WINDOW_HEIGHT/2,  WINDOW_WIDTH, WINDOW_HEIGHT);

        gc.setFill(wallColor);
        for (Wall w: walls) {
            w.draw(gc);
        }
        
        for (GameEntity entity: entities) {
            if (entity instanceof Tank) gc.setFill(enemyColor);
            if (entity instanceof Bullet) gc.setFill(bulletColor);
            entity.draw(gc);
        }
        gc.setFill(playerColor);
        player.draw(gc);

        Affine affine = gc.getTransform();
        gc.setTransform(new Affine());
        gc.setFill(Color.RED);
        gc.setFont(Font.font("System", 36));

        gc.fillText("Score: " + score, 50, 50);
        gc.fillText("- Leaderboard -", 550, 50);

        gc.setFont(Font.font("System", 18));
        String[] scores = topScoreMessage.split("\n");

        for (int i = 0; i < scores.length; i++) {
            gc.fillText(scores[i], 550, 100 + 30*i);
        }
        gc.setTransform(affine);
    }

    public void startGame() {
        setupGameScene();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                ((Stage)mainScene.getWindow()).setScene(gameScene);
            }
        });
        animator.start();
    }

    public void endGame() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                ((Stage)gameScene.getWindow()).setScene(mainScene);
            }
        });
        animator.stop();
    }

    public Client getClient() {
        return client;
    }

    public void setName(String name) {
        this.player.setName(name);
    }

    public void setPlayer(Tank player) {
        this.player = player;
    }

    public void setEntities(ArrayList<GameEntity> entities) {
        this.entities = entities;
    }

    public void setWalls(ArrayList<Wall> walls) {
        this.walls = walls;
    }

    public void loadMap(String mapInfo) throws IOException {
        String[] lines = mapInfo.split("\n");
        String[] firstLine = lines[0].split(" ");
        String[] secondline = lines[1].split(" ");
        int minX = Integer.parseInt(firstLine[0]);
        int minY = Integer.parseInt(firstLine[1]);
        int row = Integer.parseInt(secondline[0]);
        int col = Integer.parseInt(secondline[1]);
        int size = Integer.parseInt(secondline[2]);
        int lc = 2;
        for (int i = 0; i < row; i++) {
            String line = lines[lc];
            lc++;
            for (int j = 0; j < col; j++) {
                char c = line.charAt(j);
                if (c == 'w') {
                    Wall w = new Wall(minX + j*size + size/2, minY + i*size + size/2, size);
                    walls.add(w);
                }
            }
        }
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setTopScoreMessage(String topScoreMessage) {
        this.topScoreMessage = topScoreMessage;
    }
}
