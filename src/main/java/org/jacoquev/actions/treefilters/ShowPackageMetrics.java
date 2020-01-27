package org.jacoquev.actions.treefilters;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import org.jacoquev.util.MetricsIcons;
import org.jetbrains.annotations.NotNull;

public class ShowPackageMetrics extends ToggleAction {


    public ShowPackageMetrics() {
        super("Show Package Metrics", "Show or dont show package metrics", MetricsIcons.PACKAGE_METRIC);
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return false;
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {

    }
}
