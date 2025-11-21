package org.b333vv.metric.ui.component;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.b333vv.metric.service.UIStateService;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Arrays;
import java.util.Comparator;

public class ModuleSelector extends ComboBoxAction {
    private final Project project;
    private final Runnable onSelectionChange;

    public ModuleSelector(Project project, Runnable onSelectionChange) {
        this.project = project;
        this.onSelectionChange = onSelectionChange;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Module selectedModule = project.getService(UIStateService.class).getSelectedModule();
        if (selectedModule == null) {
            e.getPresentation().setText("Whole Project");
        } else {
            e.getPresentation().setText(selectedModule.getName());
        }
    }

    @Override
    protected @NotNull DefaultActionGroup createPopupActionGroup(JComponent button) {
        DefaultActionGroup group = new DefaultActionGroup();

        // "Whole Project" option
        group.add(new DumbAwareAction("Whole Project") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                project.getService(UIStateService.class).setSelectedModule(null);
                onSelectionChange.run();
            }
        });

        group.addSeparator();

        // List all modules
        Module[] modules = ModuleManager.getInstance(project).getModules();
        Arrays.sort(modules, Comparator.comparing(Module::getName));

        for (Module module : modules) {
            group.add(new DumbAwareAction(module.getName()) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e) {
                    project.getService(UIStateService.class).setSelectedModule(module);
                    onSelectionChange.run();
                }
            });
        }

        return group;
    }
}
