package org.b333vv.metric.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import org.b333vv.metric.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;

class SetAutoScrollableAction extends ToggleAction {

    @Override
    public boolean isSelected(@NotNull AnActionEvent event) {
        return MetricsUtils.isAutoScrollable();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent event, boolean autoScrollable) {
        MetricsUtils.setAutoScrollable(autoScrollable);
    }
}
