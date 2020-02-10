package org.jacoquev.model.visitor.method;

import com.intellij.psi.PsiIfStatement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiStatement;
import org.jacoquev.model.metric.util.MethodUtils;
import org.jacoquev.model.metric.value.Value;

public class ConditionNestingDepthVisitor extends JavaMethodVisitor {
    private long methodNestingCount = 0;
    private long maximumDepth = 0;
    private long currentDepth = 0;

    @Override
    public void visitMethod(PsiMethod method) {
        metric.setName("CND");
        metric.setDescription("Condition Nesting Depth");
        metric.setDescriptionUrl("/html/ConditionNestingDepth.html");
        long conditionNestingDepth = 0;
        if (methodNestingCount == 0) {
            maximumDepth = 0;
            currentDepth = 0;
        }
        methodNestingCount++;
        super.visitMethod(method);
        methodNestingCount--;
        if (methodNestingCount == 0) {
            if (!MethodUtils.isAbstract(method)) {
                conditionNestingDepth = maximumDepth;
            }
        }
        metric.setValue(Value.of(conditionNestingDepth));
    }

    @Override
    public void visitIfStatement(PsiIfStatement statement) {
        boolean isAlreadyCounted = false;
        if (statement.getParent() instanceof PsiIfStatement) {
            final PsiIfStatement parent = (PsiIfStatement) statement.getParent();
            final PsiStatement elseBranch = parent.getElseBranch();
            if (statement.equals(elseBranch)) {
                isAlreadyCounted = true;
            }
        }
        if (!isAlreadyCounted) {
            enterScope();
        }
        super.visitIfStatement(statement);

        if (!isAlreadyCounted) {
            exitScope();
        }
    }

    private void enterScope() {
        currentDepth++;
        maximumDepth = Math.max(maximumDepth, currentDepth);
    }

    private void exitScope() {
        currentDepth--;
    }
}
