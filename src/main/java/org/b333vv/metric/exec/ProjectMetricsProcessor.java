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
import com.intellij.openapi.progress.BackgroundTaskQueue;
import com.intellij.openapi.project.Project;
import org.b333vv.metric.model.builder.DependenciesBuilder;
import org.b333vv.metric.model.calculator.ClassAndMethodsMetricsCalculator;
import org.b333vv.metric.model.calculator.DependenciesCalculator;
import org.b333vv.metric.model.calculator.MoodMetricsSetCalculator;
import org.b333vv.metric.model.calculator.RobertMartinMetricsSetCalculator;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.ui.tree.builder.ProjectMetricTreeBuilder;
import org.b333vv.metric.util.MetricsService;
import org.b333vv.metric.util.MetricsUtils;

import javax.swing.tree.DefaultTreeModel;

public class ProjectMetricsProcessor {

    private final DependenciesBuilder dependenciesBuilder;

    private final Project project;
    private final JavaProject javaProject;
    private final Runnable dependenciesCalculating;
    private final Runnable classAndMethodsMetricsCalculate;
    private final Runnable martinMetricSetCalculating;
    private final Runnable moodMetricSetCalculating;
    private final Runnable buildTree;
    private final Runnable cancel;
    private final BackgroundTaskQueue queue;

    public ProjectMetricsProcessor(Project project) {
        this.project = project;
        javaProject = new JavaProject(project.getName());
        dependenciesBuilder = new DependenciesBuilder();
        AnalysisScope scope = new AnalysisScope(project);
        scope.setIncludeTestSource(false);

        queue = new BackgroundTaskQueue(project, "Calculating Metrics");

        MetricsUtils.getConsole().info("Building metrics tree for project " + javaProject.getName()
                + " started: processing " + scope.getFileCount() + " java files");

        DependenciesCalculator dependenciesCalculator = new DependenciesCalculator(scope, dependenciesBuilder);

        dependenciesCalculating = dependenciesCalculator::calculateDependencies;

        ClassAndMethodsMetricsCalculator metricsCalculator = new ClassAndMethodsMetricsCalculator(scope, javaProject);

        classAndMethodsMetricsCalculate = metricsCalculator::calculateMetrics;

        martinMetricSetCalculating = () -> {
            RobertMartinMetricsSetCalculator robertMartinMetricsSetCalculator = new RobertMartinMetricsSetCalculator(scope, dependenciesBuilder, javaProject);
            ReadAction.run(robertMartinMetricsSetCalculator::calculate);
        };

        moodMetricSetCalculating = () -> {
            MoodMetricsSetCalculator moodMetricsSetCalculator = new MoodMetricsSetCalculator(scope, dependenciesBuilder, javaProject);
            ReadAction.run(moodMetricsSetCalculator::calculate);
        };

        buildTree = () -> {
            ProjectMetricTreeBuilder projectMetricTreeBuilder = new ProjectMetricTreeBuilder(javaProject);
            DefaultTreeModel metricsTreeModel = projectMetricTreeBuilder.createMetricTreeModel();
//            DefaultTreeModel metricsTreeModel = MetricsService.getProjectTree(javaProject);

            if (metricsTreeModel != null) {
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC)
                        .projectMetricsCalculated(projectMetricTreeBuilder, metricsTreeModel);
                MetricsUtils.getConsole().info("Building metrics tree for project " + project.getName() + " finished");
                MetricsUtils.setProjectMetricsTreeExists(true);
            }

            MetricsUtils.setProjectMetricsCalculationPerforming(false);
        };

        cancel = () -> {
            queue.clear();
            MetricsUtils.getConsole().info("Building metrics tree for project " + project.getName() + " canceled");
            MetricsUtils.setProjectMetricsCalculationPerforming(false);
        };
    }

    public final void execute() {
        MetricsBackgroundableTask classMetricsTask = new MetricsBackgroundableTask(project,
                "Calculating Metrics...", true, dependenciesCalculating, null,
                cancel, null);

        MetricsBackgroundableTask classDeferredMetricsTask = new MetricsBackgroundableTask(project,
                "Calculating Deferred Metrics...", true, classAndMethodsMetricsCalculate, null,
                cancel, null);

        if (!MetricsService.isNeedToConsiderProjectMetrics() && !MetricsService.isNeedToConsiderPackageMetrics()) {
            classDeferredMetricsTask.setOnSuccess(buildTree);
            queue.run(classMetricsTask);
            queue.run(classDeferredMetricsTask);
            return;
        }
        if (!MetricsService.isNeedToConsiderProjectMetrics()) {
            MetricsBackgroundableTask packageMetricsTask = new MetricsBackgroundableTask(project,
                    "Package Level Metrics: Robert C. Martin Metrics Set Calculating...",
                    true, martinMetricSetCalculating, buildTree,
                    cancel, null);
            queue.run(classMetricsTask);
            queue.run(classDeferredMetricsTask);
            queue.run(packageMetricsTask);
            return;
        }
        if (!MetricsService.isNeedToConsiderPackageMetrics()) {
            MetricsBackgroundableTask projectMetricsTask = new MetricsBackgroundableTask(project,
                    "Project Level Metrics: MOOD Metrics Set Calculating...",
                    true, moodMetricSetCalculating, buildTree,
                    cancel, null);
            queue.run(classMetricsTask);
            queue.run(classDeferredMetricsTask);
            queue.run(projectMetricsTask);
            return;
        }
        queue.run(classMetricsTask);
        queue.run(classDeferredMetricsTask);
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
}
