package org.jacoquev.ui.tree.node;

import com.intellij.icons.AllIcons;
import org.jacoquev.model.code.JavaClass;
import org.jacoquev.ui.tree.TreeCellRenderer;

import javax.swing.*;

public class ClassNode extends AbstractNode {

    private final transient JavaClass javaClass;

    public ClassNode(JavaClass javaClass) {
        this.javaClass = javaClass;
    }

    public JavaClass getJavaClass() {
        return javaClass;
    }

    public Icon getIcon() {
        return AllIcons.Nodes.Class;
    }

    @Override
    public void render(TreeCellRenderer renderer) {
        renderer.setIcon(getIcon());
        renderer.append(javaClass.getName());
    }
}
