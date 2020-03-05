package org.b333vv.metric.model.visitor.method;

import com.intellij.psi.PsiMethod;
import org.b333vv.metric.model.metric.Metric;

public class McCabeCyclomaticComplexityVisitor extends JavaMethodVisitor {

    @Override
    public void visitMethod(PsiMethod psiMethod) {
        MethodComplexityVisitor visitor = new MethodComplexityVisitor();
        psiMethod.accept(visitor);
        metric = Metric.of("CC", "McCabe Cyclomatic Complexity",
                "/html/McCabeCyclomaticComplexity.html", visitor.getMethodComplexity());
    }
}
