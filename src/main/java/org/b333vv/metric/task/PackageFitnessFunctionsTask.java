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
import org.b333vv.metric.model.code.JavaPackage;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.service.CacheService;
import org.b333vv.metric.ui.fitnessfunction.FitnessFunction;
import org.jetbrains.annotations.NotNull;
import org.b333vv.metric.builder.PackageFitnessFunctionCalculator;

import java.util.Map;
import java.util.Set;

public class PackageFitnessFunctionsTask extends Task.Backgroundable {
    private static final String GET_FROM_CACHE_MESSAGE = "Try to getProfiles package level fitness functions from cache";
    private static final String STARTED_MESSAGE = "Building package level fitness functions started";
    private static final String FINISHED_MESSAGE = "Building package level fitness functions finished";
    private static final String CANCELED_MESSAGE = "Building package level fitness functions canceled";

    public PackageFitnessFunctionsTask(Project project) {
        super(project, "Building package level fitness functions");
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(GET_FROM_CACHE_MESSAGE);
        Map<FitnessFunction, Set<JavaPackage>> packageFitnessFunctions = myProject.getService(CacheService.class).getUserData(CacheService.PACKAGE_LEVEL_FITNESS_FUNCTION);
        if (packageFitnessFunctions == null) {
            myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(STARTED_MESSAGE);
            packageFitnessFunctions = new PackageFitnessFunctionCalculator().calculate(myProject, indicator);
            myProject.getService(CacheService.class).putUserData(CacheService.PACKAGE_LEVEL_FITNESS_FUNCTION, packageFitnessFunctions);
        }
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(FINISHED_MESSAGE);
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).packageLevelFitnessFunctionIsReady();
    }

    @Override
    public void onCancel() {
        super.onCancel();
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(CANCELED_MESSAGE);
    }
}
