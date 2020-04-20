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
import com.intellij.psi.PsiJavaFile;
import org.b333vv.metric.exec.MetricsEventListener;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultTreeModel;

public class ClassMetricsValuesEvolutionPanel extends MetricsTreePanel {

    private ClassMetricsValuesEvolutionPanel(Project project) {
        super(project, "Metrics.ClassMetricsEvolutionToolbar");
        MetricsEventListener metricsEventListener = new ClassMetricsValuesEvolutionEventListener();
        project.getMessageBus().connect(project).subscribe(MetricsEventListener.TOPIC, metricsEventListener);
    }

    public static ClassMetricsValuesEvolutionPanel newInstance(Project project) {
        ClassMetricsValuesEvolutionPanel classMetricsValuesEvolutionPanel = new ClassMetricsValuesEvolutionPanel(project);
        classMetricsValuesEvolutionPanel.scope.setPanel(classMetricsValuesEvolutionPanel);
        return classMetricsValuesEvolutionPanel;
    }

    public void update(@NotNull PsiJavaFile file) {
        psiJavaFile = file;
        clear();
    }

    public PsiJavaFile getPsiJavaFile() {
        return psiJavaFile;
    }

    private class ClassMetricsValuesEvolutionEventListener implements MetricsEventListener {
        @Override
        public void classMetricsValuesEvolutionCalculated(@NotNull DefaultTreeModel metricsTreeModel) {
            showResults(metricsTreeModel);
        }

        @Override
        public void clearClassMetricsValuesEvolutionTree() {
            clear();
        }
    }
}
