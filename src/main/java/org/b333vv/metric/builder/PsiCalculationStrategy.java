package org.b333vv.metric.builder;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiCompiledElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import org.b333vv.metric.model.code.ProjectElement;

public class PsiCalculationStrategy implements MetricCalculationStrategy {

    private ProgressIndicator indicator;
    private int filesCount;
    private int progress = 0;

    @Override
    public ProjectElement calculate(Project project, ProgressIndicator indicator) {
        this.indicator = indicator;
        AnalysisScope scope = new AnalysisScope(project);
        scope.setIncludeTestSource(false);
        ProjectElement javaProject = new ProjectElement(project.getName());
        ProjectModelBuilder projectModelBuilder = new ProjectModelBuilder(javaProject);

        indicator.setText("Initializing");
        filesCount = scope.getFileCount();
        indicator.setText("Calculating metrics");
        scope.accept(new PsiJavaFileVisitor(projectModelBuilder));
        return javaProject;
    }

    private class PsiJavaFileVisitor extends PsiElementVisitor {
        private final ProjectModelBuilder projectModelBuilder;

        public PsiJavaFileVisitor(ProjectModelBuilder projectModelBuilder) {
            this.projectModelBuilder = projectModelBuilder;
        }

        @Override
        public void visitFile(PsiFile psiFile) {
            super.visitFile(psiFile);
            indicator.checkCanceled();
            if (psiFile instanceof PsiCompiledElement) {
                return;
            }
            final FileType fileType = psiFile.getFileType();
            if (!fileType.getName().equals("JAVA") || fileType.isBinary()) {
                return;
            }
            final VirtualFile virtualFile = psiFile.getVirtualFile();
            final ProjectRootManager rootManager = ProjectRootManager.getInstance(psiFile.getProject());
            final ProjectFileIndex fileIndex = rootManager.getFileIndex();
            if (fileIndex.isExcluded(virtualFile) || !fileIndex.isInContent(virtualFile)) {
                return;
            }
            final String fileName = psiFile.getName();
            indicator.setText("Calculating metrics on class and method levels: processing file " + fileName + "...");
            progress++;
            PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
            projectModelBuilder.addJavaFileToJavaProject(psiJavaFile);
            indicator.setIndeterminate(false);
            indicator.setFraction((double) progress / (double) filesCount);
        }
    }
}