package org.jacoquev.actions.treefilters;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import org.jacoquev.util.MetricsIcons;
import org.jacoquev.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;

public class ShowMethodMetrics extends ToggleAction {


    public ShowMethodMetrics() {
        super("Show Method Metrics", "Show or dont show method metrics", MetricsIcons.METHOD_METRIC);
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return MetricsUtils.getMetricsTreeFilter().isMethodMetricsVisible();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        MetricsUtils.getMetricsTreeFilter().setMethodMetricsVisible(state);
        MetricsUtils.getMetricsToolWindowPanel().buildTreeModel();
    }
}