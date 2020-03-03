package org.b333vv.metricsTree.actions.treefilters.projecttree;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import org.b333vv.metricsTree.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;

public class ProjectTreeShowMoodMetrics extends ToggleAction {


    public ProjectTreeShowMoodMetrics() {
        super("Show MOOD Metrics Set",
                "Show or dont show MOOD metrics set", null);
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return MetricsUtils.getProjectMetricsTreeFilter().isMoodMetricsSetVisible();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        MetricsUtils.getProjectMetricsTreeFilter().setMoodMetricsSetVisible(state);
        MetricsUtils.getProjectMetricsPanel().buildTreeModel();
    }
}
