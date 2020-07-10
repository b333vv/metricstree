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
import com.intellij.openapi.progress.Task;
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.export.CsvExporter;
import org.b333vv.metric.export.Exporter;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;

import static org.b333vv.metric.task.MetricTaskManager.getProjectModel;

public class ExportToCsvTask extends Task.Backgroundable {
    private static final String STARTED_MESSAGE = "Export class level metrics to .csv started";
    private static final String FINISHED_MESSAGE = "Export class level metrics to .csv finished";
    private static final String CANCELED_MESSAGE = "Export class level metrics to .csv canceled";

    private final String fileName;

    public ExportToCsvTask(String fileName) {
        super(MetricsUtils.getCurrentProject(), "Export Class Level Metrics To CSV");
        this.fileName = fileName;
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(STARTED_MESSAGE);
        JavaProject javaProject = getProjectModel(indicator);
        if (fileName != null) {
            Exporter exporter = new CsvExporter();
            ReadAction.run(() -> exporter.export(fileName, javaProject));
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(FINISHED_MESSAGE);
        MetricsUtils.instance().notify("Class level metrics was successfully exported to " + fileName, myProject);
    }

    @Override
    public void onCancel() {
        super.onCancel();
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(CANCELED_MESSAGE);
    }


}
