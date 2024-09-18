package org.b333vv.metric.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.util.NlsActions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class AbstractToggleAction extends ToggleAction {

    public AbstractToggleAction() {
        super();
    }

    public AbstractToggleAction(@Nullable @NlsActions.ActionText final String text,
                        @Nullable @NlsActions.ActionDescription final String description,
                        @Nullable final Icon icon) {
        super(text, description, icon);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
