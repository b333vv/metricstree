package org.jacoquev.actions.treefilters.projecttree;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import org.jacoquev.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;

public class ProjectTreeShowAllowedValueMetrics extends ToggleAction {

    public ProjectTreeShowAllowedValueMetrics() {
        super("Show Metrics with Allowed Values", "Show or dont show metrics with allowed values",
                AllIcons.Actions.Commit);
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return MetricsUtils.getProjectMetricsTreeFilter().isAllowedValueMetricsVisible();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        MetricsUtils.getProjectMetricsTreeFilter().setAllowedValueMetricsVisible(state);
        MetricsUtils.getProjectMetricsPanel().buildTreeModel();
    }
}
