package org.jacoquev.model.builder;

import com.intellij.psi.PsiJavaFile;
import org.apache.commons.io.FilenameUtils;
import org.jacoquev.model.code.JavaPackage;
import org.jacoquev.model.code.JavaProject;

public class ClassModelBuilder extends ModelBuilder {

    public JavaProject buildJavaProject(PsiJavaFile psiJavaFile) {
        JavaProject javaProject = new JavaProject(FilenameUtils.getBaseName(psiJavaFile.getName()));
//        PsiDirectory directory = psiJavaFile.getContainingDirectory();
//        PsiPackage psiPackage = JavaDirectoryService.getInstance().getPackage(directory);
//        JavaPackage javaPackage = new JavaPackage(psiPackage.getName(), psiPackage);
        JavaPackage javaPackage = new JavaPackage(psiJavaFile.getPackageName(), null);
        javaProject.addPackage(javaPackage);
        createJavaClass(javaPackage, psiJavaFile);
        return javaProject;
    }
}