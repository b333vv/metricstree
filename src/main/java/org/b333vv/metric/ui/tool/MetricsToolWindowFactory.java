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

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowType;
import com.intellij.psi.PsiJavaFile;
import com.intellij.ui.content.Content;
import org.b333vv.metric.model.code.JavaFile;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.task.DependenciesTask;
import org.b333vv.metric.task.MetricTaskCache;
import org.b333vv.metric.ui.log.MetricsLogPanel;
import org.b333vv.metric.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;

public class MetricsToolWindowFactory implements ToolWindowFactory {
    public static final String TAB_CLASS_METRICS_TREE = "Class Metrics";
    public static final String TAB_PROJECT_METRICS_TREE = "Project Metrics";
    public static final String TAB_PROFILES = "Metric Fitness Functions";
    public static final String TAB_LOGS = "Log";
    public static final String TAB_METRICS_EVOLUTION = "Metrics Evolution";

    private static void addClassMetricsTreeTab(Project project, ToolWindow toolWindow) {
        ClassMetricsPanel classMetricsPanel = ClassMetricsPanel.newInstance(project);
        PsiJavaFile psiJavaFile = MetricsUtils.getSelectedPsiJavaFile(project);
        if (psiJavaFile != null) {
            classMetricsPanel.update(psiJavaFile);
        }
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

    private static void addFitnessFunctionTab(Project project, ToolWindow toolWindow) {
        var fitnessFunctionPanel = new FitnessFunctionPanel(project, toolWindow);
        var fitnessFunctionContent = toolWindow.getContentManager().getFactory()
                .createContent(
                        fitnessFunctionPanel, TAB_PROFILES, false);
        toolWindow.getContentManager().addDataProvider(fitnessFunctionPanel);
        toolWindow.getContentManager().addContent(fitnessFunctionContent);
    }

    private static void addLogTab(Project project, ToolWindow toolWindow) {
        Content logContent = toolWindow.getContentManager().getFactory()
                .createContent(
                        new MetricsLogPanel(project), TAB_LOGS, false);
        toolWindow.getContentManager().addContent(logContent);
    }

    private static void addMetricsEvolutionTab(Project project, ToolWindow toolWindow) {
        MetricsEvolutionPanel metricsEvolutionPanel = new MetricsEvolutionPanel(project);
        Content evolutionContent = toolWindow.getContentManager().getFactory()
                .createContent(metricsEvolutionPanel, TAB_METRICS_EVOLUTION, false);
        toolWindow.getContentManager().addDataProvider(metricsEvolutionPanel);
        toolWindow.getContentManager().addContent(evolutionContent);
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull final ToolWindow toolWindow) {
        MetricsUtils.setCurrentProject(project);
        addClassMetricsTreeTab(project, toolWindow);
        addProjectMetricsTreeTab(project, toolWindow);
        addFitnessFunctionTab(project, toolWindow);
        addLogTab(project, toolWindow);
        addMetricsEvolutionTab(project, toolWindow);
        toolWindow.setType(ToolWindowType.DOCKED, null);
    }
}
