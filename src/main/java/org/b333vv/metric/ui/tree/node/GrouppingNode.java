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

package org.b333vv.metric.ui.tree.node;

import org.b333vv.metric.ui.tree.TreeCellRenderer;

import javax.swing.*;

public class GrouppingNode extends AbstractNode {

    private final String name;
    private final Icon icon;

    public GrouppingNode(String name, Icon icon) {
        this.name = name;
        this.icon = icon;
    }

    public Icon getIcon() {
        return icon;
    }

    @Override
    public void render(TreeCellRenderer renderer) {
        renderer.setIcon(getIcon());
        renderer.append(name);
    }
}
