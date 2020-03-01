package org.jacoquev.actions.treefilters.projecttree;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import org.jacoquev.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;

public class ProjectTreeShowLiHenryMetrics extends ToggleAction {


    public ProjectTreeShowLiHenryMetrics() {
        super("Show Li-Henry Metrics Set",
                "Show or dont show Li-Henry metrics set", null);
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return MetricsUtils.getProjectMetricsTreeFilter().isLiHenryMetricsSetVisible();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        MetricsUtils.getProjectMetricsTreeFilter().setLiHenryMetricsSetVisible(state);
        MetricsUtils.getProjectMetricsPanel().buildTreeModel();
    }
}
