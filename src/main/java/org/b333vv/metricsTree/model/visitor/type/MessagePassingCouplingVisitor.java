package org.b333vv.metricsTree.model.visitor.type;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethodCallExpression;
import org.b333vv.metricsTree.model.metric.util.ClassUtils;
import org.b333vv.metricsTree.model.metric.value.Value;
import org.b333vv.metricsTree.model.metric.Metric;

public class MessagePassingCouplingVisitor extends JavaClassVisitor {
    private int numCalls = 0;
    @Override
    public void visitClass(PsiClass psiClass) {
        metric = Metric.of("MPC", "Message Passing Coupling",
                "/html/MessagePassingCoupling.html", Value.UNDEFINED);
        if (ClassUtils.isConcrete(psiClass) && !ClassUtils.isAnonymous(psiClass)) {
            numCalls = 0;
        }
        super.visitClass(psiClass);
        if (ClassUtils.isConcrete(psiClass) && !ClassUtils.isAnonymous(psiClass)) {
            metric = Metric.of("MPC", "Message Passing Coupling",
                    "/html/MessagePassingCoupling.html", numCalls);
        }
    }

    @Override
    public void visitMethodCallExpression(PsiMethodCallExpression expression) {
        super.visitMethodCallExpression(expression);
        numCalls++;
    }
}