package org.jacoquev.ui.tree;

import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;

public abstract class AbstractNode<T extends AbstractNode> extends DefaultMutableTreeNode {


  @NotNull
  protected static String spaceAndThinSpace() {
    String thinSpace = UIUtil.getLabelFont().canDisplay('\u2009') ? String.valueOf('\u2009') : " ";
    return " " + thinSpace;
  }

  public abstract void render(TreeCellRenderer renderer);

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + "@" + hashCode();
  }
}
