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

import com.intellij.icons.AllIcons;
import com.intellij.psi.util.PsiUtil;
import org.b333vv.metric.model.code.ClassElement;
import org.b333vv.metric.ui.tree.TreeCellRenderer;

import javax.swing.*;

public class ClassNode extends AbstractNode {

    protected final ClassElement javaClass;

    public ClassNode(ClassElement javaClass) {
        this.javaClass = javaClass;
    }

    public ClassElement getJavaClass() {
        return javaClass;
    }

    public Icon getIcon() {
        if (javaClass.getPsiClass().isInterface()) {
            return AllIcons.Nodes.Interface;
        }
        if (javaClass.getPsiClass().isEnum()) {
            return AllIcons.Nodes.Enum;
        }
        if (PsiUtil.isAbstractClass(javaClass.getPsiClass())) {
            return AllIcons.Nodes.AbstractClass;
        }
        return AllIcons.Nodes.Class;
    }

    @Override
    public void render(TreeCellRenderer renderer) {
        renderer.setIcon(getIcon());
        renderer.append(javaClass.getName());
    }
}
