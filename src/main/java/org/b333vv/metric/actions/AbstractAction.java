package org.b333vv.metric.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class AbstractAction extends AnAction {
    public AbstractAction() {
        super();
    }

    public AbstractAction(@Nullable String text, @Nullable String description, @Nullable Icon icon) {
        super(text, description, icon);
    }

    @Override
    public void update(AnActionEvent e) {
        Project p = e.getProject();

        if (isVisible(e.getPlace())) {
            e.getPresentation().setVisible(true);
        } else {
            e.getPresentation().setVisible(false);
            e.getPresentation().setEnabled(false);
            return;
        }

        if (p == null || !p.isInitialized() || p.isDisposed()) {
            e.getPresentation().setEnabled(false);
        } else {
            e.getPresentation().setEnabled(isEnabled(e));
        }
    }

    /**
     * @see com.intellij.openapi.actionSystem.ActionPlaces
     */
    protected boolean isVisible(String place) {
        return true;
    }

    protected abstract boolean isEnabled(AnActionEvent e);
}
