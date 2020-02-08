package org.jacoquev.model.visitor.method;

import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;
import org.jacoquev.model.metric.Metric;

public class NumberOfConditionsVisitor extends JavaMethodVisitor {
    private long count = 0;
    private long depth = 0;
    private long numberOfConditions = 0;

    @Override
    public void visitExpression(PsiExpression expression) {
        final long oldCount = count;
        super.visitExpression(expression);
        if (expression.getType() != null &&
                PsiType.BOOLEAN.isAssignableFrom(expression.getType()) && oldCount == count) {
            count++;
        }
    }

    @Override
    public void visitMethod(PsiMethod method) {
        if (depth == 0) {
            count = 0;
        }
        depth++;
        super.visitMethod(method);
        depth--;
        if (depth == 0) {
            numberOfConditions = count;
        }
        metric = Metric.of("NOCdt", "Number Of Conditions",
                "/html/NumberOfConditions.html", numberOfConditions);
    }
}