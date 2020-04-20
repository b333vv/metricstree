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

package org.b333vv.metric.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiCompiledElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import org.b333vv.metric.exec.ClassMetricsValuesEvolutionProcessor;
import org.b333vv.metric.exec.MetricsEventListener;
import org.b333vv.metric.ui.tool.ClassMetricsValuesEvolutionPanel;
import org.b333vv.metric.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;

public class CalculateClassMetricsEvolutionAction extends AbstractAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project != null) {
            PsiJavaFile psiJavaFile = MetricsUtils.getSelectedPsiJavaFile(project);
            if (psiJavaFile != null) {
                ClassMetricsValuesEvolutionProcessor classMetricsValuesEvolutionProcessor = new ClassMetricsValuesEvolutionProcessor(psiJavaFile);
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).clearClassMetricsValuesEvolutionTree();
                MetricsUtils.getDumbService().runWhenSmart(classMetricsValuesEvolutionProcessor::buildClassMetricsValuesEvolutionMap);
            }
        }
    }

    @Override
    public void update (AnActionEvent e) {
        e.getPresentation().setEnabled(MetricsUtils.isMetricsEvolutionCalculationPossible());
    }
}
