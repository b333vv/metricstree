package org.b333vv.metricsTree.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.b333vv.metricsTree.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;

public class RefreshMetricsTreeAction extends AbstractAction {

    @Override
    protected boolean isEnabled(AnActionEvent e) {
        return true;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        MetricsUtils.refreshMetricsTree();
    }
}
