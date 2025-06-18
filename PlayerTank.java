import java.awt.*;

public class PlayerTank extends Tank {
    public PlayerTank(int x, int y, Direction direction) {
        super(x, y, direction);
        setLife(3);
    }

    @Override
    public void update() {
        move();
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(Color.BLUE);
        g.fillRect(x, y, SIZE, SIZE);
        g.setColor(Color.CYAN);
        // 绘制炮管
        switch (direction) {
            case UP:
                g.fillRect(x + SIZE / 2 - 2, y - 5, 4, SIZE / 2 + 5);
                break;
            case DOWN:
                g.fillRect(x + SIZE / 2 - 2, y + SIZE / 2, 4, SIZE / 2 + 5);
                break;
            case LEFT:
                g.fillRect(x - 5, y + SIZE / 2 - 2, SIZE / 2 + 5, 4);
                break;
            case RIGHT:
                g.fillRect(x + SIZE / 2, y + SIZE / 2 - 2, SIZE / 2 + 5, 4);
                break;
        }
        // 绘制履带
        g.setColor(Color.DARK_GRAY);
        if (direction == Direction.UP || direction == Direction.DOWN) {
            g.fillRect(x - 5, y, 5, SIZE);
            g.fillRect(x + SIZE, y, 5, SIZE);
        } else {
            g.fillRect(x, y - 5, SIZE, 5);
            g.fillRect(x, y + SIZE, SIZE, 5);
        }
    }

    public void takeDamage() {
        life--;
    }
} 