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
import org.b333vv.metric.exec.ProjectMetricsRunner;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.ui.tree.builder.ProjectMetricTreeBuilder;
import org.b333vv.metric.util.EditorController;
import org.b333vv.metric.util.MetricsUtils;

public class ProjectMetricsPanel extends MetricsTreePanel {
    private boolean metricsCalculationPerformed;
    public ProjectMetricsPanel(Project project) {
        super(project, "Metrics.ProjectMetricsToolbar");
        MetricsUtils.setProjectMetricsPanel(this);
    }

    public boolean isMetricsCalculationPerformed() {
        return metricsCalculationPerformed;
    }

    public void setMetricsCalculationPerformed(boolean metricsCalculationPerformed) {
        this.metricsCalculationPerformed = metricsCalculationPerformed;
    }

    public void calculateMetrics() {
        clear();
        metricsCalculationPerformed = true;
        javaProject = new JavaProject(project.getName());
        console.info("Building metrics tree for project " + project.getName());
        AnalysisScope analysisScope = new AnalysisScope(project);
        analysisScope.setIncludeTestSource(false);
        console.info(analysisScope.getFileCount() + " java files will be processed");
        ProjectMetricsRunner projectMetricsRunner = new ProjectMetricsRunner(project, analysisScope, javaProject);
        MetricsUtils.getDumbService().runWhenSmart(() -> projectMetricsRunner.execute());
        metricTreeBuilder = new ProjectMetricTreeBuilder(javaProject);
    }

    public void cancelMetricsCalculate() {
        clear();
        metricsCalculationPerformed = false;
        console.info("Building metrics tree for project " + project.getName() + " canceled");
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
