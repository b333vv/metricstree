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
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiJavaFile;
import git4idea.GitUtil;
import org.b333vv.metric.exec.ClassMetricsValuesEvolutionProcessor;
import org.b333vv.metric.exec.MetricsEventListener;
import org.b333vv.metric.util.MetricsService;
import org.b333vv.metric.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;

class AddClassMetricsValuesEvolutionAction extends AbstractAction {
    private PsiJavaFile psiJavaFile;

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project != null && psiJavaFile != null) {
            ClassMetricsValuesEvolutionProcessor classMetricsValuesEvolutionProcessor = new ClassMetricsValuesEvolutionProcessor(psiJavaFile);
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).clearClassMetricsValuesEvolutionTree();
            MetricsUtils.getDumbService().runWhenSmart(classMetricsValuesEvolutionProcessor::buildClassMetricsValuesEvolutionMap);
            MetricsUtils.setClassMetricsValuesEvolutionAdded(true);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project != null) {
            psiJavaFile = MetricsUtils.getSelectedPsiJavaFile(project);
            event.getPresentation().setEnabled(
                    !MetricsUtils.isMetricsEvolutionCalculationPerforming()
                    && MetricsService.isShowClassMetricsTree()
                    && psiJavaFile != null
                    && GitUtil.isUnderGit(psiJavaFile.getVirtualFile())
                    && !MetricsUtils.isClassMetricsValuesEvolutionAdded());
        }
    }
}
