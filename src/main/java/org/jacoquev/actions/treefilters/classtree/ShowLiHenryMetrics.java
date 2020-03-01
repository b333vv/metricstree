package org.jacoquev.actions.treefilters.classtree;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import org.jacoquev.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;

public class ShowLiHenryMetrics extends ToggleAction {


    public ShowLiHenryMetrics() {
        super("Show Li-Henry Metrics Set",
                "Show or dont show Li-Henry metrics set", null);
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return MetricsUtils.getClassMetricsTreeFilter().isLiHenryMetricsSetVisible();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        MetricsUtils.getClassMetricsTreeFilter().setLiHenryMetricsSetVisible(state);
        MetricsUtils.getClassMetricsPanel().buildTreeModel();
    }
}
