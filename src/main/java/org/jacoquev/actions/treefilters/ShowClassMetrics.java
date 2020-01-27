package org.jacoquev.actions.treefilters;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import org.jacoquev.util.MetricsIcons;
import org.jacoquev.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;

public class ShowClassMetrics extends ToggleAction {

    public ShowClassMetrics() {
        super("Show Class Metrics", "Show or dont show class metrics", MetricsIcons.CLASS_METRIC);
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return MetricsUtils.getMetricsTreeFilter().isClassMetricsVisible();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        MetricsUtils.getMetricsTreeFilter().setClassMetricsVisible(state);
        MetricsUtils.getMetricsToolWindowPanel().buildTreeModel();
    }
}
