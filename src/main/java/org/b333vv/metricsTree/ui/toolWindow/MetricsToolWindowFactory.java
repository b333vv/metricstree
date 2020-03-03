package org.b333vv.metricsTree.ui.toolWindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowType;
import com.intellij.ui.content.Content;
import org.b333vv.metricsTree.ui.log.MetricsLogPanel;
import org.b333vv.metricsTree.util.MetricsService;
import org.jetbrains.annotations.NotNull;

public class MetricsToolWindowFactory implements ToolWindowFactory {
    public static final String TAB_CLASS_METRICS_TREE = "Class Metrics Tree";
    public static final String TAB_PROJECT_METRICS_TREE = "Project Metrics Tree";
    public static final String TAB_LOGS = "Log";

    private static void addClassMetricsTreeTab(Project project, ToolWindow toolWindow) {
        ClassMetricsPanel classMetricsPanel = new ClassMetricsPanel(project);
        Content treeContent = toolWindow.getContentManager().getFactory()
                .createContent(classMetricsPanel, TAB_CLASS_METRICS_TREE, false);
        toolWindow.getContentManager().addDataProvider(classMetricsPanel);
        toolWindow.getContentManager().addContent(treeContent);
    }

    private static void addProjectMetricsTreeTab(Project project, ToolWindow toolWindow) {
        ProjectMetricsPanel projectMetricsPanel = new ProjectMetricsPanel(project);
        Content treeContent = toolWindow.getContentManager().getFactory()
                .createContent(
                        projectMetricsPanel, TAB_PROJECT_METRICS_TREE, false);
        toolWindow.getContentManager().addDataProvider(projectMetricsPanel);
        toolWindow.getContentManager().addContent(treeContent);
    }

    private static void addLogTab(Project project, ToolWindow toolWindow) {
        Content logContent = toolWindow.getContentManager().getFactory()
                .createContent(
                        new MetricsLogPanel(toolWindow, project), TAB_LOGS, false);
        toolWindow.getContentManager().addContent(logContent);
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull final ToolWindow toolWindow) {
        MetricsService.init(project);
        addClassMetricsTreeTab(project, toolWindow);
        addProjectMetricsTreeTab(project, toolWindow);
        addLogTab(project, toolWindow);
        toolWindow.setType(ToolWindowType.DOCKED, null);
    }
}
