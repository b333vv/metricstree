package org.b333vv.metric.ui.tree;

import javax.swing.*;
import java.awt.*;

public class CompositeIcon implements Icon {
    public static final float TOP = 0.0f;
    public static final float LEFT = 0.0f;
    public static final float CENTER = 0.5f;
    public static final float BOTTOM = 1.0f;
    public static final float RIGHT = 1.0f;
    private final Axis axis;
    private final int gap;
    private Icon[] icons;
    private float alignmentX = CENTER;
    private float alignmentY = CENTER;

    public CompositeIcon(Icon... icons) {
        this(Axis.X_AXIS, icons);
    }

    public CompositeIcon(Axis axis, Icon... icons) {
        this(axis, 0, icons);
    }

    public CompositeIcon(Axis axis, int gap, Icon... icons) {
        this(axis, gap, CENTER, CENTER, icons);
    }

    public CompositeIcon(Axis axis, int gap, float alignmentX, float alignmentY, Icon... icons) {
        this.axis = axis;
        this.gap = gap;

        if (alignmentX > 1.0f) {
            this.alignmentX = 1.0f;
        } else {
            this.alignmentX = alignmentX < 0.0f ? 0.0f : alignmentX;
        }
        if (alignmentY > 1.0f) {
            this.alignmentY = 1.0f;
        } else {
            this.alignmentY = alignmentY < 0.0f ? 0.0f : alignmentY;
        }
        for (int i = 0; i < icons.length; i++) {
            if (icons[i] == null) {
                String message = "Icon (" + i + ") cannot be null";
                throw new IllegalArgumentException(message);
            }
        }

        this.icons = icons;
    }

    private static int getOffset(int maxValue, int iconValue, float alignment) {
        float offset = (maxValue - iconValue) * alignment;
        return Math.round(offset);
    }

    public Axis getAxis() {
        return axis;
    }

    public int getGap() {
        return gap;
    }

    public float getAlignmentX() {
        return alignmentX;
    }

    public float getAlignmentY() {
        return alignmentY;
    }

    public int getIconCount() {
        return icons.length;
    }

    public Icon getIcon(int index) {
        return icons[index];
    }

    @Override
    public int getIconWidth() {
        int width = 0;

        if (axis == Axis.X_AXIS) {
            width += (icons.length - 1) * gap;

            for (Icon icon : icons) {
                width += icon.getIconWidth();
            }
        } else {
            for (Icon icon : icons) {
                width = Math.max(width, icon.getIconWidth());
            }
        }

        return width;
    }

    @Override
    public int getIconHeight() {
        int height = 0;

        if (axis == Axis.Y_AXIS) {
            height += (icons.length - 1) * gap;

            for (Icon icon : icons) {
                height += icon.getIconHeight();
            }
        } else {
            for (Icon icon : icons) {
                height = Math.max(height, icon.getIconHeight());
            }
        }

        return height;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int paramX, int paramY) {
        int x = paramX;
        int y = paramY;
        if (axis == Axis.X_AXIS) {
            int height = getIconHeight();

            for (Icon icon : icons) {
                int iconY = getOffset(height, icon.getIconHeight(), alignmentY);
                icon.paintIcon(c, g, x, y + iconY);
                x += icon.getIconWidth() + gap;
            }
        } else if (axis == Axis.Y_AXIS) {
            int width = getIconWidth();

            for (Icon icon : icons) {
                int iconX = getOffset(width, icon.getIconWidth(), alignmentX);
                icon.paintIcon(c, g, x + iconX, y);
                y += icon.getIconHeight() + gap;
            }
        } else {
            int width = getIconWidth();
            int height = getIconHeight();

            for (Icon icon : icons) {
                int iconX = getOffset(width, icon.getIconWidth(), alignmentX);
                int iconY = getOffset(height, icon.getIconHeight(), alignmentY);
                icon.paintIcon(c, g, x + iconX, y + iconY);
            }
        }
    }

    public enum Axis {
        X_AXIS,
        Y_AXIS,
        Z_AXIS
    }
}
