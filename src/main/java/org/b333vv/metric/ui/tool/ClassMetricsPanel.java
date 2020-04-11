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
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiJavaFile;
import org.b333vv.metric.exec.Computable;
import org.b333vv.metric.exec.Memorizer;
import org.b333vv.metric.model.builder.ClassModelBuilder;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.ui.tree.builder.ClassMetricTreeBuilder;
import org.b333vv.metric.util.MetricsService;
import org.b333vv.metric.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;

import java.beans.PropertyChangeEvent;

public class ClassMetricsPanel extends MetricsTreePanel {

    private final Computable<PsiJavaFile, JavaProject> c =
            (key, subject) -> {
                ClassModelBuilder classModelBuilder = new ClassModelBuilder();
                return classModelBuilder.buildJavaCode(psiJavaFile);
            };

    private final Computable<PsiJavaFile, JavaProject> cache = new Memorizer<>(c, console);

    private ClassMetricsPanel(Project project) {
        super(project, "Metrics.ClassMetricsToolbar");
    }

    public static ClassMetricsPanel newInstance(Project project) {
        ClassMetricsPanel classMetricsPanel = new ClassMetricsPanel(project);
        MetricsUtils.setClassMetricsPanel(classMetricsPanel);
        classMetricsPanel.scope.setPanel(classMetricsPanel);
        return classMetricsPanel;
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
    }

    public void update(@NotNull PsiJavaFile file) {
        psiJavaFile = file;
        if (MetricsService.isShowClassMetricsTree()) {
            MetricsUtils.getDumbService().runWhenSmart(() -> calculateMetrics(file));
        }
    }

    public void refresh() {
        scope.update();
    }

    private void calculateMetrics(@NotNull PsiJavaFile psiJavaFile) {
//        console.info("Built metrics tree for " + psiJavaFile.getName());
//        ClassModelBuilder classModelBuilder = new ClassModelBuilder();
//        javaProject = classModelBuilder.buildJavaCode(psiJavaFile);
//        metricTreeBuilder = new ClassMetricTreeBuilder(javaProject);
//        buildTreeModel();
        console.info("Built metrics tree for " + psiJavaFile.getName());

        try {
            String key = psiJavaFile.getVirtualFile().getCanonicalPath() + ":" + psiJavaFile.getModificationStamp();
            javaProject = cache.compute(key, psiJavaFile);
        } catch (InterruptedException e) {
            console.error("Built metrics tree for " + psiJavaFile.getName() + " interrupted");
        }

        metricTreeBuilder = new ClassMetricTreeBuilder(javaProject);
        buildTreeModel();
    }
}
