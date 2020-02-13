package org.jacoquev.exec;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.progress.PerformInBackgroundOption;
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
import org.jacoquev.model.builder.ProjectModelBuilder;
import org.jacoquev.model.code.DependencyMap;
import org.jacoquev.model.code.JavaProject;
import org.jacoquev.util.MetricsUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ProjectMetricsRunner {

    private final Project project;
    private final AnalysisScope scope;
    private static DependencyMap dependencyMap;
    private JavaProject javaProject;
    private ProjectModelBuilder projectModelBuilder;
    private ProgressIndicator indicator;
    private int numFiles;
    private int progress = 0;
    ProjectMetricsCalculateTask task;

    public ProjectMetricsRunner(Project project, AnalysisScope scope, JavaProject javaProject ) {
        this.project = project;
        this.scope = scope;
        this.javaProject = javaProject;
        dependencyMap = new DependencyMap();
        task = new ProjectMetricsCalculateTask(project,
                "Calculating Metrics", true);
    }

    public static DependencyMap getDependencyMap() {
        return dependencyMap;
    }

    public final void execute() {
        task.queue();
    }

    public void calculate() {
        projectModelBuilder = new ProjectModelBuilder(javaProject);
        indicator = ProgressManager.getInstance().getProgressIndicator();
        indicator.setText("Initializing");
        numFiles = scope.getFileCount();
        scope.accept(new PsiJavaFileVisitor());
        indicator.setText("Calculating metrics");
        MetricsUtils.runInReadAction(() -> projectModelBuilder.calculateMetrics());
        indicator.setText("Build project metrics tree");
    }

    public void onFinish() {
        MetricsUtils.getProjectMetricsPanel().buildTreeModel();
    }

    public void onCancel() {}

    class ProjectMetricsCalculateTask extends Task.Backgroundable {

        public ProjectMetricsCalculateTask(@Nullable Project project,
                                           @Nls(capitalization = Nls.Capitalization.Title) @NotNull String title,
                                           boolean canBeCancelled) {
            super(project, title, canBeCancelled);
        }

        @Override
        public void run(@NotNull ProgressIndicator indicator) {
            calculate();
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
