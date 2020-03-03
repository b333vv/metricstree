package org.b333vv.metric.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.b333vv.metric.util.MetricsUtils;

public class CalculateProjectMetricsAction extends AbstractAction {

    @Override
    protected boolean isEnabled(AnActionEvent e) {
        return true;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        MetricsUtils.calculateProjectMetrics();
    }
}
