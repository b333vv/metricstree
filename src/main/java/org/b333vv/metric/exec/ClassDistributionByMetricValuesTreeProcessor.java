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
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.ui.tree.builder.SortedByMetricsValuesClassesTreeBuilder;
import org.b333vv.metric.util.MetricsUtils;

import javax.swing.tree.DefaultTreeModel;

public class ClassDistributionByMetricValuesTreeProcessor {

    private final Project project;
    private final JavaProject javaProject;
    private final Runnable calculateDependencies;
    private final Runnable calculateMetrics;
    private final Runnable buildTree;
    private final Runnable cancel;
    private final BackgroundTaskQueue queue;

    public ClassDistributionByMetricValuesTreeProcessor(Project project) {
        this.project = project;
        javaProject = new JavaProject(project.getName());
        DependenciesBuilder dependenciesBuilder = new DependenciesBuilder();
        AnalysisScope scope = new AnalysisScope(project);
        scope.setIncludeTestSource(false);

        MetricsUtils.getConsole().info("Building class distribution by metric values tree for project " + project.getName()
                + " started: processing " + scope.getFileCount() + " java files");

        queue = new BackgroundTaskQueue(project, "Calculating Metrics");

        DependenciesCalculator dependenciesCalculator = new DependenciesCalculator(scope, dependenciesBuilder);

        calculateDependencies = dependenciesCalculator::calculateDependencies;

        ClassAndMethodsMetricsCalculator metricsCalculator = new ClassAndMethodsMetricsCalculator(scope, javaProject);

        calculateMetrics = metricsCalculator::calculateMetrics;

        buildTree = () -> {
            SortedByMetricsValuesClassesTreeBuilder builder = new SortedByMetricsValuesClassesTreeBuilder();
            DefaultTreeModel metricsTreeModel = builder.createMetricTreeModel(javaProject);

            if (metricsTreeModel != null) {
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).classesSortedByMetricsValues(metricsTreeModel);
                MetricsUtils.getConsole().info("Building class distribution by metric values tree for project " + project.getName() + " finished");
            }

            MetricsUtils.setProjectMetricsCalculationPerforming(false);
        };

        cancel = () -> {
            queue.clear();
            MetricsUtils.getConsole().info("Building class distribution by metric values tree for project " + project.getName() + " canceled");
            MetricsUtils.setProjectMetricsCalculationPerforming(false);
        };
    }

    public final void execute() {
        MetricsBackgroundableTask dependenciesTask = new MetricsBackgroundableTask(project,
                "Calculating Dependencies...", true, calculateDependencies, null,
                cancel, null);
        MetricsBackgroundableTask classMetricsTask = new MetricsBackgroundableTask(project,
                "Calculating Metrics...", true, calculateMetrics, buildTree,
                cancel, null);
        queue.run(dependenciesTask);
        queue.run(classMetricsTask);
    }
}
