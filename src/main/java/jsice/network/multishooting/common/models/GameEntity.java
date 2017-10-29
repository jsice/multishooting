package jsice.network.multishooting.common.models;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Shape;

import java.io.Serializable;
/**
 * Wiwadh Chinanuphandh
 * 5810400051
 */
public abstract class GameEntity implements Serializable {

    protected int id;
    protected double x;
    protected double y;

    public GameEntity(double _x, double _y) {
        x = _x;
        y = _y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public abstract Shape getShape();
    public abstract void draw(GraphicsContext gc);
}
