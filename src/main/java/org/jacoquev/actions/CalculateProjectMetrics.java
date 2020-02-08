package org.jacoquev.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jacoquev.util.MetricsUtils;

public class CalculateProjectMetrics extends AbstractAction {

    @Override
    protected boolean isEnabled(AnActionEvent e) {
        return true;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        MetricsUtils.calculateProjectMetrics();
    }
}
