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
import com.intellij.openapi.progress.BackgroundTaskQueue;
import com.intellij.openapi.project.Project;
import org.b333vv.metric.model.builder.DependenciesBuilder;
import org.b333vv.metric.model.calculator.DependenciesCalculator;
import org.b333vv.metric.model.calculator.PackagesCalculator;
import org.b333vv.metric.model.calculator.RobertMartinMetricsSetCalculator;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.ui.chart.builder.ProjectMetricXYChartBuilder;
import org.b333vv.metric.util.MetricsUtils;
import org.knowm.xchart.XYChart;

import java.util.Map;
import java.util.TreeMap;

public class ProjectMetricXYChartProcessor {

    private final Project project;
    private final JavaProject javaProject;
    private final Runnable calculateDependencies;
    private final Runnable calculatePackagesStructure;
    private final Runnable calculatePackageMetrics;
    private final Runnable buildChart;
    private final Runnable cancel;
    private final BackgroundTaskQueue queue;

    public ProjectMetricXYChartProcessor(Project project) {
        this.project = project;
        javaProject = new JavaProject(project.getName());
        DependenciesBuilder dependenciesBuilder = new DependenciesBuilder();
        AnalysisScope scope = new AnalysisScope(project);
        scope.setIncludeTestSource(false);

        MetricsUtils.getConsole().info("Building package level metrics distribution chart for project " + project.getName()
                + " started: processing " + scope.getFileCount() + " java files");

        queue = new BackgroundTaskQueue(project, "Calculating Metrics");

        DependenciesCalculator dependenciesCalculator = new DependenciesCalculator(scope, dependenciesBuilder);

        calculateDependencies = dependenciesCalculator::calculateDependencies;

        PackagesCalculator packagesCalculator = new PackagesCalculator(scope, javaProject);

        calculatePackagesStructure = packagesCalculator::calculatePackagesStructure;

        RobertMartinMetricsSetCalculator packageMetricsCalculator = new RobertMartinMetricsSetCalculator(scope, dependenciesBuilder, javaProject);

        calculatePackageMetrics = packageMetricsCalculator::calculate;

        buildChart = () -> {
            Map<String, Double> instability = new TreeMap<>();
            Map<String, Double> abstractness = new TreeMap<>();

            ProjectMetricXYChartDataBuilder.build(javaProject, instability, abstractness);
            ProjectMetricXYChartBuilder builder = new ProjectMetricXYChartBuilder();
            XYChart xyChart = builder.createChart(instability, abstractness);

            if (xyChart != null) {
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).projectMetricsChartBuilt(xyChart, instability, abstractness);
                MetricsUtils.getConsole().info("Building package level metrics distribution chart for project " + project.getName() + " finished");
            }
            MetricsUtils.setProjectMetricsCalculationPerforming(false);
        };

        cancel = () -> {
            queue.clear();
            MetricsUtils.getConsole().info("Building package level metrics distribution chart for project  " + project.getName() + " canceled");
            MetricsUtils.setProjectMetricsCalculationPerforming(false);
        };
    }

    public final void execute() {
        MetricsBackgroundableTask dependenciesTask = new MetricsBackgroundableTask(project,
                "Calculating Dependencies...", true, calculateDependencies, null,
                cancel, null);
        MetricsBackgroundableTask classMetricsTask = new MetricsBackgroundableTask(project,
                "Building Package Structure...", true, calculatePackagesStructure, null,
                cancel, null);
        MetricsBackgroundableTask packageMetricsTask = new MetricsBackgroundableTask(project,
                "Calculating Package Metrics...", true, calculatePackageMetrics, buildChart,
                cancel, null);
        queue.run(dependenciesTask);
        queue.run(classMetricsTask);
        queue.run(packageMetricsTask);
    }
}
