package org.jacoquev.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jacoquev.util.MetricsUtils;
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
