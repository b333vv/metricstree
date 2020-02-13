package org.jacoquev.model.visitor.method;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiMethod;
import org.jacoquev.model.code.JavaMethod;
import org.jacoquev.model.metric.Metric;

public abstract class JavaMethodVisitor extends JavaRecursiveElementVisitor {

    protected Metric metric;

    public void visitJavaMethod(JavaMethod javaMethod) {
        PsiMethod psiMethod = javaMethod.getPsiMethod();
        visitMethod(psiMethod);
        javaMethod.addMetric(metric);
    }
}
