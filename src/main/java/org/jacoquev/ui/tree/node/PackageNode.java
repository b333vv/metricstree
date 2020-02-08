package org.jacoquev.ui.tree.node;

import com.intellij.icons.AllIcons;
import org.jacoquev.model.code.JavaPackage;
import org.jacoquev.ui.tree.TreeCellRenderer;

import javax.swing.*;

public class PackageNode extends AbstractNode {

    private final transient JavaPackage javaPackage;

    public PackageNode(JavaPackage javaPackage) {
        this.javaPackage = javaPackage;
    }

    public JavaPackage getJavaPackage() {
        return javaPackage;
    }

    public Icon getIcon() {
        return AllIcons.Nodes.Package;
    }

    @Override
    public void render(TreeCellRenderer renderer) {
        renderer.setIcon(getIcon());
        renderer.append(javaPackage.getName());
    }
}
