/*
 * Copyright 2020 b333vv
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.b333vv.metric.ui.tool;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowType;
import com.intellij.ui.content.Content;
import org.b333vv.metric.ui.log.MetricsLogPanel;
import org.b333vv.metric.util.MetricsService;
import org.jetbrains.annotations.NotNull;

public class MetricsToolWindowFactory implements ToolWindowFactory {
    public static final String TAB_CLASS_METRICS_TREE = "Class Metrics Tree";
    public static final String TAB_PROJECT_METRICS_TREE = "Project Metrics Tree";
    public static final String TAB_METRICS_CHART = "Metrics Charts";
    public static final String TAB_LOGS = "Log";

    private static void addClassMetricsTreeTab(Project project, ToolWindow toolWindow) {
        ClassMetricsPanel classMetricsPanel = ClassMetricsPanel.newInstance(project);
        Content treeContent = toolWindow.getContentManager().getFactory()
                .createContent(classMetricsPanel, TAB_CLASS_METRICS_TREE, false);
        toolWindow.getContentManager().addDataProvider(classMetricsPanel);
        toolWindow.getContentManager().addContent(treeContent);
    }

    private static void addProjectMetricsTreeTab(Project project, ToolWindow toolWindow) {
        ProjectMetricsPanel projectMetricsPanel = ProjectMetricsPanel.newInstance(project);
        Content treeContent = toolWindow.getContentManager().getFactory()
                .createContent(
                        projectMetricsPanel, TAB_PROJECT_METRICS_TREE, false);
        toolWindow.getContentManager().addDataProvider(projectMetricsPanel);
        toolWindow.getContentManager().addContent(treeContent);
    }

    private static void addMetricsChartTab(Project project, ToolWindow toolWindow) {
        MetricsChartPanel metricsChartPanel = new MetricsChartPanel(project);
        Content chartContent = toolWindow.getContentManager().getFactory()
                .createContent(
                        metricsChartPanel, TAB_METRICS_CHART, false);
        toolWindow.getContentManager().addDataProvider(metricsChartPanel);
        toolWindow.getContentManager().addContent(chartContent);
    }

    private static void addLogTab(Project project, ToolWindow toolWindow) {
        Content logContent = toolWindow.getContentManager().getFactory()
                .createContent(
                        new MetricsLogPanel(project), TAB_LOGS, false);
        toolWindow.getContentManager().addContent(logContent);
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull final ToolWindow toolWindow) {
        MetricsService.init(project);
        addClassMetricsTreeTab(project, toolWindow);
        addProjectMetricsTreeTab(project, toolWindow);
        addMetricsChartTab(project, toolWindow);
        addLogTab(project, toolWindow);
        toolWindow.setType(ToolWindowType.DOCKED, null);
    }
}
