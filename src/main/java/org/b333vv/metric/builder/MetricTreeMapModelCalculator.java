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

package org.b333vv.metric.builder;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.code.JavaCode;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.task.MetricTaskManager;
import org.b333vv.metric.ui.treemap.builder.MetricTypeColorProvider;
import org.b333vv.metric.ui.treemap.builder.TreeMapBuilder;
import org.b333vv.metric.ui.treemap.presentation.MetricTreeMap;

public class MetricTreeMapModelCalculator {
    private final Project project;

    public MetricTreeMapModelCalculator(Project project) {
        this.project = project;
    }

    public MetricTreeMap<JavaCode> calculate(ProgressIndicator indicator) {
        AnalysisScope scope = new AnalysisScope(project);
        scope.setIncludeTestSource(false);
        JavaProject javaProject = project.getService(MetricTaskManager.class).getClassAndMethodModel(indicator);
        TreeMapBuilder treeMapBuilder = new TreeMapBuilder(javaProject);
        MetricTreeMap<JavaCode> metricTreeMap = treeMapBuilder.getTreeMap();
        metricTreeMap.setColorProvider(new MetricTypeColorProvider(MetricType.NCSS, project));
        metricTreeMap.setSelectionChangedAction((String text) ->
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC)
                        .setProjectPanelBottomText(text));
        metricTreeMap.setClickedAction((JavaClass javaClass) ->
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC)
                        .projectTreeMapCellClicked(javaClass));
        return metricTreeMap;
    }
}
