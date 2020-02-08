package org.jacoquev.ui.tree;

import com.intellij.ui.ColoredTreeCellRenderer;
import org.jacoquev.ui.tree.node.AbstractNode;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.MouseEvent;

/**
 * Can't unit test this because the parent uses a service, depending on a pico container with a method
 * that doesn't exist in the pico container used by SonarLint (different versions), causing NoSuchMethodError.
 */
public class TreeCellRenderer extends ColoredTreeCellRenderer {
  private String iconToolTip = null;

  @Override
  public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected,
                                    boolean expanded, boolean leaf, int row, boolean hasFocus) {
    AbstractNode node = (AbstractNode) value;
    node.render(this);
  }

  public void setIconToolTip(String toolTip) {
    this.iconToolTip = toolTip;
  }

  @Override
  public String getToolTipText(MouseEvent event) {
    if (iconToolTip == null) {
      return super.getToolTipText(event);
    }

    if (event.getX() < getIconWidth()) {
      return iconToolTip;
    }

    return super.getToolTipText(event);
  }

  private int getIconWidth() {
    if (getIcon() != null) {
      return getIcon().getIconWidth() + myIconTextGap;
    }
    return 0;
  }
}
