package org.b333vv.metricsTree.model.visitor.type;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiClass;
import org.b333vv.metricsTree.model.code.JavaClass;
import org.b333vv.metricsTree.model.metric.Metric;

public abstract class JavaClassVisitor extends JavaRecursiveElementVisitor {

    protected Metric metric;

    public void visitJavaClass(JavaClass javaClass) {
        PsiClass psiClass = javaClass.getPsiClass();
        visitClass(psiClass);
        javaClass.addMetric(metric);
    }
}
