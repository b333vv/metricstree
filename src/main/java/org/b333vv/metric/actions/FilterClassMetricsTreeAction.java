package org.b333vv.metric.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;

import java.awt.event.MouseEvent;
import java.util.Objects;

public class FilterClassMetricsTreeAction extends AbstractAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        showPopup(e);
    }

    private void showPopup(@NotNull AnActionEvent e) {
        final ActionManager actionManager = ActionManager.getInstance();
        DefaultActionGroup actionGroup =
                (DefaultActionGroup) actionManager.getAction("Metrics.FilterClassMetricsTreeGroup");

        ActionPopupMenu actionPopupMenu =
                ActionManager.getInstance().createActionPopupMenu(ActionPlaces.TOOLWINDOW_POPUP, actionGroup);

        ToolWindow toolWindow =
                ToolWindowManager.getInstance(Objects.requireNonNull(e.getProject()))
                        .getToolWindow("MetricsTree");
        actionPopupMenu.getComponent().show(
                toolWindow.getComponent().getComponent(0),
                ((MouseEvent) e.getInputEvent()).getX(),
                ((MouseEvent) e.getInputEvent()).getY());
    }
}
