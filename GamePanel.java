import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.List;
import java.util.Iterator;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class GamePanel extends JPanel implements ActionListener, KeyListener, MouseListener, MouseMotionListener {
    private enum GameState {
        START_MENU,
        PLAYING,
        LEVEL_TRANSITION,
        GAME_OVER,
        GAME_WIN
    }

    private GameState currentState = GameState.START_MENU;
    private int levelTransitionCountdown = 3;
    private JButton startButton;
    private JButton exitButton;
    private JButton restartButton;
    private JButton menuExitButton;
    public static final int PANEL_WIDTH = 1200;
    public static final int PANEL_HEIGHT = 1000;
    private final int GRID_SIZE = 40;
    private javax.swing.Timer timer;
    private javax.swing.Timer levelTransitionTimer;
    private javax.swing.Timer skillSpawnTimer;
    public static PlayerTank playerTank;
    public static java.util.List<EnemyTank> enemyTanks;
    public static List<Bullet> bullets = new ArrayList<>();
    public static java.util.List<Rectangle> walls = new ArrayList<>();
    private Skill currentSkill;
    private int level = 1;
    private final int MAX_LEVEL = 3;
    private boolean attackSpeedBoost = false;
    private long attackSpeedEndTime = 0;
    private boolean laserReady = false;
    private boolean teleportReady = false;
    private int teleportRemaining = 0;
    private long lastShootTime = 0;
    private static final int NORMAL_SHOOT_COOLDOWN = 400; // ms
    private static final int FAST_SHOOT_COOLDOWN = 100; // ms
    private Image backgroundImage;

    public GamePanel() {
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        walls = new ArrayList<>();
        enemyTanks = new ArrayList<>();
        playerTank = new PlayerTank(PANEL_WIDTH / 2, PANEL_HEIGHT - 100, Direction.UP);
        initializeButtons();
        setupTimers();

        // 加载背景图片
        try {
            backgroundImage = ImageIO.read(new File("background.png"));
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading background image: " + e.getMessage());
        }
    }

    private void initializeButtons() {
        startButton = new JButton("开始游戏");
        exitButton = new JButton("退出游戏");
        restartButton = new JButton("重新开始");
        menuExitButton = new JButton("退出游戏");

        Font buttonFont = new Font("微软雅黑", Font.BOLD, 20);
        Dimension buttonSize = new Dimension(200, 50);
        for (JButton button : new JButton[]{startButton, exitButton, restartButton, menuExitButton}) {
            button.setFont(buttonFont);
            button.setPreferredSize(buttonSize);
            button.setFocusPainted(false);
            button.addActionListener(this);
        }

        startButton.addActionListener(e -> {
            currentState = GameState.PLAYING;
            startLevel(1);
            timer.start();
            skillSpawnTimer.start();
            removeAll();
            repaint();
        });
        exitButton.addActionListener(e -> System.exit(0));
        menuExitButton.addActionListener(e -> System.exit(0));
        restartButton.addActionListener(e -> {
            currentState = GameState.PLAYING;
            startLevel(1);
            timer.start();
            removeAll();
            repaint();
        });
    }

    private void setupTimers() {
        timer = new javax.swing.Timer(20, this);
        levelTransitionTimer = new javax.swing.Timer(1000, e -> {
            levelTransitionCountdown--;
            if (levelTransitionCountdown <= 0) {
                levelTransitionTimer.stop();
                currentState = GameState.PLAYING;
                startLevel(level + 1);
                timer.start();
            }
            repaint();
        });
        skillSpawnTimer = new javax.swing.Timer(Skill.getRandomSpawnTime(), e -> {
            if (currentState == GameState.PLAYING) {
                spawnSkill();
            }
        });
    }

    private void spawnSkill() {
        Random random = new Random();
        int x = random.nextInt(PANEL_WIDTH - 100) + 50;
        int y = random.nextInt(PANEL_HEIGHT - 100) + 50;
        Rectangle skillRect = new Rectangle(x, y, 30, 30);
        boolean validPosition = true;
        System.out.println("Attempting to spawn skill at (" + x + ", " + y + ")");
        for (Rectangle wall : walls) {
            if (skillRect.intersects(wall)) {
                validPosition = false;
                System.out.println("Skill position blocked by wall: " + wall);
                break;
            }
        }
        if (validPosition) {
            currentSkill = new Skill(x, y);
            skillSpawnTimer.setDelay(Skill.getRandomSpawnTime());
            System.out.println("Skill spawned: " + currentSkill.getType() + " at (" + x + ", " + y + ")");
        } else {
            System.out.println("Failed to spawn skill: no valid position.");
        }
    }

    private void startLevel(int level) {
        this.level = level;
        enemyTanks.clear();
        bullets.clear();
        playerTank.setX(PANEL_WIDTH / 2);
        playerTank.setY(PANEL_HEIGHT - 100);
        playerTank.setDirection(Direction.UP);
        playerTank.setMoving(false);
        playerTank.setLife(3);
        createMazeForLevel(level);
        int enemyCount;
        switch (level) {
            case 1: enemyCount = 8; break;
            case 2: enemyCount = 16; break;
            case 3: enemyCount = 36; break;
            default: enemyCount = 8;
        }
        for (int i = 0; i < enemyCount; i++) {
            int[] pos = getRandomPosition();
            final int x = pos[0];
            final int y = pos[1];
            if (isAreaFree(x, y, 40)) {
                enemyTanks.add(new EnemyTank(x, y, Direction.LEFT));
            }
        }
        currentState = GameState.PLAYING;
        skillSpawnTimer.start();
    }

    private int[] getRandomPosition() {
        Random random = new Random();
        int x = random.nextInt(PANEL_WIDTH - 100) + 50;
        int y = random.nextInt(PANEL_HEIGHT - 100) + 50;
        return new int[]{x, y};
    }

    private void createMazeForLevel(int lvl) {
        walls.clear();
        for (int i = 0; i < PANEL_WIDTH; i += GRID_SIZE) {
            walls.add(new Rectangle(i, 0, GRID_SIZE, GRID_SIZE));
            walls.add(new Rectangle(i, PANEL_HEIGHT - GRID_SIZE, GRID_SIZE, GRID_SIZE));
        }
        for (int i = GRID_SIZE; i < PANEL_HEIGHT - GRID_SIZE; i += GRID_SIZE) {
            walls.add(new Rectangle(0, i, GRID_SIZE, GRID_SIZE));
            walls.add(new Rectangle(PANEL_WIDTH - GRID_SIZE, i, GRID_SIZE, GRID_SIZE));
        }
        if (lvl == 1) {
            // 更稀疏的墙壁布局，方便技能生成
            // 移除大部分内墙，只保留边界和少量中央障碍
            // 确保有足够的开放空间
            walls.add(new Rectangle(PANEL_WIDTH / 3, PANEL_HEIGHT / 3, GRID_SIZE, GRID_SIZE));
            walls.add(new Rectangle(PANEL_WIDTH * 2 / 3, PANEL_HEIGHT / 3, GRID_SIZE, GRID_SIZE));
            walls.add(new Rectangle(PANEL_WIDTH / 3, PANEL_HEIGHT * 2 / 3, GRID_SIZE, GRID_SIZE));
            walls.add(new Rectangle(PANEL_WIDTH * 2 / 3, PANEL_HEIGHT * 2 / 3, GRID_SIZE, GRID_SIZE));
        } else if (lvl == 2) {
            // 生成不规则但确保连通性的地图
            Random rand = new Random();
            for (int i = 0; i < 40; i++) { // 尝试生成40个随机墙块
                int wallX = rand.nextInt(PANEL_WIDTH / GRID_SIZE - 2) * GRID_SIZE + GRID_SIZE;
                int wallY = rand.nextInt(PANEL_HEIGHT / GRID_SIZE - 2) * GRID_SIZE + GRID_SIZE;
                walls.add(new Rectangle(wallX, wallY, GRID_SIZE, GRID_SIZE));
            }

            // 确保玩家和敌方出生点附近的区域是清晰的
            walls.removeIf(w -> w.intersects(playerTank.getBounds())); // 玩家出生点
            
            // 清除顶部的敌人出生区域
            walls.removeIf(w -> w.intersects(new Rectangle(GRID_SIZE, GRID_SIZE, PANEL_WIDTH - 2 * GRID_SIZE, GRID_SIZE * 3)));
            // 清除底部的敌人出生区域
            walls.removeIf(w -> w.intersects(new Rectangle(GRID_SIZE, PANEL_HEIGHT - GRID_SIZE * 4, PANEL_WIDTH - 2 * GRID_SIZE, GRID_SIZE * 3)));
            // 清除左侧的敌人出生区域
            walls.removeIf(w -> w.intersects(new Rectangle(GRID_SIZE, GRID_SIZE, GRID_SIZE * 3, PANEL_HEIGHT - 2 * GRID_SIZE)));
            // 清除右侧的敌人出生区域
            walls.removeIf(w -> w.intersects(new Rectangle(PANEL_WIDTH - GRID_SIZE * 4, GRID_SIZE, GRID_SIZE * 3, PANEL_HEIGHT - 2 * GRID_SIZE)));

            // 在地图中心开辟一些通道，确保整体连通性
            walls.removeIf(w -> w.intersects(new Rectangle(PANEL_WIDTH / 3, PANEL_HEIGHT / 3, GRID_SIZE * 2, GRID_SIZE * 2)));
            walls.removeIf(w -> w.intersects(new Rectangle(PANEL_WIDTH * 2 / 3 - GRID_SIZE * 2, PANEL_HEIGHT * 2 / 3 - GRID_SIZE * 2, GRID_SIZE * 2, GRID_SIZE * 2)));
            walls.removeIf(w -> w.intersects(new Rectangle(PANEL_WIDTH / 2 - GRID_SIZE, PANEL_HEIGHT / 2 - GRID_SIZE, GRID_SIZE * 2, GRID_SIZE * 2))); // 中心
        } else if (lvl == 3) {
            // 生成更复杂但不困坦克的随机地图
            Random rand = new Random();
            int wallCount = 80; // 增加墙壁数量以提高复杂度
            for (int i = 0; i < wallCount; i++) {
                int wallX = rand.nextInt(PANEL_WIDTH / GRID_SIZE - 2) * GRID_SIZE + GRID_SIZE;
                int wallY = rand.nextInt(PANEL_HEIGHT / GRID_SIZE - 2) * GRID_SIZE + GRID_SIZE;
                walls.add(new Rectangle(wallX, wallY, GRID_SIZE, GRID_SIZE));
            }

            // 确保玩家和敌方出生点附近的区域是清晰的
            walls.removeIf(w -> w.intersects(playerTank.getBounds())); // 玩家出生点
            
            // 清除顶部的敌人出生区域
            walls.removeIf(w -> w.intersects(new Rectangle(GRID_SIZE, GRID_SIZE, PANEL_WIDTH - 2 * GRID_SIZE, GRID_SIZE * 4)));
            // 清除底部的敌人出生区域
            walls.removeIf(w -> w.intersects(new Rectangle(GRID_SIZE, PANEL_HEIGHT - GRID_SIZE * 5, PANEL_WIDTH - 2 * GRID_SIZE, GRID_SIZE * 4)));
            // 清除左侧的敌人出生区域
            walls.removeIf(w -> w.intersects(new Rectangle(GRID_SIZE, GRID_SIZE, GRID_SIZE * 4, PANEL_HEIGHT - 2 * GRID_SIZE)));
            // 清除右侧的敌人出生区域
            walls.removeIf(w -> w.intersects(new Rectangle(PANEL_WIDTH - GRID_SIZE * 5, GRID_SIZE, GRID_SIZE * 4, PANEL_HEIGHT - 2 * GRID_SIZE)));

            // 在地图中心开辟一些更大的通道，确保整体连通性
            walls.removeIf(w -> w.intersects(new Rectangle(PANEL_WIDTH / 4, PANEL_HEIGHT / 4, PANEL_WIDTH / 2, PANEL_HEIGHT / 2)));
        }
        final Rectangle fixedRemoveRect = new Rectangle(100, 100, 40, 40);
        walls.removeIf(w -> w.intersects(fixedRemoveRect));
    }

    private boolean isAreaFree(int x, int y, int size) {
        Rectangle rect = new Rectangle(x, y, size, size);
        for (Rectangle wall : walls) {
            if (rect.intersects(wall)) return false;
        }
        return true;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        switch (currentState) {
            case START_MENU:
                drawStartMenu(g);
                break;
            case PLAYING:
                drawGame(g);
                break;
            case LEVEL_TRANSITION:
                drawLevelTransition(g);
                break;
            case GAME_OVER:
                drawGameOver(g);
                break;
            case GAME_WIN:
                drawGameWin(g);
                break;
        }
    }

    private void drawStartMenu(Graphics g) {
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, PANEL_WIDTH, PANEL_HEIGHT, this);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("微软雅黑", Font.BOLD, 60));
        String title = "坦克大战";
        FontMetrics fm = g.getFontMetrics();
        int titleX = (PANEL_WIDTH - fm.stringWidth(title)) / 2;
        g.drawString(title, titleX, 120);

        g.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        g.setColor(Color.CYAN);
        int textY = 200;
        int lineSpacing = 25;

        String[] instructions = {
            "游戏介绍：",
            "- 移动我方坦克：使用 'W', 'A', 'S', 'D' 键 或 上下左右箭头键。",
            "- 攻击键： 空格键。",
            "- 技能键： 'V' 键。",
            "- 取得胜利：消灭当前关卡所有敌方坦克进入下一关，共三关。",
            "- 技能作用：",
            "    - 激光： (Laser) 攻击穿透所有墙壁和敌人，造成大量伤害。",
            "    - 传送： (Teleport) 短距离瞬间移动，可穿越障碍物。",
            "    - 攻击速度： (Attack Speed) 短时间内大幅提升射击频率。",
            "",
            "祝您游戏愉快！"
        };

        for (String line : instructions) {
            int textX = (PANEL_WIDTH - g.getFontMetrics().stringWidth(line)) / 2; // 中心对齐
            g.drawString(line, textX, textY);
            textY += lineSpacing;
        }

        setLayout(null);
        startButton.setBounds((PANEL_WIDTH - 200) / 2, PANEL_HEIGHT - 150, 200, 50);
        exitButton.setBounds((PANEL_WIDTH - 200) / 2, PANEL_HEIGHT - 80, 200, 50);
        add(startButton);
        add(exitButton);
    }

    private void drawGame(Graphics g) {
        drawGrid(g);
        for (Rectangle wall : walls) {
            g.setColor(Color.GRAY);
            g.fillRect(wall.x, wall.y, wall.width, wall.height);
        }
        playerTank.draw(g);
        for (EnemyTank et : enemyTanks) {
            et.draw(g);
        }
        for (Bullet bullet : bullets) {
            bullet.draw(g);
        }
        if (currentSkill != null) {
            currentSkill.draw(g);
        }
        g.setColor(Color.WHITE);
        g.drawString("Player Life: " + playerTank.getLife(), 10, 20);
        g.drawString("Level: " + level, 10, 40);
    }

    private void drawLevelTransition(Graphics g) {
        drawGame(g);
        g.setColor(new Color(0, 0, 0, 128));
        g.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        g.setColor(Color.WHITE);
        g.setFont(new Font("微软雅黑", Font.BOLD, 40));
        String text = "下一关: " + levelTransitionCountdown;
        FontMetrics fm = g.getFontMetrics();
        int textX = (PANEL_WIDTH - fm.stringWidth(text)) / 2;
        g.drawString(text, textX, PANEL_HEIGHT / 2);
    }

    private void drawGameOver(Graphics g) {
        g.setColor(new Color(0, 0, 0, 128));
        g.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        g.setColor(Color.WHITE);
        g.setFont(new Font("微软雅黑", Font.BOLD, 40));
        String text = "游戏结束";
        FontMetrics fm = g.getFontMetrics();
        int textX = (PANEL_WIDTH - fm.stringWidth(text)) / 2;
        g.drawString(text, textX, PANEL_HEIGHT / 2 - 50);
        setLayout(null);
        restartButton.setBounds((PANEL_WIDTH - 200) / 2, PANEL_HEIGHT / 2, 200, 50);
        menuExitButton.setBounds((PANEL_WIDTH - 200) / 2, PANEL_HEIGHT / 2 + 70, 200, 50);
        add(restartButton);
        add(menuExitButton);
    }

    private void drawGameWin(Graphics g) {
        g.setColor(new Color(0, 0, 0, 128));
        g.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        g.setColor(Color.WHITE);
        g.setFont(new Font("微软雅黑", Font.BOLD, 40));
        String text = "恭喜通关！";
        FontMetrics fm = g.getFontMetrics();
        int textX = (PANEL_WIDTH - fm.stringWidth(text)) / 2;
        g.drawString(text, textX, PANEL_HEIGHT / 2 - 50);
        setLayout(null);
        restartButton.setBounds((PANEL_WIDTH - 200) / 2, PANEL_HEIGHT / 2, 200, 50);
        menuExitButton.setBounds((PANEL_WIDTH - 200) / 2, PANEL_HEIGHT / 2 + 70, 200, 50);
        add(restartButton);
        add(menuExitButton);
    }

    private void drawGrid(Graphics g) {
        g.setColor(new Color(50, 50, 50));
        for (int i = 0; i < PANEL_WIDTH; i += GRID_SIZE) {
            g.drawLine(i, 0, i, PANEL_HEIGHT);
        }
        for (int i = 0; i < PANEL_HEIGHT; i += GRID_SIZE) {
            g.drawLine(0, i, PANEL_WIDTH, i);
        }
    }

    private void checkCollisions() {
        // Player bullet collisions with enemy tanks and walls
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            if (!bullet.isActive()) {
                bullets.remove(i);
                continue;
            }

            bullet.update(); // Move the bullet

            // Check collision with walls
            boolean hitWall = false;
            if (bullet.isLaserBullet()) {
                // Laser bullets penetrate walls, so no hitWall logic here for walls
            } else { // Regular bullet
                for (Rectangle wall : walls) {
                    if (bullet.getBounds().intersects(wall)) {
                        hitWall = true;
                        bullet.setActive(false);
                        break;
                    }
                }
            }

            if (hitWall && !bullet.isLaserBullet()) {
                bullets.remove(i);
                continue;
            }

            // Check collision with tanks
            if (bullet.isFromPlayer()) {
                for (int j = enemyTanks.size() - 1; j >= 0; j--) {
                    EnemyTank enemyTank = enemyTanks.get(j);
                    if (bullet.getBounds().intersects(enemyTank.getBounds())) {
                        enemyTank.takeDamage();
                        bullet.setActive(false);
                        if (enemyTank.getLife() <= 0) {
                            enemyTanks.remove(j);
                        }
                        break; // Bullet hits one enemy, remove it
                    }
                }
            } else { // Enemy bullet
                if (bullet.getBounds().intersects(playerTank.getBounds())) {
                    playerTank.takeDamage();
                    bullet.setActive(false);
                    if (playerTank.getLife() <= 0) {
                        currentState = GameState.GAME_OVER;
                        timer.stop();
                    }
                }
                // Enemy bullets do not damage other enemy tanks
            }

            // Remove bullet if out of bounds
            if (bullet.getX() < 0 || bullet.getX() > PANEL_WIDTH ||
                bullet.getY() < 0 || bullet.getY() > PANEL_HEIGHT) {
                bullet.setActive(false);
            }

            if (!bullet.isActive()) {
                bullets.remove(i);
            }
        }

        // Tank to tank collisions
        // Player tank vs enemy tanks
        for (EnemyTank enemyTank : enemyTanks) {
            if (playerTank.getBounds().intersects(enemyTank.getBounds())) {
                // Simple separation: push tanks apart based on direction of overlap
                Rectangle intersection = playerTank.getBounds().intersection(enemyTank.getBounds());
                if (intersection.width < intersection.height) { // Horizontal overlap is smaller
                    if (playerTank.getX() < enemyTank.getX()) {
                        playerTank.setX(playerTank.getX() - intersection.width / 2);
                        enemyTank.setX(enemyTank.getX() + intersection.width / 2);
                    } else {
                        playerTank.setX(playerTank.getX() + intersection.width / 2);
                        enemyTank.setX(enemyTank.getX() - intersection.width / 2);
                    }
                } else { // Vertical overlap is smaller
                    if (playerTank.getY() < enemyTank.getY()) {
                        playerTank.setY(playerTank.getY() - intersection.height / 2);
                        enemyTank.setY(enemyTank.getY() + intersection.height / 2);
                    } else {
                        playerTank.setY(playerTank.getY() + intersection.height / 2);
                        enemyTank.setY(enemyTank.getY() - intersection.height / 2);
                    }
                }
            }
        }

        // Enemy tank vs enemy tank collisions
        for (int i = 0; i < enemyTanks.size(); i++) {
            for (int j = i + 1; j < enemyTanks.size(); j++) {
                EnemyTank tank1 = enemyTanks.get(i);
                EnemyTank tank2 = enemyTanks.get(j);
                if (tank1.getBounds().intersects(tank2.getBounds())) {
                    Rectangle intersection = tank1.getBounds().intersection(tank2.getBounds());
                    if (intersection.width < intersection.height) {
                        if (tank1.getX() < tank2.getX()) {
                            tank1.setX(tank1.getX() - intersection.width / 2);
                            tank2.setX(tank2.getX() + intersection.width / 2);
                        } else {
                            tank1.setX(tank1.getX() + intersection.width / 2);
                            tank2.setX(tank2.getX() - intersection.width / 2);
                        }
                    } else {
                        if (tank1.getY() < tank2.getY()) {
                            tank1.setY(tank1.getY() - intersection.height / 2);
                            tank2.setY(tank2.getY() + intersection.height / 2);
                        } else {
                            tank1.setY(tank1.getY() + intersection.height / 2);
                            tank2.setY(tank2.getY() - intersection.height / 2);
                        }
                    }
                }
            }
        }
    }

    private void updateTanks() {
        playerTank.update();
        for (EnemyTank enemyTank : enemyTanks) {
            enemyTank.update();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == timer) {
            if (currentState == GameState.PLAYING) {
                long currentTime = System.currentTimeMillis();

                // Skill pickup detection
                if (currentSkill != null) {
                    System.out.println("Checking skill pickup. Player bounds: " + playerTank.getBounds() + ", Skill bounds: " + currentSkill.getBounds());
                }
                if (currentSkill != null && playerTank.getBounds().intersects(currentSkill.getBounds())) {
                    Skill.SkillType type = currentSkill.getType();
                    System.out.println("Skill picked up: " + type);
                    switch (type) {
                        case LASER:
                            laserReady = true;
                            teleportReady = false;
                            teleportRemaining = 0;
                            break;
                        case TELEPORT:
                            teleportReady = true;
                            teleportRemaining = 3;
                            laserReady = false;
                            break;
                        case ATTACK_SPEED:
                            attackSpeedBoost = true;
                            attackSpeedEndTime = System.currentTimeMillis() + 5000; // 5 seconds
                            break;
                    }
                    currentSkill = null;
                }

                // Attack speed skill cooldown
                if (attackSpeedBoost && currentTime > attackSpeedEndTime) {
                    attackSpeedBoost = false;
                }

                updateTanks(); // Update tank positions (including movement limits and obstacle collision via Tank.move())
                checkCollisions(); // Handle bullet movement, tank-bullet and bullet-wall collisions

                if (enemyTanks.isEmpty()) {
                    if (level < MAX_LEVEL) {
                        currentState = GameState.LEVEL_TRANSITION;
                        levelTransitionCountdown = 3;
                        levelTransitionTimer.start();
                        timer.stop();
                    } else {
                        currentState = GameState.GAME_WIN;
                        timer.stop();
                    }
                }
                repaint();
            }
        } else if (e.getSource() == levelTransitionTimer) {
            levelTransitionCountdown--;
            if (levelTransitionCountdown <= 0) {
                levelTransitionTimer.stop();
                currentState = GameState.PLAYING;
                startLevel(level + 1);
                timer.start();
            }
            repaint();
        } else if (e.getSource() == skillSpawnTimer) {
            if (currentState == GameState.PLAYING) {
                spawnSkill();
            }
        }
    }

    private void teleportPlayer() {
        if (teleportRemaining <= 0) return;

        int targetX = playerTank.getX();
        int targetY = playerTank.getY();
        int teleportDistance = Tank.SIZE * 5; // Teleport by 5 tank sizes

        switch (playerTank.getDirection()) {
            case UP: targetY -= teleportDistance; break;
            case DOWN: targetY += teleportDistance; break;
            case LEFT: targetX -= teleportDistance; break;
            case RIGHT: targetX += teleportDistance; break;
        }

        // Ensure target position is within bounds
        targetX = Math.max(0, Math.min(targetX, PANEL_WIDTH - Tank.SIZE));
        targetY = Math.max(0, Math.min(targetY, PANEL_HEIGHT - Tank.SIZE));

        if (isAreaFree(targetX, targetY, Tank.SIZE)) {
            playerTank.setX(targetX);
            playerTank.setY(targetY);
            teleportRemaining--;
        } else {
            // If direct teleport is blocked, try to find a nearby valid spot
            // This is a simplified approach, a more robust solution would involve pathfinding
            for (int i = 0; i < 5; i++) { // Try up to 5 nearby spots
                int offsetX = (int)(Math.random() * 2 - 1) * Tank.SIZE; // -1, 0, or 1 * SIZE
                int offsetY = (int)(Math.random() * 2 - 1) * Tank.SIZE; // -1, 0, or 1 * SIZE
                int newX = playerTank.getX() + offsetX;
                int newY = playerTank.getY() + offsetY;

                newX = Math.max(0, Math.min(newX, PANEL_WIDTH - Tank.SIZE));
                newY = Math.max(0, Math.min(newY, PANEL_HEIGHT - Tank.SIZE));

                if (isAreaFree(newX, newY, Tank.SIZE)) {
                    playerTank.setX(newX);
                    playerTank.setY(newY);
                    teleportRemaining--;
                    break;
                }
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (currentState == GameState.PLAYING) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_W:
                case KeyEvent.VK_UP:
                    playerTank.setDirection(Direction.UP);
                    playerTank.setMoving(true);
                    break;
                case KeyEvent.VK_S:
                case KeyEvent.VK_DOWN:
                    playerTank.setDirection(Direction.DOWN);
                    playerTank.setMoving(true);
                    break;
                case KeyEvent.VK_A:
                case KeyEvent.VK_LEFT:
                    playerTank.setDirection(Direction.LEFT);
                    playerTank.setMoving(true);
                    break;
                case KeyEvent.VK_D:
                case KeyEvent.VK_RIGHT:
                    playerTank.setDirection(Direction.RIGHT);
                    playerTank.setMoving(true);
                    break;
                case KeyEvent.VK_SPACE:
                    long now = System.currentTimeMillis();
                    int cooldown = attackSpeedBoost ? FAST_SHOOT_COOLDOWN : NORMAL_SHOOT_COOLDOWN;
                    if (now - lastShootTime >= cooldown) {
                        Bullet bullet = playerTank.shoot();
                        if (bullet != null) {
                            bullets.add(bullet);
                            lastShootTime = now;
                        }
                    }
                    break;
                case KeyEvent.VK_V:
                    if (laserReady) {
                        Bullet laser = new Bullet(playerTank.getX() + 18, playerTank.getY() + 18, playerTank.getDirection(), true) {
                            @Override
                            public void update() {
                                // 激光直线穿透
                                switch (direction) {
                                    case UP: setY(0); break;
                                    case DOWN: setY(PANEL_HEIGHT); break;
                                    case LEFT: setX(0); break;
                                    case RIGHT: setX(PANEL_WIDTH); break;
                                }
                            }
                            @Override
                            public void draw(Graphics g) {
                                g.setColor(Color.CYAN);
                                switch (direction) {
                                    case UP:
                                        g.fillRect(getX(), 0, 4, getY() + 20);
                                        break;
                                    case DOWN:
                                        g.fillRect(getX(), getY(), 4, PANEL_HEIGHT - getY());
                                        break;
                                    case LEFT:
                                        g.fillRect(0, getY(), getX() + 20, 4);
                                        break;
                                    case RIGHT:
                                        g.fillRect(getX(), getY(), PANEL_WIDTH - getX(), 4);
                                        break;
                                }
                            }
                        };
                        bullets.add(laser);
                        laserReady = false;
                    } else if (teleportReady) {
                        teleportPlayer();
                    }
                    break;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (currentState == GameState.PLAYING) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_W:
                case KeyEvent.VK_UP:
                case KeyEvent.VK_S:
                case KeyEvent.VK_DOWN:
                case KeyEvent.VK_A:
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_D:
                case KeyEvent.VK_RIGHT:
                    playerTank.setMoving(false);
                    break;
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public void mouseClicked(MouseEvent e) {}
    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void mouseDragged(MouseEvent e) {}
    @Override
    public void mouseMoved(MouseEvent e) {}
} 