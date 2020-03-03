package org.b333vv.metricsTree.actions.treefilters.projecttree;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import org.b333vv.metricsTree.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;

public class ProjectTreeShowLorenzKiddMetrics extends ToggleAction {


    public ProjectTreeShowLorenzKiddMetrics() {
        super("Show Lorenz-Kidd Metrics Set",
                "Show or dont show Lorenz-Kidd metrics set", null);
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return MetricsUtils.getProjectMetricsTreeFilter().isLorenzKiddMetricsSetVisible();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        MetricsUtils.getProjectMetricsTreeFilter().setLorenzKiddMetricsSetVisible(state);
        MetricsUtils.getProjectMetricsPanel().buildTreeModel();
    }
}
