package org.jacoquev.actions.treefilters.projecttree;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import org.jacoquev.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;

public class ProjectTreeShowDisallowedValueMetrics extends ToggleAction {


    public ProjectTreeShowDisallowedValueMetrics() {
        super("Show Metrics with Disallowed Values", "Show or dont show metrics with disallowed values",
                AllIcons.General.BalloonError);
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return MetricsUtils.getProjectMetricsTreeFilter().isDisallowedValueMetricsVisible();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        MetricsUtils.getProjectMetricsTreeFilter().setDisallowedValueMetricsVisible(state);
        MetricsUtils.getProjectMetricsPanel().buildTreeModel();
    }
}
