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
import org.b333vv.metric.exec.Computable;
import org.b333vv.metric.exec.Memorizer;
import org.b333vv.metric.exec.MetricsEventListener;
import org.b333vv.metric.model.builder.ClassModelBuilder;
import org.b333vv.metric.model.code.JavaFile;
import org.b333vv.metric.ui.tree.builder.ClassMetricTreeBuilder;
import org.b333vv.metric.util.MetricsService;
import org.b333vv.metric.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultTreeModel;

public class ClassMetricsPanel extends MetricsTreePanel {

//    private final Computable<PsiJavaFile, JavaFile> c =
//            (key, subject) -> {
//                ClassModelBuilder classModelBuilder = new ClassModelBuilder();
//                return classModelBuilder.buildJavaFile(psiJavaFile);
//            };
//
//    private final Computable<PsiJavaFile, JavaFile> cache = new Memorizer<>(c);

    private ClassMetricsPanel(Project project) {
        super(project, "Metrics.ClassMetricsToolbar");
        MetricsEventListener metricsEventListener = new ClassMetricsEventListener();
        project.getMessageBus().connect(project).subscribe(MetricsEventListener.TOPIC, metricsEventListener);
    }

    public static ClassMetricsPanel newInstance(Project project) {
        ClassMetricsPanel classMetricsPanel = new ClassMetricsPanel(project);
        classMetricsPanel.scope.setPanel(classMetricsPanel);
        return classMetricsPanel;
    }

    @Override
    public void update(@NotNull PsiJavaFile file) {
        MetricsUtils.setClassMetricsValuesEvolutionAdded(false);
        project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).cancelMetricsValuesEvolutionCalculation();
        psiJavaFile = file;
        if (MetricsService.isShowClassMetricsTree()) {
            MetricsUtils.getDumbService().runWhenSmart(() -> calculateMetrics(file));
        }
    }

    public void refresh() {
//        MetricsUtils.setClassMetricsValuesEvolutionAdded(false);
//        project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).cancelMetricsValuesEvolutionCalculation();
        scope.update();
    }

    private void calculateMetrics(@NotNull PsiJavaFile psiJavaFile) {
        MetricsUtils.setClassMetricsTreeExists(false);
        MetricsUtils.getConsole().info("Built metrics tree for " + psiJavaFile.getName());
        ClassModelBuilder classModelBuilder = new ClassModelBuilder();
        JavaFile javaFile = classModelBuilder.buildJavaFile(psiJavaFile);
        metricTreeBuilder = new ClassMetricTreeBuilder(javaFile);
        buildTreeModel();
        MetricsUtils.setClassMetricsTreeExists(true);
//        MetricsUtils.setClassMetricsTreeExists(false);
//        MetricsUtils.getConsole().firstPart("Built metrics tree for " + psiJavaFile.getName());
//        JavaFile javaFile = null;
//        try {
//            String key = psiJavaFile.getPackageName() + "|" + psiJavaFile.getName() + ":" + psiJavaFile.getModificationStamp();
//            javaFile = cache.compute(key, psiJavaFile);
//        } catch (InterruptedException e) {
//            MetricsUtils.getConsole().error("Built metrics tree for " + psiJavaFile.getName() + " interrupted");
//        }
//        if (javaFile != null) {
//            metricTreeBuilder = new ClassMetricTreeBuilder(javaFile);
//            buildTreeModel();
//            MetricsUtils.setClassMetricsTreeExists(true);
//        }
    }

    private class ClassMetricsEventListener implements MetricsEventListener {
        @Override
        public void buildClassMetricsTree() {
            MetricsUtils.setClassMetricsValuesEvolutionAdded(false);
            buildTreeModel();
        }

        @Override
        public void showClassMetricsTree(boolean showClassMetricsTree) {
            if (!showClassMetricsTree) {
                clear();
            } else {
                createUIComponents();
                refresh();
            }
        }

        @Override
        public void refreshClassMetricsTree() {
            refresh();
        }

        @Override
        public void classMetricsValuesEvolutionCalculated(@NotNull DefaultTreeModel metricsTreeModel) {
            showResults(metricsTreeModel);
        }
    }
}
