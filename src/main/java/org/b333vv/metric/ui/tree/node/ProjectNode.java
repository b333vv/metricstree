package org.b333vv.metric.ui.tree.node;

import com.intellij.icons.AllIcons;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.ui.tree.TreeCellRenderer;

public class ProjectNode extends AbstractNode {

    private final transient JavaProject javaProject;

    public ProjectNode(JavaProject javaProject) {
        this.javaProject = javaProject;
    }

    public JavaProject getJavaProject() {
        return javaProject;
    }

    @Override
    public void render(TreeCellRenderer renderer) {
        renderer.setIcon(AllIcons.General.ProjectStructure);
        renderer.append(javaProject.getName());
    }
}
