import java.awt.*;

public abstract class Tank {
    protected int x, y;
    protected Direction direction;
    protected boolean moving;
    protected int life;
    protected static final int SPEED = 5;
    protected static final int SIZE = 26;

    public Tank(int x, int y, Direction direction) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.moving = false;
        this.life = 1;
    }

    public abstract void update();
    public abstract void draw(Graphics g);

    protected void move() {
        if (!moving) return;
        int newX = x;
        int newY = y;
        switch (direction) {
            case UP: newY -= SPEED; break;
            case DOWN: newY += SPEED; break;
            case LEFT: newX -= SPEED; break;
            case RIGHT: newX += SPEED; break;
        }

        // 边界检查
        if (newX < 0 || newX > GamePanel.PANEL_WIDTH - SIZE || newY < 0 || newY > GamePanel.PANEL_HEIGHT - SIZE) {
            return; // 如果超出边界，则不允许移动
        }

        Rectangle newBounds = new Rectangle(newX, newY, SIZE, SIZE);

        // 检查与墙壁的碰撞
        for (Rectangle wall : GamePanel.walls) {
            if (newBounds.intersects(wall)) {
                return; // 不能移动到墙壁内部
            }
        }

        // 检查与其他坦克的碰撞
        // 如果是玩家坦克
        if (this instanceof PlayerTank) {
            for (EnemyTank otherTank : GamePanel.enemyTanks) {
                if (newBounds.intersects(otherTank.getBounds())) {
                    return; // 不能移动到敌方坦克内部
                }
            }
        }
        // 如果是敌方坦克
        else if (this instanceof EnemyTank) {
            // 检查与玩家坦克的碰撞
            if (GamePanel.playerTank != null && newBounds.intersects(GamePanel.playerTank.getBounds())) {
                return; // 不能移动到玩家坦克内部
            }
            // 检查与其他敌方坦克的碰撞
            for (EnemyTank otherTank : GamePanel.enemyTanks) {
                if (otherTank != this && newBounds.intersects(otherTank.getBounds())) {
                    return; // 不能移动到另一个敌方坦克内部
                }
            }
        }

        // 如果没有碰撞，更新位置
        x = newX;
        y = newY;
    }

    public boolean canMove(Direction testDirection) {
        int newX = x;
        int newY = y;

        switch (testDirection) {
            case UP: newY -= SPEED; break;
            case DOWN: newY += SPEED; break;
            case LEFT: newX -= SPEED; break;
            case RIGHT: newX += SPEED; break;
        }

        // 边界检查
        if (newX < 0 || newX > GamePanel.PANEL_WIDTH - SIZE || newY < 0 || newY > GamePanel.PANEL_HEIGHT - SIZE) {
            return false;
        }

        Rectangle newBounds = new Rectangle(newX, newY, SIZE, SIZE);

        // 检查与墙壁的碰撞
        for (Rectangle wall : GamePanel.walls) {
            if (newBounds.intersects(wall)) {
                return false;
            }
        }

        // 检查与其他坦克的碰撞
        if (this instanceof PlayerTank) {
            for (EnemyTank otherTank : GamePanel.enemyTanks) {
                // 确保不检查自身与自身碰撞
                if (newBounds.intersects(otherTank.getBounds())) {
                    return false;
                }
            }
        } else if (this instanceof EnemyTank) {
            // 检查与玩家坦克的碰撞
            if (GamePanel.playerTank != null && newBounds.intersects(GamePanel.playerTank.getBounds())) {
                return false;
            }
            // 检查与其他敌方坦克的碰撞
            for (EnemyTank otherTank : GamePanel.enemyTanks) {
                if (otherTank != this && newBounds.intersects(otherTank.getBounds())) {
                    return false;
                }
            }
        }
        return true;
    }

    public Bullet shoot() {
        int bulletX = x;
        int bulletY = y;
        
        switch (direction) {
            case UP:
                bulletX += SIZE / 2 - 2;
                bulletY -= 10;
                break;
            case DOWN:
                bulletX += SIZE / 2 - 2;
                bulletY += SIZE;
                break;
            case LEFT:
                bulletX -= 10;
                bulletY += SIZE / 2 - 2;
                break;
            case RIGHT:
                bulletX += SIZE;
                bulletY += SIZE / 2 - 2;
                break;
        }
        
        return new Bullet(bulletX, bulletY, direction, this instanceof PlayerTank);
    }

    public void hit() {
        life--;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, SIZE, SIZE);
    }

    // Getters and setters
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public Direction getDirection() { return direction; }
    public void setDirection(Direction direction) { this.direction = direction; }
    public boolean isMoving() { return moving; }
    public void setMoving(boolean moving) { this.moving = moving; }
    public int getLife() { return life; }
    public void setLife(int life) { this.life = life; }
} 