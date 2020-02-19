package org.jacoquev.actions.treefilters.classtree;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import org.jacoquev.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;

public class ShowChidamberKemererMetrics extends ToggleAction {


    public ShowChidamberKemererMetrics() {
        super("Show Chidamber-Kemerer Metrics Set",
                "Show or dont show Chidamber-Kemerer metrics set", null);
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return MetricsUtils.getClassMetricsTreeFilter().isChidamberKemererMetricsSetVisible();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        MetricsUtils.getClassMetricsTreeFilter().setChidamberKemererMetricsSetVisible(state);
        MetricsUtils.getClassMetricsPanel().buildTreeModel();
    }
}
