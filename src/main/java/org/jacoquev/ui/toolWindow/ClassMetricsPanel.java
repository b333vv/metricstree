
package org.jacoquev.ui.toolWindow;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaFile;
import org.jacoquev.model.builder.ClassModelBuilder;
import org.jacoquev.model.code.JavaProject;
import org.jacoquev.ui.tree.builder.ClassMetricTreeBuilder;
import org.jacoquev.util.CurrentFileController;
import org.jacoquev.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;

public class ClassMetricsPanel extends MetricsTreePanel {

    public ClassMetricsPanel(Project project) {
        super(project, "Metrics.ClassMetricsToolbar");

        this.scope = new CurrentFileController(project);
        MetricsUtils.setClassMetricsPanel(this);
        subscribeToEvents();
    }

    protected void subscribeToEvents() {
        scope.setPanel(this);
    }

    public void update(@NotNull PsiJavaFile file) {
        psiJavaFile = file;
        MetricsUtils.getDumbService().runWhenSmart(() -> calculateMetrics(file));
    }

    public void refresh() {
        if (psiJavaFile != null) {
            MetricsUtils.getDumbService().runWhenSmart(() -> calculateMetrics(psiJavaFile));
        }
    }

    private void calculateMetrics(@NotNull PsiJavaFile psiJavaFile) {
        ClassModelBuilder classModelBuilder = new ClassModelBuilder();
        JavaProject javaProject = classModelBuilder.buildJavaProject(psiJavaFile);
        metricTreeBuilder = new ClassMetricTreeBuilder(javaProject);
        buildTreeModel();
        console.info("Evaluating metrics values for " + psiJavaFile.getName());
    }

    @Override
    protected void openInEditor(PsiElement psiElement) {}
}
