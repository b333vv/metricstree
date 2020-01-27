package org.jacoquev.actions.treefilters;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import org.jacoquev.util.MetricsIcons;
import org.jetbrains.annotations.NotNull;

public class ShowProjectMetrics extends ToggleAction {


    public ShowProjectMetrics() {
        super("Show Project Metrics", "Show or dont show project metrics", MetricsIcons.PROJECT_METRIC);
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return false;
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {

    }
}
