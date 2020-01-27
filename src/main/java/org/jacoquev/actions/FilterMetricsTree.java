package org.jacoquev.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;

import java.awt.event.MouseEvent;

public class FilterMetricsTree extends AbstractAction {

    @Override
    protected boolean isEnabled(AnActionEvent e) {
        return true;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        showPopup(e);
    }

    private void showPopup(@NotNull AnActionEvent e) {
        final ActionManager actionManager = ActionManager.getInstance();
        DefaultActionGroup actionGroup =
                (DefaultActionGroup) actionManager.getAction("Metrics.FilterMetricsTreeGroup");

        ActionPopupMenu actionPopupMenu =
                ActionManager.getInstance().createActionPopupMenu(ActionPlaces.TOOLWINDOW_POPUP, actionGroup);

        ToolWindow toolWindow =
                ToolWindowManager.getInstance(e.getProject())
                        .getToolWindow("Metrics");
        actionPopupMenu.getComponent().show(
                toolWindow.getComponent().getComponent(0),
                ((MouseEvent) e.getInputEvent()).getX(),
                ((MouseEvent) e.getInputEvent()).getY());
    }
}
