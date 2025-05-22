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

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.progress.Task;
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.ui.fitnessfunction.FitnessFunction;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

import static org.b333vv.metric.builder.ClassLevelFitnessFunctionBuilder.classesByMetricsProfileDistribution;
import static org.b333vv.metric.task.MetricTaskManager.getClassAndMethodModel;

public class ClassFitnessFunctionsTask extends Task.Backgroundable {
    private static final String GET_FROM_CACHE_MESSAGE = "Try to getProfiles class level fitness functions from cache";
    private static final String STARTED_MESSAGE = "Building class level fitness functions started";
    private static final String FINISHED_MESSAGE = "Building class level fitness functions finished";
    private static final String CANCELED_MESSAGE = "Building class level fitness functions canceled";

    public ClassFitnessFunctionsTask(Project project) {
        super(project, "Building class level fitness functions");
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(GET_FROM_CACHE_MESSAGE);
        Map<FitnessFunction, Set<JavaClass>> classFitnessFunctions = myProject.getService(MetricTaskCache.class).getUserData(MetricTaskCache.CLASS_LEVEL_FITNESS_FUNCTION);
        if (classFitnessFunctions == null) {
            myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(STARTED_MESSAGE);
            JavaProject javaProject = myProject.getService(MetricTaskManager.class).getClassAndMethodModel(indicator);
            classFitnessFunctions = classesByMetricsProfileDistribution(javaProject);
            myProject.getService(MetricTaskCache.class).putUserData(MetricTaskCache.CLASS_LEVEL_FITNESS_FUNCTION, classFitnessFunctions);
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(FINISHED_MESSAGE);
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).classLevelFitnessFunctionIsReady();
    }

    @Override
    public void onCancel() {
        super.onCancel();
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(CANCELED_MESSAGE);
    }
}
