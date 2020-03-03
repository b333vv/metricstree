package org.b333vv.metric.ui.tree.node;

import com.intellij.icons.AllIcons;
import org.b333vv.metric.model.code.JavaMethod;
import org.b333vv.metric.ui.tree.TreeCellRenderer;
import org.b333vv.metric.util.MetricsIcons;

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
