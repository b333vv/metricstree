package org.b333vv.metric.model.visitor.type;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethodCallExpression;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.util.ClassUtils;
import org.b333vv.metric.model.metric.value.Value;

public class MessagePassingCouplingVisitor extends JavaClassVisitor {
    private int methodCallsNumber = 0;
    @Override
    public void visitClass(PsiClass psiClass) {
        metric = Metric.of("MPC", "Message Passing Coupling",
                "/html/MessagePassingCoupling.html", Value.UNDEFINED);
        if (ClassUtils.isConcrete(psiClass) && !ClassUtils.isAnonymous(psiClass)) {
            methodCallsNumber = 0;
        }
        super.visitClass(psiClass);
        if (ClassUtils.isConcrete(psiClass) && !ClassUtils.isAnonymous(psiClass)) {
            metric = Metric.of("MPC", "Message Passing Coupling",
                    "/html/MessagePassingCoupling.html", methodCallsNumber);
        }
    }

    @Override
    public void visitMethodCallExpression(PsiMethodCallExpression expression) {
        super.visitMethodCallExpression(expression);
        methodCallsNumber++;
    }
}