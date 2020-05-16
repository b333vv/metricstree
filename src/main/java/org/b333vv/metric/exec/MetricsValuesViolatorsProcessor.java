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
import org.b333vv.metric.model.builder.ProjectModelBuilder;
import org.b333vv.metric.model.calculator.ClassAndMethodsMetricsCalculator;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.ui.tree.builder.MetricsValuesViolatorsTreeBuilder;
import org.b333vv.metric.util.MetricsUtils;

import javax.swing.tree.DefaultTreeModel;

public class MetricsValuesViolatorsProcessor {

    private final Project project;
    private final JavaProject javaProject;
    private final Runnable calculate;
    private final Runnable postCalculate;
    private final Runnable buildTree;
    private final Runnable cancel;
    private final BackgroundTaskQueue queue;

    public MetricsValuesViolatorsProcessor(Project project) {
        this.project = project;
        javaProject = new JavaProject(project.getName());
        DependenciesBuilder dependenciesBuilder = new DependenciesBuilder();
        AnalysisScope scope = new AnalysisScope(project);
        scope.setIncludeTestSource(false);

        MetricsUtils.getConsole().info("Building metrics values violators tree for project " + project.getName()
                + " started: processing " + scope.getFileCount() + " java files");

        queue = new BackgroundTaskQueue(project, "Calculating Metrics");

        ClassAndMethodsMetricsCalculator calculator =
                new ClassAndMethodsMetricsCalculator(scope, dependenciesBuilder, javaProject);

        calculate = calculator::calculate;

        postCalculate = () -> ReadAction.run(calculator::postCalculate);


        buildTree = () -> {
            MetricsValuesViolatorsTreeBuilder builder = new MetricsValuesViolatorsTreeBuilder();
            DefaultTreeModel metricsTreeModel = builder.createMetricTreeModel(javaProject);

            if (metricsTreeModel != null) {
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).metricsValuesViolatorsCalculated(metricsTreeModel);
                MetricsUtils.getConsole().info("Building metrics values violators tree for project " + project.getName() + " finished");
            }

            MetricsUtils.setProjectMetricsCalculationPerforming(false);
        };

        cancel = () -> {
            queue.clear();
            MetricsUtils.getConsole().info("Building metrics values violators tree for project " + project.getName() + " canceled");
            MetricsUtils.setProjectMetricsCalculationPerforming(false);
        };
    }

    public final void execute() {
        MetricsBackgroundableTask classMetricsTask = new MetricsBackgroundableTask(project,
                "Calculating Metrics...", true, calculate, postCalculate,
                cancel, buildTree);

        queue.run(classMetricsTask);
    }
}
