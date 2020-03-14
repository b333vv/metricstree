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

package org.b333vv.metric.exec;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.progress.BackgroundTaskQueue;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiCompiledElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import org.b333vv.metric.model.builder.DependenciesBuilder;
import org.b333vv.metric.model.builder.ProjectModelBuilder;
import org.b333vv.metric.model.calculator.MoodMetricsSetCalculator;
import org.b333vv.metric.model.calculator.RobertMartinMetricsSetCalculator;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.ui.tree.builder.ProjectMetricTreeBuilder;
import org.b333vv.metric.util.MetricsService;
import org.b333vv.metric.util.MetricsUtils;

import javax.swing.tree.DefaultTreeModel;

public class ProjectMetricsRunner {

    private final Project project;
    private final JavaProject javaProject;
    private ProjectModelBuilder projectModelBuilder;
    private ProgressIndicator indicator;
    private int filesCount;
    private int progress = 0;

    private static DependenciesBuilder dependenciesBuilder = new DependenciesBuilder();

    private final Runnable calculate;
    private final Runnable martinMetricSetCalculating;
    private final Runnable moodMetricSetCalculating;
    private final Runnable buildTree;
    private final Runnable cancel;
    private final BackgroundTaskQueue queue;

    public ProjectMetricsRunner(Project project, AnalysisScope scope, JavaProject javaProject ) {
        this.project = project;
        this.javaProject = javaProject;

        queue = new BackgroundTaskQueue(project, "Calculating Metrics");

        calculate = () -> {
            MetricsUtils.getProjectMetricsPanel().setMetricsCalculationPerformed(true);
            projectModelBuilder = new ProjectModelBuilder(javaProject);
            indicator = ProgressManager.getInstance().getProgressIndicator();
            indicator.setText("Initializing");
            filesCount = scope.getFileCount();
            scope.accept(new PsiJavaFileVisitor());
            indicator.setText("Calculating metrics");
        };

        martinMetricSetCalculating = () -> {
            ReadAction.run(() -> projectModelBuilder.calculateMetrics());
            RobertMartinMetricsSetCalculator robertMartinMetricsSetCalculator =
                    new RobertMartinMetricsSetCalculator(scope);
            ReadAction.run(() -> robertMartinMetricsSetCalculator.calculate(javaProject));
        };

        moodMetricSetCalculating = () -> {
            MoodMetricsSetCalculator moodMetricsSetCalculator = new MoodMetricsSetCalculator(scope);
            ReadAction.run(() -> moodMetricsSetCalculator.calculate(javaProject));
        };

        buildTree = () -> {
            ProjectMetricTreeBuilder projectMetricTreeBuilder = new ProjectMetricTreeBuilder(javaProject);
            DefaultTreeModel metricsTreeModel = projectMetricTreeBuilder.createMetricTreeModel();
            MetricsUtils.getProjectMetricsPanel().showResults(metricsTreeModel);
            MetricsUtils.getProjectMetricsPanel().setMetricsCalculationPerformed(false);
        };

        cancel = () -> {
            queue.clear();
            MetricsUtils.getProjectMetricsPanel().cancelMetricsCalculate();
            MetricsUtils.getProjectMetricsPanel().setMetricsCalculationPerformed(false);
        };
    }

    public static DependenciesBuilder getDependenciesBuilder() {
        return dependenciesBuilder;
    }

    public final void execute() {
        MetricsBackgroundableTask classMetricsTask = new MetricsBackgroundableTask(project,
                "Calculating Metrics...", true, calculate, null,
                cancel, null);

        if (!MetricsService.isNeedToConsiderProjectMetrics() && !MetricsService.isNeedToConsiderPackageMetrics()) {
            classMetricsTask.setOnSuccess(buildTree);
            queue.run(classMetricsTask);
            return;
        }
        if (!MetricsService.isNeedToConsiderProjectMetrics()) {
            MetricsBackgroundableTask packageMetricsTask = new MetricsBackgroundableTask(project,
                    "Package Level Metrics: Robert C. Martin Metrics Set Calculating...",
                    true, martinMetricSetCalculating, buildTree,
                    cancel, null);
            queue.run(classMetricsTask);
            queue.run(packageMetricsTask);
            return;
        }
        if (!MetricsService.isNeedToConsiderPackageMetrics()) {
            MetricsBackgroundableTask projectMetricsTask = new MetricsBackgroundableTask(project,
                    "Project Level Metrics: MOOD Metrics Set Calculating...",
                    true, moodMetricSetCalculating, buildTree,
                    cancel, null);
            queue.run(classMetricsTask);
            queue.run(projectMetricsTask);
            return;
        }
        queue.run(classMetricsTask);
        MetricsBackgroundableTask packageMetricsTask = new MetricsBackgroundableTask(project,
                "Package Level Metrics: Robert C. Martin Metrics Set Calculating...",
                true, martinMetricSetCalculating, null,
                cancel, null);
        queue.run(packageMetricsTask);
        MetricsBackgroundableTask projectMetricsTask = new MetricsBackgroundableTask(project,
                "Project Level Metrics: MOOD Metrics Set Calculating...",
                true, moodMetricSetCalculating, buildTree,
                cancel, null);
        queue.run(projectMetricsTask);
    }

    private class PsiJavaFileVisitor extends PsiElementVisitor {
        @Override
        public void visitFile(PsiFile psiFile) {
            super.visitFile(psiFile);
            indicator.checkCanceled();
            if (!psiFile.getFileType().getName().equals("JAVA")) {
                return;
            }
            if (psiFile instanceof PsiCompiledElement) {
                return;
            }
            final FileType fileType = psiFile.getFileType();
            if (fileType.isBinary()) {
                return;
            }
            final VirtualFile virtualFile = psiFile.getVirtualFile();
            final ProjectRootManager rootManager = ProjectRootManager.getInstance(psiFile.getProject());
            final ProjectFileIndex fileIndex = rootManager.getFileIndex();
            if (fileIndex.isExcluded(virtualFile) || !fileIndex.isInContent(virtualFile)) {
                return;
            }
            final String fileName = psiFile.getName();
            indicator.setText("Calculating metrics on class and method levels: processing file " + fileName + "...");
            progress++;
            PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
            projectModelBuilder.addJavaFileToJavaProject(javaProject, psiJavaFile);
             dependenciesBuilder.build(psiJavaFile);
            indicator.setIndeterminate(false);
            indicator.setFraction((double) progress / (double) filesCount);
        }
    }
}
