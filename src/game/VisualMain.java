package game;

import ui.WorldPanel;

import javax.swing.*;
import java.awt.*;

public class VisualMain {

    public static void start() {
        GameWorld world = new GameWorld(12345L);
        Player player = new Player();
        
        // этап 2: seed для визуала
        visual.VisualRegistry.setWorldSeed(world.getSeed());
        
        world.generateNode(0, player.getCurrentBranch());
        player.setCurrentNode(world.getCurrentNode());

        WorldPanel panel = new WorldPanel(world, player);
        JFrame frame = new JFrame("Мультивселенная — прототип");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(980, 820);
        frame.setLocationRelativeTo(null);
        frame.add(panel, BorderLayout.CENTER);
        frame.setVisible(true);
        panel.repaint();
    }
}