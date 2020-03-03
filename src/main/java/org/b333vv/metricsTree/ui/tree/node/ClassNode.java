package org.b333vv.metricsTree.ui.tree.node;

import com.intellij.icons.AllIcons;
import com.intellij.psi.util.PsiUtil;
import org.b333vv.metricsTree.model.code.JavaClass;
import org.b333vv.metricsTree.ui.tree.TreeCellRenderer;

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
