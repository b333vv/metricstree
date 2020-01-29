package org.jacoquev.actions.treefilters;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import org.jacoquev.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;

public class ShowDisallowedValueMetrics extends ToggleAction {


    public ShowDisallowedValueMetrics() {
        super("Show Metrics with Disallowed Values", "Show or dont show metrics with disallowed values",
                AllIcons.General.BalloonError);
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return MetricsUtils.getMetricsTreeFilter().isDisallowedValueMetricsVisible();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        MetricsUtils.getMetricsTreeFilter().setDisallowedValueMetricsVisible(state);
        MetricsUtils.getMetricsToolWindowPanel().buildTreeModel();
    }
}