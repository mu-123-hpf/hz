import java.awt.*;
import java.util.Random;

public class Skill {
    public enum SkillType {
        LASER(0.1),      // 激光技能
        TELEPORT(0.3),    // 瞬移技能
        ATTACK_SPEED(0.6); // 攻击速度提升

        private final double spawnProbability;
        SkillType(double probability) {
            this.spawnProbability = probability;
        }
    }

    private SkillType type;
    private int x, y;
    private static final int SIZE = 30;
    private static final Random random = new Random();
    private static final int MIN_SPAWN_TIME = 1000; // 调整为1秒
    private static final int MAX_SPAWN_TIME = 3000; // 调整为3秒

    public Skill(int x, int y) {
        this.x = x;
        this.y = y;
        this.type = getRandomSkillType();
    }

    private SkillType getRandomSkillType() {
        double rand = random.nextDouble();
        double cumulativeProbability = 0.0;
        
        for (SkillType skillType : SkillType.values()) {
            cumulativeProbability += skillType.spawnProbability;
            if (rand <= cumulativeProbability) {
                return skillType;
            }
        }
        return SkillType.ATTACK_SPEED; // 默认返回攻击速度提升
    }

    public void draw(Graphics g) {
        switch (type) {
            case LASER:
                g.setColor(Color.RED);
                break;
            case TELEPORT:
                g.setColor(Color.BLUE);
                break;
            case ATTACK_SPEED:
                g.setColor(Color.GREEN);
                break;
        }
        g.fillOval(x, y, SIZE, SIZE);
        g.setColor(Color.WHITE);
        g.drawOval(x, y, SIZE, SIZE);
    }

    public Rectangle getBounds() {
        return new Rectangle(x - 5, y - 5, SIZE + 10, SIZE + 10);
    }

    public SkillType getType() {
        return type;
    }

    public static int getRandomSpawnTime() {
        return MIN_SPAWN_TIME + random.nextInt(MAX_SPAWN_TIME - MIN_SPAWN_TIME);
    }
} 