package org.jacoquev.model.code;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import org.apache.commons.io.FilenameUtils;

public class ModelBuilder {
    private final Project project;

    public ModelBuilder(Project project) {
        this.project = project;
    }

    public void buildPsiClasses(VirtualFile virtualFile, JavaPackage javaPackage) {
        PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
        PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
        final PsiClass[] classes = psiJavaFile.getClasses();
        for (int i = 0; i < classes.length; i++) {
            JavaClass javaClass = new JavaClass(classes[i]);
            javaPackage.addType(javaClass);
            buildConstructors(javaClass);
            buildMethods(javaClass);
            buildInnerClasses(classes[i], javaPackage);
        }
    }

    private void buildMethods(JavaClass javaClass) {
        for (PsiMethod aMethod : javaClass.getPsiClass().getMethods()) {
            JavaMethod javaMethod = new JavaMethod(aMethod);
            javaClass.addMethod(javaMethod);
        }
    }

    private void buildConstructors(JavaClass javaClass) {
        for (PsiMethod aConstructor : javaClass.getPsiClass().getConstructors()) {
            JavaMethod javaMethod = new JavaMethod(aConstructor);
            javaClass.addMethod(javaMethod);
        }
    }

    private void buildInnerClasses(PsiClass aClass, JavaPackage javaPackage) {
        final PsiClass[] classes = aClass.getInnerClasses();
        for (int i = 0; i < classes.length; i++) {
            JavaClass javaClass = new JavaClass(classes[i]);
            javaPackage.addType(javaClass);
            buildConstructors(javaClass);
            buildMethods(javaClass);
            buildInnerClasses(classes[i], javaPackage);
        }
    }

    public JavaProject buildJavaProject(VirtualFile virtualFile) {
        JavaProject javaProject = new JavaProject(FilenameUtils.getBaseName(virtualFile.getCanonicalPath()));
        JavaPackage javaPackage = new JavaPackage(virtualFile.getParent().getCanonicalPath());
        javaProject.addPackage(javaPackage);
        buildPsiClasses(virtualFile, javaPackage);
        return javaProject;
    }
}