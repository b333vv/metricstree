package org.b333vv.metric.actions.treefilters.classtree;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import org.b333vv.metric.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;

public class ShowLorenzKiddMetrics extends ToggleAction {


    public ShowLorenzKiddMetrics() {
        super("Show Lorenz-Kidd Metrics Set",
                "Show or dont show Lorenz-Kidd metrics set", null);
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return MetricsUtils.getClassMetricsTreeFilter().isLorenzKiddMetricsSetVisible();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        MetricsUtils.getClassMetricsTreeFilter().setLorenzKiddMetricsSetVisible(state);
        MetricsUtils.getClassMetricsPanel().buildTreeModel();
    }
}
