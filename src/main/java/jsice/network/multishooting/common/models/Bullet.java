package jsice.network.multishooting.common.models;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

public class Bullet extends GameEntity {

    private double angle;
    private double speed = 10;
    private double width = 10;
    private double height = 10;
    private AnimationTimer animator;
    private boolean hit = false;
    private Tank tank;
    private int move = 50;

    public Bullet(Tank tank) {
        super(tank.getX(), tank.getY());
        angle = tank.getAngle();
        this.tank = tank;
        double _y = y - tank.getRadius() + 10;
        double _x = x;
        double rotatedX = getRotatedX(_x, _y);
        double rotatedY = getRotatedY(_x, _y);
        x = rotatedX;
        y = rotatedY;
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.fillRect(x - width/2, y - height/2, width, height);
    }

    private void drawRect(GraphicsContext gc, int x, int y, int w, int h) {
        gc.fillRect(x - w/2, y - h/2, w, h);
    }

    private double getRotatedX(double x, double y) {
        x -= this.x;
        y -= this.y;
        double _x = x*Math.cos(angle*Math.PI/180) - y*Math.sin(angle*Math.PI/180);
        return _x + this.x;
    }

    private double getRotatedY(double x, double y) {
        x -= this.x;
        y -= this.y;
        double _y = x*Math.sin(angle*Math.PI/180) + y*Math.cos(angle*Math.PI/180);
        return _y + this.y;
    }


    public void move() {
        if (move > 0) {
            double x = this.x;
            double y = this.y - speed;
            double newx = getRotatedX(x, y);
            double newy = getRotatedY(x, y);
            this.x = newx;
            this.y = newy;
            move--;
            if (move == 0) hit = true;
        }

    }

    @Override
    public Shape getShape() {
        Rectangle rectangle = new Rectangle();
        rectangle.setX(x - width/2);
        rectangle.setY(y - height/2);
        rectangle.setWidth(width);
        rectangle.setHeight(height);
        return rectangle;
    }

    public boolean isHit() {
        return hit;
    }

    public void setHit(boolean hit) {
        this.hit = hit;
    }

    public Tank getTank() {
        return tank;
    }
}
