package ui;

import game.GameWorld;
import game.Player;
import game.Portal;

import javax.swing.*;

public class PortalDialog {

    public static void ask(JComponent parent, Portal p, GameWorld world, Player player,
                           Runnable onDone) {
        Object[] opts = { "В будущее", "В прошлое", "В параллель", "Отмена" };
        int choice = JOptionPane.showOptionDialog(parent,
                "Куда ведёт этот портал?",
                "Настройка портала",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, opts, opts[0]);

        if (choice == 3 || choice == -1) { p.refreshState(); onDone.run(); return; }

        if (choice == 0) {
            int dt = askNumber(parent, "На сколько лет в будущее? (1..20)", 1, 20, 1);
            if (dt <= 0) { p.refreshState(); onDone.run(); return; }
            p.kind = Portal.Kind.TIME_FUTURE;
            p.deltaTime = dt;
            p.setLabel("→ +" + dt);
        } else if (choice == 1) {
            int dt = askNumber(parent, "На сколько лет в прошлое? (1..20)", 1, 20, 1);
            if (dt <= 0) { p.refreshState(); onDone.run(); return; }
            p.kind = Portal.Kind.TIME_PAST;
            p.deltaTime = dt;
            p.setLabel("→ -" + dt);
        } else {
            int b = askNumber(parent, "Номер параллельной ветки (1..999)", 1, 999, 1);
            if (b <= 0) { p.refreshState(); onDone.run(); return; }
            p.kind = Portal.Kind.PARALLEL;
            p.targetBranch = b;
            p.setLabel("⇄ ветка " + b);
            world.ensureBranchExists(b, player.getCurrentBranch());
        }
        p.refreshState();
        onDone.run();
    }

    public static int askNumber(JComponent parent, String msg, int min, int max, int def) {
        String s = JOptionPane.showInputDialog(parent, msg, String.valueOf(def));
        if (s == null) return -1;
        try {
            int v = Integer.parseInt(s.trim());
            if (v < min || v > max) return -1;
            return v;
        } catch (Exception e) {
            return -1;
        }
    }
}