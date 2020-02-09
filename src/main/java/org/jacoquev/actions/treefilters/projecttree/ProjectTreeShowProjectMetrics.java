package org.jacoquev.actions.treefilters.projecttree;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import org.jacoquev.util.MetricsIcons;
import org.jacoquev.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;

public class ProjectTreeShowProjectMetrics extends ToggleAction {


    public ProjectTreeShowProjectMetrics() {
        super("Show Project Metrics", "Show or dont show project metrics", MetricsIcons.PROJECT_METRIC);
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return MetricsUtils.getProjectMetricsTreeFilter().isProjectMetricsVisible();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        MetricsUtils.getProjectMetricsTreeFilter().setProjectMetricsVisible(state);
        MetricsUtils.getProjectMetricsPanel().buildTreeModel();
    }
}
