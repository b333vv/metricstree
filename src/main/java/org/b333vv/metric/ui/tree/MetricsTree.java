/*
 * Copyright 2020 b333vv
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.b333vv.metric.ui.tree;

import com.intellij.ui.treeStructure.Tree;
import org.b333vv.metric.ui.tree.node.AbstractNode;

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
