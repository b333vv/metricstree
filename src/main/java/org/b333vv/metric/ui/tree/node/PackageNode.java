package org.b333vv.metric.ui.tree.node;

import com.intellij.icons.AllIcons;
import org.b333vv.metric.model.code.JavaPackage;
import org.b333vv.metric.ui.tree.TreeCellRenderer;

public class PackageNode extends AbstractNode {

    private final transient JavaPackage javaPackage;

    public PackageNode(JavaPackage javaPackage) {
        this.javaPackage = javaPackage;
    }

    public JavaPackage getJavaPackage() {
        return javaPackage;
    }

    @Override
    public void render(TreeCellRenderer renderer) {
        renderer.setIcon(AllIcons.Nodes.Package);
        renderer.append(javaPackage.getName());
    }
}
