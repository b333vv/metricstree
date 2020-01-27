package org.jacoquev.ui.tree;

import com.intellij.ui.treeStructure.Tree;

import javax.annotation.CheckForNull;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class MetricsTree extends Tree {

  public MetricsTree(TreeModel model) {
    super(model);
    init();
  }

  private void init() {
    this.setShowsRootHandles(true);
    this.setCellRenderer(new TreeCellRenderer());
    this.expandRow(0);
  }

  @CheckForNull
  public AbstractNode getSelectedNode() {
    TreePath path = getSelectionPath();
    if (path == null) {
      return null;
    }
    return (AbstractNode) path.getLastPathComponent();
  }
}
