package org.b333vv.metric.ui.tree.node;

import org.b333vv.metric.ui.tree.TreeCellRenderer;

import javax.swing.tree.DefaultMutableTreeNode;

public abstract class AbstractNode extends DefaultMutableTreeNode {

  public abstract void render(TreeCellRenderer renderer);

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + "@" + hashCode();
  }
}
