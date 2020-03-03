package org.b333vv.metricsTree.actions.treefilters.classtree;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import org.b333vv.metricsTree.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;

public class ShowNotApplicableMetrics extends ToggleAction {


    public ShowNotApplicableMetrics() {
        super("Show Not Applicable Metrics", "Show or dont show not applicable metrics",
                AllIcons.General.BalloonWarning);
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return MetricsUtils.getClassMetricsTreeFilter().isNotApplicableMetricsVisible();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        MetricsUtils.getClassMetricsTreeFilter().setNotApplicableMetricsVisible(state);
        MetricsUtils.getClassMetricsPanel().buildTreeModel();
    }
}
