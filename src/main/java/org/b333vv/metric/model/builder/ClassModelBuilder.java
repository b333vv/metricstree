package org.b333vv.metric.model.builder;

import com.intellij.psi.PsiJavaFile;
import org.apache.commons.io.FilenameUtils;
import org.b333vv.metric.model.code.JavaPackage;
import org.b333vv.metric.model.code.JavaProject;

public class ClassModelBuilder extends ModelBuilder {

    public JavaProject buildJavaProject(PsiJavaFile psiJavaFile) {
        JavaProject javaProject = new JavaProject(FilenameUtils.getBaseName(psiJavaFile.getName()));
        JavaPackage javaPackage = new JavaPackage(psiJavaFile.getPackageName(), null);
        javaProject.addPackage(javaPackage);
        createJavaClass(javaPackage, psiJavaFile);
        return javaProject;
    }
}