package org.jacoquev.actions.treefilters;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import org.jacoquev.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;

public class ShowNotSetValueMetrics extends ToggleAction {


    public ShowNotSetValueMetrics() {
        super("Show Metrics Whose Allowed Values Are not Set", "Show or dont show metrics whose allowed values are not set",
                AllIcons.General.BalloonWarning);
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return MetricsUtils.getMetricsTreeFilter().isNotSetValueMetricsVisible();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        MetricsUtils.getMetricsTreeFilter().setNotSetValueMetricsVisible(state);
        MetricsUtils.getMetricsToolWindowPanel().buildTreeModel();
    }
}
