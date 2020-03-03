
package org.b333vv.metricsTree.ui.toolWindow;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.b333vv.metricsTree.exec.ProjectMetricsRunner;
import org.b333vv.metricsTree.ui.tree.builder.ProjectMetricTreeBuilder;
import org.b333vv.metricsTree.model.code.JavaProject;
import org.b333vv.metricsTree.util.EditorOpener;
import org.b333vv.metricsTree.util.MetricsUtils;

public class ProjectMetricsPanel extends MetricsTreePanel {
    public ProjectMetricsPanel(Project project) {
        super(project, "Metrics.ProjectMetricsToolbar");
        MetricsUtils.setProjectMetricsPanel(this);
    }

    public void calculateMetrics() {
        javaProject = new JavaProject(project.getName());
        console.info("Evaluating metrics values for project: " + project.getName());
        AnalysisScope analysisScope = new AnalysisScope(project);
        analysisScope.setIncludeTestSource(false);
        ProjectMetricsRunner projectMetricsRunner = new ProjectMetricsRunner(project, analysisScope, javaProject);
        projectMetricsRunner.execute();
        metricTreeBuilder = new ProjectMetricTreeBuilder(javaProject);
    }

    @Override
    protected void openInEditor(PsiElement psiElement) {
        if (MetricsUtils.isAutoScrollable()) {
            final EditorOpener caretMover = new EditorOpener(project);
            if (psiElement != null) {
                Editor editor = caretMover.openInEditor(psiElement);
                if (editor != null) {
                    caretMover.moveEditorCaret(psiElement);
                }
            }
        }
    }
}
