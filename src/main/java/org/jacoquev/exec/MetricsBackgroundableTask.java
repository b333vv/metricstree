package org.jacoquev.exec;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MetricsBackgroundableTask extends Task.Backgroundable {

    Runnable task;
    Runnable onSuccess;
    Runnable onCancel;
    Runnable onFinished;

    public MetricsBackgroundableTask(@Nullable Project project,
                                     @Nls(capitalization = Nls.Capitalization.Title) @NotNull String title,
                                     boolean canBeCancelled,
                                     Runnable task,
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
}
