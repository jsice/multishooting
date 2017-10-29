package jsice.network.multishooting.common.models;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
/**
 * Wiwadh Chinanuphandh
 * 5810400051
 */
public class Tank extends GameEntity {

    private String name = "noname";
    private double maxHp = 5;
    private double hp = maxHp;
    private double radius = 20;
    private double angle = 0;
    private double speed = 4;

    public Tank(double _x, double _y) {
        super(_x, _y);
    }

    @Override
    public void draw(GraphicsContext gc) {
        drawBody(gc);
        drawEyes(gc);
        drawHPBar(gc);
        drawName(gc);
    }

    private void drawBody(GraphicsContext gc) {
        double bodyX = x;
        double bodyY = y;
        drawCircle(gc, getRotatedX(bodyX, bodyY), getRotatedY(bodyX, bodyY), radius);
    }

    private void drawEyes(GraphicsContext gc) {
        double leftEyeX = x - 10;
        double leftEyeY = y - 12.5;
        double rightEyeX = x + 10;
        double rightEyeY = y - 12.5;
        gc.setFill(Color.RED);
        drawCircle(gc, getRotatedX(leftEyeX, leftEyeY), getRotatedY(leftEyeX, leftEyeY), 2.5);
        drawCircle(gc, getRotatedX(rightEyeX, rightEyeY), getRotatedY(rightEyeX, rightEyeY), 2.5);
    }

    private void drawName(GraphicsContext gc) {
        gc.setFill(Color.BLACK);
        gc.setFont(new Font("System", 15));
        gc.fillText(name, x - radius, y - radius - 20);
    }

    private void drawHPBar(GraphicsContext gc) {
        gc.setFill(Color.RED);
        gc.fillRect(x - radius, y - radius - 10, radius*2 * hp/maxHp, 5);
        gc.setFill(Color.BLACK);
        gc.strokeRect(x - radius, y - radius - 10, radius*2, 5);
    }

    private void drawCircle(GraphicsContext gc, double centerX, double centerY, double radius) {
        gc.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
    }

    public void rotateRight() {
        angle += 2;
    }

    public void rotateLeft() {
        angle -= 2;
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

    public void move(double _x, double _y) {
        double x = this.x + _x * speed;
        double y = this.y + _y * speed;
        double newx = getRotatedX(x, y);
        double newy = getRotatedY(x, y);
        this.x = newx;
        this.y = newy;
    }

    public double getAngle() {
        return angle;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Shape getShape() {
        Circle circle = new Circle();
        circle.setCenterX(x);
        circle.setCenterY(y);
        circle.setRadius(radius);
        return circle;
    }

    public void setHp(double hp) {
        this.hp = hp;
    }

    public double getHp() {
        return hp;
    }

    public double getRadius() {
        return radius;
    }

    public double getMaxHp() {
        return maxHp;
    }
}
