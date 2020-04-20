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

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.psi.PsiElement;
import org.b333vv.metric.exec.MetricsEventListener;
import org.b333vv.metric.exec.ProjectMetricsProcessor;
import org.b333vv.metric.ui.log.MetricsConsole;
import org.b333vv.metric.util.CalculationState;
import org.b333vv.metric.util.EditorController;
import org.b333vv.metric.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultTreeModel;
import java.beans.PropertyChangeEvent;

public class ProjectMetricsPanel extends MetricsTreePanel {

    private ProjectMetricsPanel(Project project) {
        super(project, "Metrics.ProjectMetricsToolbar");
        MetricsEventListener metricsEventListener = new ProjectMetricsEventListener();
        project.getMessageBus().connect(project).subscribe(MetricsEventListener.TOPIC, metricsEventListener);
    }

    public static ProjectMetricsPanel newInstance(Project project) {
        ProjectMetricsPanel projectMetricsPanel = new ProjectMetricsPanel(project);
//        ----------------
        MetricsUtils.setProjectMetricsPanel(projectMetricsPanel);
//        ----------------
        return projectMetricsPanel;
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

    private class ProjectMetricsEventListener implements MetricsEventListener {
        @Override
        public void projectMetricsCalculated(@NotNull DefaultTreeModel metricsTreeModel) {
            showResults(metricsTreeModel);
        }

        @Override
        public void clearProjectMetricsTree() {
            clear();
        }
    }
}
