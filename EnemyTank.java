import java.awt.*;
import java.util.Random;

public class EnemyTank extends Tank {
    private static final Random random = new Random();
    private int moveTimer = 0;
    private int shootTimer = 0;
    private static final int MOVE_INTERVAL = 40;
    private static final int SHOOT_INTERVAL = 15;
    private Direction currentAIStrategyDirection = null;
    private int strategyChangeCooldown = 0;
    private static final int STRATEGY_COOLDOWN_MAX = 100;

    public EnemyTank(int x, int y, Direction direction) {
        super(x, y, direction);
        setLife(1);
    }

    @Override
    public void update() {
        moveTimer++;
        shootTimer++;
        
        if (GamePanel.playerTank != null) {
            int playerX = GamePanel.playerTank.getX();
            int playerY = GamePanel.playerTank.getY();

            if (strategyChangeCooldown <= 0 || (currentAIStrategyDirection == null || !canMove(currentAIStrategyDirection))) {
                strategyChangeCooldown = STRATEGY_COOLDOWN_MAX;

                int dx = playerX - x;
                int dy = playerY - y;

                Direction preferredDirection = null;
                if (Math.abs(dx) > Math.abs(dy)) {
                    if (dx > 0) preferredDirection = Direction.RIGHT;
                    else preferredDirection = Direction.LEFT;
                } else {
                    if (dy > 0) preferredDirection = Direction.DOWN;
                    else preferredDirection = Direction.UP;
                }

                if (canMove(preferredDirection)) {
                    currentAIStrategyDirection = preferredDirection;
                } else {
                    Direction[] directions = {Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};
                    Direction bestAlternative = null;
                    int bestDistance = Integer.MAX_VALUE;

                    for (Direction d : directions) {
                        if (canMove(d)) {
                            int tempX = x, tempY = y;
                            switch (d) {
                                case UP: tempY -= SPEED; break;
                                case DOWN: tempY += SPEED; break;
                                case LEFT: tempX -= SPEED; break;
                                case RIGHT: tempX += SPEED; break;
                            }
                            int newDx = playerX - tempX;
                            int newDy = playerY - tempY;
                            int newDistance = newDx * newDx + newDy * newDy;

                            if (newDistance < bestDistance) {
                                bestDistance = newDistance;
                                bestAlternative = d;
                            }
                        }
                    }

                    if (bestAlternative != null) {
                        currentAIStrategyDirection = bestAlternative;
                    } else {
                        Direction randomDir = directions[random.nextInt(directions.length)];
                        if (canMove(randomDir)) {
                            currentAIStrategyDirection = randomDir;
                        } else {
                            currentAIStrategyDirection = null;
                        }
                    }
                }
            }
            strategyChangeCooldown--;
            
            if (currentAIStrategyDirection != null) {
                setDirection(currentAIStrategyDirection);
            }
        }

        this.setMoving(true);
        
        if (shootTimer >= SHOOT_INTERVAL) {
            shootTimer = 0;
            if (GamePanel.playerTank != null) {
                boolean facingPlayer = false;
                int dx = GamePanel.playerTank.getX() - x;
                int dy = GamePanel.playerTank.getY() - y;

                if (direction == Direction.UP && dy < 0 && Math.abs(dx) < SIZE) facingPlayer = true;
                if (direction == Direction.DOWN && dy > 0 && Math.abs(dx) < SIZE) facingPlayer = true;
                if (direction == Direction.LEFT && dx < 0 && Math.abs(dy) < SIZE) facingPlayer = true;
                if (direction == Direction.RIGHT && dx > 0 && Math.abs(dy) < SIZE) facingPlayer = true;

                if (facingPlayer || Math.random() < 0.5) {
                    Bullet bullet = new Bullet(x + SIZE / 2, y + SIZE / 2, direction, false);
                    GamePanel.bullets.add(bullet);
                }
            } else if (Math.random() < 0.4) {
                Bullet bullet = new Bullet(x + SIZE / 2, y + SIZE / 2, direction, false);
                GamePanel.bullets.add(bullet);
            }
        }
        
        move();
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(Color.RED);
        g.fillRect(x, y, SIZE, SIZE);
        g.setColor(Color.ORANGE);
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
        if (life <= 0) {
            // 处理坦克被摧毁的逻辑
        }
    }
} 