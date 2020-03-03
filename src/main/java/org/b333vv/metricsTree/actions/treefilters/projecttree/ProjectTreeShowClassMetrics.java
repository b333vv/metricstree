package org.b333vv.metricsTree.actions.treefilters.projecttree;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import org.b333vv.metricsTree.util.MetricsIcons;
import org.b333vv.metricsTree.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;

public class ProjectTreeShowClassMetrics extends ToggleAction {

    public ProjectTreeShowClassMetrics() {
        super("Show Class Metrics", "Show or dont show class metrics", MetricsIcons.CLASS_METRIC);
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return MetricsUtils.getProjectMetricsTreeFilter().isClassMetricsVisible();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        MetricsUtils.getProjectMetricsTreeFilter().setClassMetricsVisible(state);
        MetricsUtils.getProjectMetricsPanel().buildTreeModel();
    }
}
