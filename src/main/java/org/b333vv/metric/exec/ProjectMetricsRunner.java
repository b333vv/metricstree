package org.b333vv.metric.exec;

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
import org.b333vv.metric.model.builder.ProjectModelBuilder;
import org.b333vv.metric.model.calculator.MoodMetricsSetCalculator;
import org.b333vv.metric.model.calculator.RobertMartinMetricsSetCalculator;
import org.b333vv.metric.model.builder.DependenciesBuilder;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.ui.tree.builder.ProjectMetricTreeBuilder;
import org.b333vv.metric.util.MetricsService;
import org.b333vv.metric.util.MetricsUtils;

import javax.swing.tree.DefaultTreeModel;

public class ProjectMetricsRunner {

    private final Project project;
    private final AnalysisScope scope;
    private static DependenciesBuilder dependenciesBuilder;
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

    private Runnable martinMetricSetCalculating = new Runnable() {
        @Override
        public void run() {
            ReadAction.run(() -> projectModelBuilder.calculateMetrics());
            RobertMartinMetricsSetCalculator robertMartinMetricsSetCalculator = new RobertMartinMetricsSetCalculator(scope);
            ReadAction.run(() -> robertMartinMetricsSetCalculator.calculate(javaProject));
        }
    };

    private Runnable moodMetricSetCalculating = new Runnable() {
        @Override
        public void run() {
            MoodMetricsSetCalculator moodMetricsSetCalculator = new MoodMetricsSetCalculator(scope);
            ReadAction.run(() -> moodMetricsSetCalculator.calculate(javaProject));
        }
    };

    private Runnable buildTree = new Runnable() {
        @Override
        public void run() {
            ProjectMetricTreeBuilder projectMetricTreeBuilder = new ProjectMetricTreeBuilder(javaProject);
            DefaultTreeModel metricsTreeModel = projectMetricTreeBuilder.createMetricTreeModel();
            MetricsUtils.getProjectMetricsPanel().showResults(metricsTreeModel);
        }
    };

    public ProjectMetricsRunner(Project project, AnalysisScope scope, JavaProject javaProject ) {
        this.project = project;
        this.scope = scope;
        this.javaProject = javaProject;
        dependenciesBuilder = new DependenciesBuilder();
        queue = new BackgroundTaskQueue(project, "Calculating Metrics");
    }

    public static DependenciesBuilder getDependenciesBuilder() {
        return dependenciesBuilder;
    }

    public final void execute() {
        MetricsBackgroundableTask classMetricsTask = new MetricsBackgroundableTask(project,
                "Calculating Metrics...", true, calculate, null,
                () -> queue.clear(), null);

        if (!MetricsService.isNeedToConsiderProjectMetrics() && !MetricsService.isNeedToConsiderPackageMetrics()) {
            classMetricsTask.setOnFinished(buildTree);
            queue.run(classMetricsTask);
            return;
        }
        if (!MetricsService.isNeedToConsiderProjectMetrics()) {
            MetricsBackgroundableTask packageMetricsTask = new MetricsBackgroundableTask(project,
                    "Package Level Metrics: Robert C. Martin Metrics Set Calculating...",
                    true, martinMetricSetCalculating, null,
                    () -> queue.clear(), buildTree);
            queue.run(classMetricsTask);
            queue.run(packageMetricsTask);
            return;
        }
        if (!MetricsService.isNeedToConsiderPackageMetrics()) {
            MetricsBackgroundableTask projectMetricsTask = new MetricsBackgroundableTask(project,
                    "Project Level Metrics: MOOD Metrics Set Calculating...",
                    true, moodMetricSetCalculating, null,
                    () -> queue.clear(), buildTree);
            queue.run(classMetricsTask);
            queue.run(projectMetricsTask);
            return;
        }
        queue.run(classMetricsTask);
        MetricsBackgroundableTask packageMetricsTask = new MetricsBackgroundableTask(project,
                "Package Level Metrics: Robert C. Martin Metrics Set Calculating...",
                true, martinMetricSetCalculating, null,
                () -> queue.clear(), null);
        queue.run(packageMetricsTask);
        MetricsBackgroundableTask projectMetricsTask = new MetricsBackgroundableTask(project,
                "Project Level Metrics: MOOD Metrics Set Calculating...",
                true, moodMetricSetCalculating, null,
                () -> queue.clear(), buildTree);
        queue.run(projectMetricsTask);
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
                indicator.setText("Processing " + fileName + "...");
                progress++;
                PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
                projectModelBuilder.addJavaFileToJavaProject(javaProject, psiJavaFile);
                dependenciesBuilder.build(psiJavaFile);
                indicator.setIndeterminate(false);
                indicator.setFraction((double) progress / (double) numFiles);
            }
        }
}
