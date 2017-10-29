package jsice.network.multishooting.common.models;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

public class Wall extends GameEntity {

    private double size;

    public Wall(double _x, double _y, double size) {
        super(_x, _y);
        this.size = size;
    }

    @Override
    public Shape getShape() {
        return new Rectangle(x - size/2, y - size/2, size, size);
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.fillRect(x - size/2, y - size/2, size, size);
    }
}
