package org.jacoquev.model.visitor.method;

import com.intellij.psi.*;
import org.jacoquev.model.metric.util.MethodUtils;

public class LoopNestingDepthVisitor extends JavaRecursiveElementVisitor {
    private long result = 0;
    private long methodNestingCount = 0;
    private long maximumDepth = 0;
    private long currentDepth = 0;

    @Override
    public void visitMethod(PsiMethod method) {
        if (methodNestingCount == 0) {
            maximumDepth = 0;
            currentDepth = 0;
        }
        methodNestingCount++;
        super.visitMethod(method);
        methodNestingCount--;
        if (methodNestingCount == 0) {
            if (!MethodUtils.isAbstract(method)) {
                result = maximumDepth;
            }
        }
    }

    @Override
    public void visitDoWhileStatement(PsiDoWhileStatement statement) {
        enterScope();
        super.visitDoWhileStatement(statement);
        exitScope();
    }

    @Override
    public void visitWhileStatement(PsiWhileStatement statement) {
        enterScope();
        super.visitWhileStatement(statement);
        exitScope();
    }

    @Override
    public void visitForStatement(PsiForStatement statement) {
        enterScope();
        super.visitForStatement(statement);
        exitScope();
    }

    @Override
    public void visitForeachStatement(PsiForeachStatement statement) {
        enterScope();
        super.visitForeachStatement(statement);
        exitScope();
    }

    private void enterScope() {
        currentDepth++;
        maximumDepth = Math.max(maximumDepth, currentDepth);
    }

    private void exitScope() {
        currentDepth--;
    }

    public long getResult() {
        return result;
    }
}