package org.jacoquev.model.visitor.method;

import com.intellij.psi.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Query;
import org.jacoquev.model.metric.Metric;
import org.jacoquev.model.metric.value.Value;

public class FanOutVisitor extends JavaMethodVisitor {
    private PsiMethod currentMethod;
    private int methodNestingDepth = 0;
    private int result = 0;

    @Override
    public void visitMethod(PsiMethod method) {
        metric = Metric.of("FANOUT", "Fan-Out",
                "/html/FanOut.html", Value.UNDEFINED);
        if (methodNestingDepth == 0) {
            result = 0;
            currentMethod = method;
        }

        methodNestingDepth++;
        super.visitMethod(method);
        methodNestingDepth--;

        if (methodNestingDepth == 0) {
            metric = Metric.of("FANOUT", "Fan-Out",
                    "/html/FanOut.html", result);
        }
    }

    @Override
    public void visitLambdaExpression(PsiLambdaExpression expression) {
    }

    @Override
    public void visitCallExpression(PsiCallExpression callExpression) {
        super.visitCallExpression(callExpression);
        final PsiMethod method = callExpression.resolveMethod();
        if (method == null || method.getContainingClass() == null || method.equals(currentMethod)) {
            return;
        }
        result++;
    }
}
