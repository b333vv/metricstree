package org.jacoquev.model.visitor.method;

import com.intellij.psi.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Query;
import org.jacoquev.model.metric.Metric;
import org.jacoquev.model.metric.value.Value;

public class FanInVisitor extends JavaMethodVisitor {
    private PsiMethod currentMethod;
    private int methodNestingDepth = 0;
    private int result = 0;

    @Override
    public void visitMethod(PsiMethod method) {
        metric = Metric.of("FANIN", "Fan-In",
                "/html/FanIn.html", Value.UNDEFINED);
        if (methodNestingDepth == 0) {
            result = 0;
            currentMethod = method;
            final Query<PsiReference> references = ReferencesSearch.search(method);
            for (PsiReference reference : references) {
                PsiElement element = reference.getElement();
                if (element.getParent() instanceof PsiCallExpression) {
                    result++;
                }
            }
        }

        methodNestingDepth++;
        super.visitMethod(method);
        methodNestingDepth--;

        if (methodNestingDepth == 0) {
            metric = Metric.of("FANIN", "Fan-In",
                    "/html/FanIn.html", result);
        }
    }

    @Override
    public void visitMethodCallExpression(PsiMethodCallExpression expression) {
        PsiMethod method = expression.resolveMethod();
        if (currentMethod != null && currentMethod.equals(method)) {
            result--;
        }
        super.visitMethodCallExpression(expression);
    }
}
