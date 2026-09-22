package game;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        // Всё, что делает main — запускает визуальный прототип.
        SwingUtilities.invokeLater(VisualMain::start);
    }
}