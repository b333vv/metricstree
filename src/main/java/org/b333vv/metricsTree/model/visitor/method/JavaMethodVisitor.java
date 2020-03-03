package org.b333vv.metricsTree.model.visitor.method;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiMethod;
import org.b333vv.metricsTree.model.code.JavaMethod;
import org.b333vv.metricsTree.model.metric.Metric;

public abstract class JavaMethodVisitor extends JavaRecursiveElementVisitor {

    protected Metric metric;

    public void visitJavaMethod(JavaMethod javaMethod) {
        PsiMethod psiMethod = javaMethod.getPsiMethod();
        visitMethod(psiMethod);
        javaMethod.addMetric(metric);
    }
}
