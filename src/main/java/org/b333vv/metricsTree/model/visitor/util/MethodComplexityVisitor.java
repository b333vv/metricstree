package org.b333vv.metricsTree.model.visitor.util;

import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;

public class MethodComplexityVisitor extends JavaRecursiveElementWalkingVisitor {

    private long methodComplexity = 1;

    @Override
    public void visitForStatement(PsiForStatement statement) {
        super.visitForStatement(statement);
        methodComplexity++;
    }

    @Override
    public void visitForeachStatement(PsiForeachStatement statement) {
        super.visitForeachStatement(statement);
        methodComplexity++;
    }

    @Override
    public void visitIfStatement(PsiIfStatement statement) {
        super.visitIfStatement(statement);
        methodComplexity++;
    }

    @Override
    public void visitDoWhileStatement(PsiDoWhileStatement statement) {
        super.visitDoWhileStatement(statement);
        methodComplexity++;
    }

    @Override
    public void visitConditionalExpression(PsiConditionalExpression expression) {
        super.visitConditionalExpression(expression);
        methodComplexity++;
    }

    @Override
    public void visitSwitchStatement(PsiSwitchStatement statement) {
        super.visitSwitchStatement(statement);
        final PsiCodeBlock body = statement.getBody();
        if (body == null) {
            return;
        }
        final PsiStatement[] statements = body.getStatements();
        boolean pendingLabel = false;
        for (final PsiStatement child : statements) {
            if (child instanceof PsiSwitchLabelStatement) {
                if (!pendingLabel) {
                    methodComplexity++;
                }
                pendingLabel = true;
            } else {
                pendingLabel = false;
            }
        }
    }

    @Override
    public void visitWhileStatement(PsiWhileStatement statement) {
        super.visitWhileStatement(statement);
        methodComplexity++;
    }

    @Override
    public void visitCatchSection(PsiCatchSection section) {
        super.visitCatchSection(section);
        methodComplexity++;
    }

    @Override
    public void visitPolyadicExpression(PsiPolyadicExpression expression) {
        super.visitPolyadicExpression(expression);
        final IElementType token = expression.getOperationTokenType();
        if (token.equals(JavaTokenType.ANDAND) || token.equals(JavaTokenType.OROR)) {
            methodComplexity += expression.getOperands().length - 1;
        }
    }

    public long getMethodComplexity() {
        return methodComplexity;
    }
}
