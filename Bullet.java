import java.awt.*;

public class Bullet {
    private int x, y;
    protected Direction direction;
    private boolean fromPlayer;
    private boolean isLaserBullet;
    private static final int SPEED = 10;
    private static final int SIZE = 10;
    private boolean active = true;

    public Bullet(int x, int y, Direction direction, boolean fromPlayer) {
        this(x, y, direction, fromPlayer, false);
    }

    public Bullet(int x, int y, Direction direction, boolean fromPlayer, boolean isLaserBullet) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.fromPlayer = fromPlayer;
        this.isLaserBullet = isLaserBullet;
    }

    public void update() {
        switch (direction) {
            case UP:
                y -= SPEED;
                break;
            case DOWN:
                y += SPEED;
                break;
            case LEFT:
                x -= SPEED;
                break;
            case RIGHT:
                x += SPEED;
                break;
        }
    }

    public void draw(Graphics g) {
        g.setColor(fromPlayer ? Color.GREEN : Color.RED);
        g.fillOval(x, y, SIZE, SIZE);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, SIZE, SIZE);
    }

    public boolean isFromPlayer() {
        return fromPlayer;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isLaserBullet() {
        return isLaserBullet;
    }

    public Direction getDirection() { return direction; }
    public void setDirection(Direction direction) { this.direction = direction; }
} 