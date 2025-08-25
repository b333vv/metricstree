package org.b333vv.metric.builder;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.ui.log.MetricsConsole;

public class JavaParserCalculationStrategy implements MetricCalculationStrategy {
    @Override
    public JavaProject calculate(Project project, ProgressIndicator indicator) {
        MetricsConsole console = project.getService(MetricsConsole.class);
        console.info("JavaParser calculation engine is not yet implemented.");
        
        // Return a new, empty JavaProject to avoid NullPointerExceptions downstream.
        return new JavaProject(project.getName());
    }
}