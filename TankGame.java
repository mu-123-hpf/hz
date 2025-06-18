import javax.swing.*;
import java.awt.*;

public class TankGame extends JFrame {
    public static void main(String[] args) {
        JFrame frame = new JFrame("坦克大战");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(new GamePanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
} 