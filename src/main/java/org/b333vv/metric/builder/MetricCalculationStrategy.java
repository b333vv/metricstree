package org.b333vv.metric.builder;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.github.javaparser.ast.CompilationUnit;
import org.b333vv.metric.model.code.ProjectElement;

import java.util.List;

public interface MetricCalculationStrategy {
    ProjectElement calculate(Project project, ProgressIndicator indicator);

    default void augment(ProjectElement projectElement, Project project, List<CompilationUnit> allUnits, ProgressIndicator indicator) {
        // default implementation does nothing
    }
}
