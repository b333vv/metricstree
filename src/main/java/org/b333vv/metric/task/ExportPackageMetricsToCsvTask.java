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

package org.b333vv.metric.task;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.progress.Task;
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.export.CsvPackageMetricsExporter;
import org.b333vv.metric.export.Exporter;
import org.b333vv.metric.model.code.JavaProject;
import org.jetbrains.annotations.NotNull;

public class ExportPackageMetricsToCsvTask extends Task.Backgroundable {
    private static final String STARTED_MESSAGE = "Export package level metrics to .csv started";
    private static final String FINISHED_MESSAGE = "Export package level metrics to .csv finished";
    private static final String CANCELED_MESSAGE = "Export package level metrics to .csv canceled";

    private final String fileName;

    public ExportPackageMetricsToCsvTask(Project project, String fileName) {
        super(project, "Export Package Level Metrics To CSV");
        this.fileName = fileName;
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(STARTED_MESSAGE);
        JavaProject javaProject = myProject.getService(MetricTaskManager.class).getProjectModel(indicator);
        if (fileName != null) {
            Exporter exporter = new CsvPackageMetricsExporter(myProject);
            ReadAction.run(() -> exporter.export(fileName, javaProject));
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(FINISHED_MESSAGE);
//        MetricsUtils.instance().notify("Package level metrics have been successfully exported to " + fileName, myProject);
    }

    @Override
    public void onCancel() {
        super.onCancel();
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(CANCELED_MESSAGE);
    }


}
