package org.b333vv.metric.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import org.b333vv.metric.util.MetricsService;
import org.jetbrains.annotations.NotNull;

class SetShowClassMetricsTreeAction extends ToggleAction {

    @Override
    public boolean isSelected(@NotNull AnActionEvent event) {
        return MetricsService.isShowClassMetricsTree();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent event, boolean showClassMetricsTree) {
        MetricsService.setShowClassMetricsTree(showClassMetricsTree);
    }
}
