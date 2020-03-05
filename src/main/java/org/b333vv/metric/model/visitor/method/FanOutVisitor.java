package org.b333vv.metric.model.visitor.method;

import com.intellij.psi.PsiCallExpression;
import com.intellij.psi.PsiLambdaExpression;
import com.intellij.psi.PsiMethod;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.value.Value;

public class FanOutVisitor extends JavaMethodVisitor {
    private PsiMethod currentMethod;
    private int methodNestingDepth = 0;
    private int result = 0;

    @Override
    public void visitMethod(PsiMethod method) {
        metric = Metric.of("FOUT", "Fan-Out",
                "/html/FanOut.html", Value.UNDEFINED);
        if (methodNestingDepth == 0) {
            result = 0;
            currentMethod = method;
        }
        methodNestingDepth++;
        super.visitMethod(method);
        methodNestingDepth--;
        if (methodNestingDepth == 0) {
            metric = Metric.of("FOUT", "Fan-Out",
                    "/html/FanOut.html", result);
        }
    }

    @Override
    public void visitLambdaExpression(PsiLambdaExpression expression) {
    }

    @Override
    public void visitCallExpression(PsiCallExpression callExpression) {
        super.visitCallExpression(callExpression);
        PsiMethod method = callExpression.resolveMethod();
        if (method == null || method.getContainingClass() == null || method.equals(currentMethod)) {
            return;
        }
        result++;
    }
}
