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

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MetricsBackgroundableTask extends Task.Backgroundable {

    private final Runnable task;
    private final Runnable onCancel;
    private Runnable onSuccess;
    private Runnable onFinished;

    public MetricsBackgroundableTask(@Nullable Project project,
                                     @Nls(capitalization = Nls.Capitalization.Title) @NotNull String title,
                                     boolean canBeCancelled,
                                     @NotNull Runnable task,
                                     Runnable onSuccess,
                                     Runnable onCancel,
                                     Runnable onFinished) {
        super(project, title, canBeCancelled);
        this.task = task;
        this.onSuccess = onSuccess;
        this.onCancel = onCancel;
        this.onFinished = onFinished;
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        task.run();
    }

    @Override
    public void onSuccess() {
        if (onSuccess != null) {
            onSuccess.run();
        }
    }

    @Override
    public void onFinished() {
        if (onFinished != null) {
            onFinished.run();
        }
    }

    @Override
    public void onCancel() {
        if (onCancel != null) {
            onCancel.run();
        }
    }

    public void setOnFinished(Runnable onFinished) {
        this.onFinished = onFinished;
    }

    public void setOnSuccess(Runnable onSuccess) {
        this.onSuccess = onSuccess;
    }
}
