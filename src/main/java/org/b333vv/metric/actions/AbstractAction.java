package org.b333vv.metric.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

public abstract class AbstractAction extends AnAction {
    public AbstractAction() {
        super();
    }

    @Override
    public void update(AnActionEvent e) {
        Project p = e.getProject();
        e.getPresentation().setVisible(true);
        e.getPresentation().setEnabled(check(p));
    }

    private boolean check(Project p) {
        return !(p == null || !p.isInitialized() || p.isDisposed());
    }
}
