package org.unibl.etf.mdp.common.ui;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.UIManager;
import java.awt.Color;
import java.util.Map;

/** The common look of the three desktop applications (FlatLaf, light, rounded, blue accent). */
public final class Theme {
    private Theme() {
    }

    /** Call once, before the first window is created. */
    public static void apply() {
        // The same knobs as a style sheet: accent colour and roundness
        FlatLightLaf.setGlobalExtraDefaults(Map.of("@accentColor", "#2f6fdf"));
        FlatLightLaf.setup();
        UIManager.put("Button.arc", 10);
        UIManager.put("Component.arc", 10);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.width", 12);
        UIManager.put("Table.rowHeight", 28);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.intercellSpacing", new java.awt.Dimension(0, 1));
        UIManager.put("TableHeader.height", 30);
        UIManager.put("TitlePane.unifiedBackground", true);
        UIManager.put("Panel.background", new Color(0xf6f7f9));
    }
}
