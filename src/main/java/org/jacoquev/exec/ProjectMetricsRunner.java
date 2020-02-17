package org.jacoquev.exec;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.progress.BackgroundTaskQueue;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiCompiledElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import org.jacoquev.model.builder.ProjectModelBuilder;
import org.jacoquev.model.calculator.AttributeHidingFactorCalculator;
import org.jacoquev.model.calculator.MethodHidingFactorCalculator;
import org.jacoquev.model.calculator.PolymorphismFactorCalculator;
import org.jacoquev.model.code.DependencyMap;
import org.jacoquev.model.code.JavaProject;
import org.jacoquev.model.calculator.RobertMartinMetricsSetCalculator;
import org.jacoquev.ui.tree.builder.ProjectMetricTreeBuilder;
import org.jacoquev.util.MetricsUtils;

import javax.swing.tree.DefaultTreeModel;

public class ProjectMetricsRunner {

    private final Project project;
    private final AnalysisScope scope;
    private static DependencyMap dependencyMap;
    private JavaProject javaProject;
    private ProjectModelBuilder projectModelBuilder;
    private ProgressIndicator indicator;
    private int numFiles;
    private int progress = 0;
    private BackgroundTaskQueue queue;

    private Runnable calculate = new Runnable() {
        @Override
        public void run() {
            projectModelBuilder = new ProjectModelBuilder(javaProject);
            indicator = ProgressManager.getInstance().getProgressIndicator();
            indicator.setText("Initializing");
            numFiles = scope.getFileCount();
            scope.accept(new PsiJavaFileVisitor());
            indicator.setText("Calculating metrics");
        }
    };

    private Runnable postCalculate = new Runnable() {
        @Override
        public void run() {
            ReadAction.run(() -> projectModelBuilder.calculateMetrics());
            RobertMartinMetricsSetCalculator robertMartinMetricsSetCalculator = new RobertMartinMetricsSetCalculator();
            ReadAction.run(() -> robertMartinMetricsSetCalculator.calculate(javaProject));

            PolymorphismFactorCalculator polymorphismFactorCalculator = new PolymorphismFactorCalculator(scope);
            ReadAction.run(() -> polymorphismFactorCalculator.calculate(javaProject));

            MethodHidingFactorCalculator methodHidingFactorCalculator = new MethodHidingFactorCalculator(scope);
            ReadAction.run(() -> methodHidingFactorCalculator.calculate(javaProject));

            AttributeHidingFactorCalculator attributeHidingFactorCalculator = new AttributeHidingFactorCalculator(scope);
            ReadAction.run(() -> attributeHidingFactorCalculator.calculate(javaProject));

            ProjectMetricTreeBuilder projectMetricTreeBuilder = new ProjectMetricTreeBuilder(javaProject);
            DefaultTreeModel metricsTreeModel = projectMetricTreeBuilder.createProjectMetricTreeModel();
            MetricsUtils.getProjectMetricsPanel().showResults(metricsTreeModel);
        }
    };

    public ProjectMetricsRunner(Project project, AnalysisScope scope, JavaProject javaProject ) {
        this.project = project;
        this.scope = scope;
        this.javaProject = javaProject;
        dependencyMap = new DependencyMap();
        queue = new BackgroundTaskQueue(project, "Calculating Metrics");
    }

    public static DependencyMap getDependencyMap() {
        return dependencyMap;
    }

    public final void execute() {
        MetricsBackgroundableTask task1 = new MetricsBackgroundableTask(project,
                "Calculating Metrics", true, calculate, null,
                () -> queue.clear(), null);
        MetricsBackgroundableTask task2 = new MetricsBackgroundableTask(project,
                "Post Processing", true, postCalculate, null,
                () -> queue.clear(), null);
        queue.run(task1);
        queue.run(task2);
    }

    class PsiJavaFileVisitor extends PsiElementVisitor {
            @Override
            public void visitFile(PsiFile psiFile) {
                super.visitFile(psiFile);
                indicator.checkCanceled();
                if (!psiFile.getFileType().getName().equals("JAVA")) {
                    return;
                }
                if (psiFile instanceof PsiCompiledElement) {
                    return;
                }
                final FileType fileType = psiFile.getFileType();
                if (fileType.isBinary()) {
                    return;
                }
                final VirtualFile virtualFile = psiFile.getVirtualFile();
                final ProjectRootManager rootManager = ProjectRootManager.getInstance(psiFile.getProject());
                final ProjectFileIndex fileIndex = rootManager.getFileIndex();
                if (fileIndex.isExcluded(virtualFile) || !fileIndex.isInContent(virtualFile)) {
                    return;
                }
                final String fileName = psiFile.getName();
                indicator.setText("Handling file " + fileName);
                progress++;
                PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
                projectModelBuilder.addJavaFileToJavaProject(javaProject, psiJavaFile);

                dependencyMap.build(psiJavaFile);

                indicator.setFraction((double) progress / (double) numFiles);
            }

        }
}
