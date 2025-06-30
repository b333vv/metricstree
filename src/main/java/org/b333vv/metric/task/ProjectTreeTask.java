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
import org.jetbrains.annotations.NotNull;

import org.b333vv.metric.service.CacheService;

import javax.swing.tree.DefaultTreeModel;
import java.util.function.Supplier;


public class  ProjectTreeTask extends Task.Backgroundable {
    private static final String STARTED_MESSAGE = "Building tree model started";
    private static final String FINISHED_MESSAGE = "Building tree model finished";
    private static final String CANCELED_MESSAGE = "Building tree model canceled";

    private final Supplier<DefaultTreeModel> modelSupplier;
    private final CacheService cacheService;
    private DefaultTreeModel treeModel;

    public ProjectTreeTask(Project project, Supplier<DefaultTreeModel> modelSupplier, CacheService cacheService) {
        super(project, "Build Project Tree");
        this.modelSupplier = modelSupplier;
        this.cacheService = cacheService;
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(STARTED_MESSAGE);
        treeModel = modelSupplier.get();
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
        cacheService.putUserData(CacheService.PROJECT_TREE, treeModel);
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(FINISHED_MESSAGE);
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC)
                .projectMetricsTreeIsReady(treeModel);
    }

    @Override
    public void onCancel() {
        super.onCancel();
        myProject.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(CANCELED_MESSAGE);
    }
}