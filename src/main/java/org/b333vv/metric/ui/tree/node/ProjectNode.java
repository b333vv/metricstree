package org.b333vv.metric.ui.tree.node;

import com.intellij.icons.AllIcons;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.ui.tree.TreeCellRenderer;

import javax.swing.*;

public class ProjectNode extends AbstractNode {

    private final transient JavaProject javaProject;

    public ProjectNode(JavaProject javaProject) {
        this.javaProject = javaProject;
    }

    public JavaProject getJavaProject() {
        return javaProject;
    }

    public Icon getIcon() {
        return AllIcons.General.ProjectStructure;
    }

    @Override
    public void render(TreeCellRenderer renderer) {
        renderer.setIcon(getIcon());
        renderer.append(javaProject.getName());
    }
}
