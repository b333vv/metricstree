package org.jacoquev.model.visitor.method;

import com.intellij.psi.PsiMethod;
import org.jacoquev.model.metric.Metric;
import org.jacoquev.model.visitor.util.MethodComplexityVisitor;

public class McCabeCyclomaticComplexityVisitor extends JavaMethodVisitor {

    @Override
    public void visitMethod(PsiMethod psiMethod) {
        MethodComplexityVisitor visitor = new MethodComplexityVisitor();
        psiMethod.accept(visitor);
        metric = Metric.of("CC", "McCabe Cyclomatic Complexity",
                        "/html/McCabeCyclomaticComplexity.html", visitor.getMethodComplexity());
    }
}
