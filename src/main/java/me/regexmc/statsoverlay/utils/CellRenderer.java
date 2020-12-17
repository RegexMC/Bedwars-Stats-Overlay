package me.regexmc.statsoverlay.utils;

import me.regexmc.statsoverlay.Main;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class CellRenderer extends DefaultTableCellRenderer {

    private static final long serialVersionUID = 1L;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        int level = Integer.parseInt(table.getValueAt(row, 1).toString());

        if(level==0) {
            this.setBackground(new Color(255, 10, 50));
            this.setForeground(new Color(255, 255, 255));
            return this;
        }

        double FKD = Double.parseDouble(table.getValueAt(row, 10).toString());
        double index = level * Math.pow(FKD, 2);

        this.setValue(table.getValueAt(row, column));
        this.setForeground(getColor(index));
        if (getColor(index).equals(Color.decode("#FFFFFF"))) { //if text is white, set background color to gray
            this.setBackground(Color.decode("#4d4d4d"));
        } else {
            this.setBackground(Main.defaultCellColor);
        }
        return this;
    }

    private Color getColor(double index) {
        if (index < 500) return Color.decode("#AAAAAA");
        if (index < 1000) return Color.decode("#FFFFFF");
        if (index < 3000) return Color.decode("#FFFF55");
        if (index < 7500) return Color.decode("#FFAA00");
        if (index < 15000) return Color.decode("#FF5555");
        if (index < 30000) return Color.decode("#AA00AA");
        if (index < 100000) return Color.decode("#5555FF");
        if (index < 500000) return Color.decode("#55FFFF");
        return Color.decode("#AA0000");
    }
}