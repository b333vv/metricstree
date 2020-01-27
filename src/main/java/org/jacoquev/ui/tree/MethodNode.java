package org.jacoquev.ui.tree;

import com.intellij.icons.AllIcons;
import org.jacoquev.model.code.JavaMethod;
import org.jacoquev.util.MetricsIcons;

import javax.swing.*;

public class MethodNode extends AbstractNode {

    private final JavaMethod javaMethod;

    public MethodNode(JavaMethod javaMethod) {
        this.javaMethod = javaMethod;
    }

    public JavaMethod getJavaMethod() {
        return javaMethod;
    }

    public Icon getIcon() {
        return javaMethod.getPsiMethod().isConstructor() ?
                MetricsIcons.CONSTRUCTOR :
                AllIcons.Nodes.Method;
    }

    @Override
    public void render(TreeCellRenderer renderer) {
        renderer.setIcon(getIcon());
        renderer.append(javaMethod.getName());
    }
}
