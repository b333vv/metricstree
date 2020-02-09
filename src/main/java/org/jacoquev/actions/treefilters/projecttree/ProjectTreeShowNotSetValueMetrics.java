package org.jacoquev.actions.treefilters.projecttree;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import org.jacoquev.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;

public class ProjectTreeShowNotSetValueMetrics extends ToggleAction {


    public ProjectTreeShowNotSetValueMetrics() {
        super("Show Metrics Whose Allowed Values Are not Set", "Show or dont show metrics whose allowed values are not set",
                AllIcons.General.BalloonWarning);
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return MetricsUtils.getProjectMetricsTreeFilter().isNotSetValueMetricsVisible();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        MetricsUtils.getProjectMetricsTreeFilter().setNotSetValueMetricsVisible(state);
        MetricsUtils.getProjectMetricsPanel().buildTreeModel();
    }
}
