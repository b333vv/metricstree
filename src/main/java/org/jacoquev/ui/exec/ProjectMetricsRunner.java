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
import org.jacoquev.model.builder.ProjectModelBuilder;
import org.jacoquev.model.code.JavaProject;
import org.jacoquev.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;

public class ProjectMetricsRunner {

    private final Project project;
    private final AnalysisScope scope;

    public ProjectMetricsRunner(Project project, AnalysisScope scope) {
        this.project = project;
        this.scope = scope;
    }

    public final void execute(JavaProject javaProject) {
        final Task.Backgroundable task = new Task.Backgroundable(project, "Calculating Metrics", true) {

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

                indicator.setIndeterminate(false);
                indicator.setFraction((double) progress / (double) numFiles);
            }
        });
        indicator.setIndeterminate(false);
        indicator.setText("Build project metrics tree");
    }

    public void onFinish() {
        MetricsUtils.getProjectMetricsPanel().buildTreeModel();
    }

    public void onCancel() {}
}
