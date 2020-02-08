package org.jacoquev.ui.exec;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiCompiledElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import org.jacoquev.model.code.JavaProject;
import org.jacoquev.model.builder.ProjectModelBuilder;
import org.jacoquev.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;

public class ProjectMetricsRunner
//        implements UserDataHolder
        {

    private final Project project;
    private final AnalysisScope scope;

    public ProjectMetricsRunner(Project project, AnalysisScope scope) {
        this.project = project;
        this.scope = scope;
    }

    public final void execute(JavaProject javaProject) {
        final Task.Backgroundable task = new Task.Backgroundable(project, "Calculating metrics", true) {

            @Override
            public void run(@NotNull final ProgressIndicator indicator) {
                calculate(javaProject);
            }

            @Override
            public void onSuccess() {
                onFinish();
            }

            @Override
            public void onCancel() {
                ProjectMetricsRunner.this.onCancel();
            }
        };
        task.queue();
    }

    public void calculate(JavaProject javaProject) {
        ProjectModelBuilder projectModelBuilder = new ProjectModelBuilder();
        final ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
        indicator.setText("Initializing");
        final int numFiles = scope.getFileCount();
//        final List<MetricCalculator> calculators = new ArrayList<MetricCalculator>(numMetrics);
//        for (final MetricInstance metricInstance : metrics) {
//            indicator.checkCanceled();
//            if (!metricInstance.isEnabled()) {
//                continue;
//            }
//            final Metric metric = metricInstance.getMetric();
//            final MetricCalculator calculator = metric.createCalculator();
//
//            calculators.add(calculator);
//            calculator.beginMetricsRun(metric, resultsHolder, this);
//        }

        scope.accept(new PsiElementVisitor() {
            private int progress = 0;

            @Override
            public void visitFile(PsiFile psiFile) {
                indicator.checkCanceled();
                if (!psiFile.getFileType().getName().equals("JAVA")) {
                    return;
                }
                super.visitFile(psiFile);
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
                indicator.setText("Handling psiFile " + fileName);
                progress++;
                PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
                projectModelBuilder.addJavaFileToJavaProject(javaProject, psiJavaFile);
//                for (MetricVisitor visitor : visitors) {
//                    visitor.visit(psiFile);
//                }
                indicator.setIndeterminate(false);
                indicator.setFraction((double) progress / (double) numFiles);
            }
        });
        indicator.setIndeterminate(false);
        indicator.setText("Build project metrics tree");
//        for (MetricCalculator calculator : calculators) {
//            indicator.checkCanceled();
//            calculator.endMetricsRun();
//        }
    }

    public void onFinish() {
        MetricsUtils.getProjectMetricsPanel().buildTreeModel();
    }

    public void onCancel() {}

//    private Map userData = new HashMap();
//
//    @Override
//    public final <T> T getUserData(@NotNull Key<T> key) {
//        return (T) userData.get(key);
//    }
//
//    @Override
//    public final <T> void putUserData(@NotNull Key<T> key, T t) {
//        userData.put(key, t);
//    }
}
