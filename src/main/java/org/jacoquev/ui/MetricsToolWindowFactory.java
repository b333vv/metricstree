package org.jacoquev.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowType;
import com.intellij.ui.content.Content;
import org.jacoquev.ui.log.MetricsLogPanel;
import org.jacoquev.util.MetricsService;

public class MetricsToolWindowFactory implements ToolWindowFactory {
    public static final String TAB_METRICS_TREE = "Metrics tree";
    public static final String TAB_LOGS = "Log";

    private static void addMetricsTreeTab(Project project, ToolWindow toolWindow) {
        MetricsService.setMetricsSettings(project);
        CurrentFileController currentFileController = new CurrentFileController(project);
        MetricsToolWindowPanel metricsToolWindowPanel = new MetricsToolWindowPanel(currentFileController, project);
        Content treeContent = toolWindow.getContentManager().getFactory()
                .createContent(
                        metricsToolWindowPanel,
                        TAB_METRICS_TREE,
                        false);
        toolWindow.getContentManager().addDataProvider(metricsToolWindowPanel);
        toolWindow.getContentManager().addContent(treeContent);
    }

    private static void addLogTab(Project project, ToolWindow toolWindow) {
        Content logContent = toolWindow.getContentManager().getFactory()
                .createContent(
                        new MetricsLogPanel(toolWindow, project),
                        TAB_LOGS,
                        false);
        toolWindow.getContentManager().addContent(logContent);
    }

    @Override
    public void createToolWindowContent(Project project, final ToolWindow toolWindow) {
        addMetricsTreeTab(project, toolWindow);
        addLogTab(project, toolWindow);
        toolWindow.setType(ToolWindowType.DOCKED, null);
    }
}
