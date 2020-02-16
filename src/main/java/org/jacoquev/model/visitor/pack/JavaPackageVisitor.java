package org.jacoquev.model.visitor.pack;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiPackage;
import org.jacoquev.model.code.JavaClass;
import org.jacoquev.model.code.JavaPackage;
import org.jacoquev.model.metric.Metric;

public abstract class JavaPackageVisitor extends JavaRecursiveElementVisitor {

    protected Metric metric;

    public void visitJavaPackage(JavaPackage javaPackage) {
        PsiPackage psiPackage = javaPackage.getPsiPackage();
        visitPackage(psiPackage);
        javaPackage.addMetric(metric);
    }
}
