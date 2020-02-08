package org.jacoquev.model.builder;

import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiPackage;
import org.apache.commons.io.FilenameUtils;
import org.jacoquev.model.code.JavaPackage;
import org.jacoquev.model.code.JavaProject;

public class ClassModelBuilder extends ModelBuilder {

    public JavaProject buildJavaProject(PsiJavaFile psiJavaFile) {
        JavaProject javaProject = new JavaProject(FilenameUtils.getBaseName(psiJavaFile.getName()));
        PsiDirectory directory = psiJavaFile.getContainingDirectory();
        PsiPackage psiPackage = JavaDirectoryService.getInstance().getPackage(directory);
        JavaPackage javaPackage = new JavaPackage(psiPackage.getName(), psiPackage);
        javaProject.addPackage(javaPackage);
        createJavaClass(javaPackage, psiJavaFile);
        return javaProject;
    }
}