package org.b333vv.metric.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.b333vv.metric.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;

public class RefreshMetricsTreeAction extends AbstractAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        MetricsUtils.refreshMetricsTree();
    }
}
