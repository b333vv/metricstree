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

import com.intellij.ui.SimpleTextAttributes;
import org.b333vv.metric.model.code.ProjectElement;
import org.b333vv.metric.ui.tree.TreeCellRenderer;

import javax.swing.*;

public class ProjectNode extends AbstractNode {

    private final ProjectElement projectElement;
    private final String description;
    private final Icon icon;

    public ProjectNode(ProjectElement projectElement, String description, Icon icon) {
        this.projectElement = projectElement;
        this.description = description;
        this.icon = icon;
    }

    public ProjectElement getprojectElement() {
        return projectElement;
    }

    @Override
    public void render(TreeCellRenderer renderer) {
//        renderer.setIcon(AllIcons.General.ProjectStructure);
        renderer.setIcon(icon);
        renderer.append(projectElement.getName());
        renderer.append(" (" + description + ")", SimpleTextAttributes.GRAY_SMALL_ATTRIBUTES);
    }
}
