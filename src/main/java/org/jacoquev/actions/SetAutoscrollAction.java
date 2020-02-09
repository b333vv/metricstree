package org.jacoquev.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import org.jacoquev.util.MetricsUtils;

class SetAutoscrollAction extends ToggleAction {

    @Override
    public boolean isSelected(AnActionEvent event) {
        return MetricsUtils.isAutoscroll();
    }

    @Override
    public void setSelected(AnActionEvent event, boolean autoscroll) {
        MetricsUtils.setAutoscroll(autoscroll);
    }
}
