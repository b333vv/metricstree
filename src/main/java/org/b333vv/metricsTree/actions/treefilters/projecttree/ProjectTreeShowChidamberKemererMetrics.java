package org.b333vv.metricsTree.actions.treefilters.projecttree;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import org.b333vv.metricsTree.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;

public class ProjectTreeShowChidamberKemererMetrics extends ToggleAction {


    public ProjectTreeShowChidamberKemererMetrics() {
        super("Show Chidamber-Kemerer Metrics Set",
                "Show or dont show Chidamber-Kemerer metrics set", null);
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return MetricsUtils.getProjectMetricsTreeFilter().isChidamberKemererMetricsSetVisible();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        MetricsUtils.getProjectMetricsTreeFilter().setChidamberKemererMetricsSetVisible(state);
        MetricsUtils.getProjectMetricsPanel().buildTreeModel();
    }
}
