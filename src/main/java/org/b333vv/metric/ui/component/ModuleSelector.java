package org.b333vv.metric.ui.component;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.configurations.RunConfiguration;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
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
            Module module;
            if (getRunningModule(project) != null) {
                module = getRunningModule(project);
            } else {
                // Get the first real module (not a Kotlin script or source set module)
                module = Arrays.stream(ModuleManager.getInstance(project).getModules())
                        .filter(this::isRealModule)
                        .findFirst()
                        .orElse(ModuleManager.getInstance(project).getModules()[0]);
            }
            e.getPresentation().setText(module.getName());
        } else {
            e.getPresentation().setText(selectedModule.getName());
        }
    }

    @Override
    protected @NotNull DefaultActionGroup createPopupActionGroup(JComponent button) {
        DefaultActionGroup group = new DefaultActionGroup();

        Module runningModule = getRunningModule(project);

        if (runningModule != null) {
            group.add(new DumbAwareAction(runningModule.getName()) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e) {
                    project.getService(UIStateService.class).setSelectedModule(runningModule);
                    onSelectionChange.run();
                }
            });
            group.addSeparator();
        }

        // "Whole Project" option
        // group.add(new DumbAwareAction("Whole Project") {
        // @Override
        // public void actionPerformed(@NotNull AnActionEvent e) {
        // project.getService(UIStateService.class).setSelectedModule(null);
        // onSelectionChange.run();
        // }
        // });

        // group.addSeparator();

        // List all modules, filtering out Kotlin script modules and source set modules
        Module[] modules = ModuleManager.getInstance(project).getModules();
        Module[] realModules = Arrays.stream(modules)
                .filter(this::isRealModule)
                .sorted(Comparator.comparing(Module::getName))
                .toArray(Module[]::new);

        for (Module module : realModules) {
            if (module.equals(runningModule)) {
                continue;
            }
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

    private Module getRunningModule(Project project) {
        // 1. Получаем менеджер запуска для текущего проекта
        RunManager runManager = RunManager.getInstance(project);

        // 2. Берем выбранную (активную) конфигурацию
        RunnerAndConfigurationSettings selectedSetting = runManager.getSelectedConfiguration();

        if (selectedSetting != null) {
            RunConfiguration config = selectedSetting.getConfiguration();

            // 3. Проверяем, привязана ли конфигурация к модулю (например, Java Application)
            if (config instanceof ModuleBasedConfiguration) {
                // 4. Извлекаем модуль
                return ((ModuleBasedConfiguration) config).getConfigurationModule().getModule();
            }
        }
        return null; // Конфигурация не выбрана или не зависит от модуля
    }

    /**
     * Filters out IntelliJ IDEA internal modules (Kotlin scripts, source sets,
     * etc.)
     * and returns only real Gradle/Maven modules.
     *
     * @param module the module to check
     * @return true if this is a real project module, false otherwise
     */
    private boolean isRealModule(Module module) {
        String moduleName = module.getName();

        // Filter out Kotlin script modules (build.gradle.kts, settings.gradle.kts,
        // etc.)
        if (moduleName.startsWith("kotlin.scripts.")) {
            return false;
        }

        // Filter out source set modules (.main, .test, .integrationTest, etc.)
//        if (moduleName.contains(".main") ||
//                moduleName.contains(".test") ||
//                moduleName.contains(".integrationTest") ||
//                moduleName.endsWith(".main") ||
//                moduleName.endsWith(".test") ||
//                moduleName.endsWith(".integrationTest")) {
//            return false;
//        }

        return true;
    }

    private Module getRootModule(Project project) {
        // 1. Получаем базовую директорию проекта
        String projectBasePath = project.getBasePath();
        if (projectBasePath == null)
            return null;

        // 2. Перебираем все модули
        Module[] modules = ModuleManager.getInstance(project).getModules();

        for (Module module : modules) {
            // 3. Получаем корни контента модуля
            VirtualFile[] contentRoots = ModuleRootManager.getInstance(module).getContentRoots();

            for (VirtualFile root : contentRoots) {
                // 4. Если корень модуля совпадает с корнем проекта — это "Главный/Корневой"
                // модуль
                if (root.getPath().equals(projectBasePath)) {
                    return module;
                }
            }
        }
        return null;
    }
}
