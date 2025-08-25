package org.b333vv.metric.builder;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import org.b333vv.metric.model.code.JavaProject;

public interface MetricCalculationStrategy {
    JavaProject calculate(Project project, ProgressIndicator indicator);
}
