package ui;

import game.GameWorld;
import game.Node;
import game.Player;

import java.awt.*;

public class HudLayer {

    public static void draw(Graphics2D g2, Node node, Player player, int secondsToNextYear) {
        g2.setColor(Palette.TEXT);
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.drawString(node.getName()
                + "   ветка=" + player.getCurrentBranch()
                + "   t=" + node.getTimeAnchor(), 16, 24);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g2.setColor(Palette.PAST);
        String pe = GameWorld.DEBUG_INFINITE_ENERGY ? "∞" : (player.getPastEnergy() + "/20");
        g2.drawString("Прошлое " + pe, 16, 48);

        g2.setColor(Palette.FUTURE);
        String fe = GameWorld.DEBUG_INFINITE_ENERGY ? "∞" : (player.getFutureEnergy() + "/20");
        g2.drawString("Будущее " + fe, 16, 68);

        g2.setColor(Palette.TEXT_DIM);
        g2.drawString("год=" + player.getGameYear()
                + "  время=" + player.getGlobalTime(), 16, 88);

        if (GameWorld.DEBUG_INFINITE_ENERGY) {
            g2.setColor(new Color(120, 255, 160));
            g2.drawString("[ТЕСТ: бесконечная энергия]", 16, 108);
        }
       
    }
    
}