package org.jacoquev.model.visitor.type;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiClass;
import org.jacoquev.model.code.JavaClass;
import org.jacoquev.model.metric.Metric;

public abstract class JavaClassVisitor extends JavaRecursiveElementVisitor {

    protected Metric metric;

    public void visitJavaClass(JavaClass javaClass) {
        PsiClass psiClass = javaClass.getPsiClass();
        visitClass(psiClass);
        javaClass.addMetric(metric);
    }
}
