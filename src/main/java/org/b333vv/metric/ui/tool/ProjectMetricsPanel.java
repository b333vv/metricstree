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

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.b333vv.metric.exec.ProjectMetricsProcessor;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.ui.tree.builder.ProjectMetricTreeBuilder;
import org.b333vv.metric.util.CalculationState;
import org.b333vv.metric.util.EditorController;
import org.b333vv.metric.util.MetricsUtils;

import java.beans.PropertyChangeEvent;

public class ProjectMetricsPanel extends MetricsTreePanel {

    private ProjectMetricsProcessor projectMetricsProcessor;

    private ProjectMetricsPanel(Project project) {
        super(project, "Metrics.ProjectMetricsToolbar");
    }

    public static ProjectMetricsPanel newInstance(Project project) {
        ProjectMetricsPanel projectMetricsPanel = new ProjectMetricsPanel(project);
        MetricsUtils.setProjectMetricsPanel(projectMetricsPanel);
        return projectMetricsPanel;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        CalculationState state = (CalculationState) evt.getNewValue();
        switch (state) {
            case DONE:
                showResults(projectMetricsProcessor.getMetricsTreeModel());
                MetricsUtils.setProjectMetricsCalculationPerforming(false);
                MetricsUtils.getConsole().info("Building metrics tree for project " + project.getName() + " finished");
                break;
            case CANCELED :
                clear();
                MetricsUtils.getConsole().info("Building metrics tree for project " + project.getName() + " canceled");
                MetricsUtils.setProjectMetricsCalculationPerforming(false);
                break;
            case RUNNING:
                MetricsUtils.setProjectMetricsCalculationPerforming(true);
            default: break;
        }
    }

    public void calculateMetrics() {
        clear();
        javaProject = new JavaProject(project.getName());
        AnalysisScope analysisScope = new AnalysisScope(project);
        analysisScope.setIncludeTestSource(false);
        MetricsUtils.getConsole().info("Building metrics tree for project " + project.getName()
                + " started: processing " + analysisScope.getFileCount() + " java files");
        projectMetricsProcessor = new ProjectMetricsProcessor(project, analysisScope, javaProject);
        projectMetricsProcessor.addPropertyChangeListener(this);
        MetricsUtils.getDumbService().runWhenSmart(projectMetricsProcessor::execute);
        metricTreeBuilder = new ProjectMetricTreeBuilder(javaProject);
    }

    @Override
    protected void openInEditor(PsiElement psiElement) {
        if (MetricsUtils.isAutoScrollable()) {
            final EditorController caretMover = new EditorController(project);
            if (psiElement != null) {
                Editor editor = caretMover.openInEditor(psiElement);
                if (editor != null) {
                    caretMover.moveEditorCaret(psiElement);
                }
            }
        }
    }
}
