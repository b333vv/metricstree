package org.jacoquev.model.visitor.method;

import com.intellij.psi.*;
import org.jacoquev.model.metric.util.MethodUtils;

public class NumberOfLoopsVisitor extends JavaRecursiveElementVisitor {
    private long methodNestingDepth = 0;
    private long elementCount = 0;
    private long result = 0;

    @Override
    public void visitMethod(PsiMethod method) {
        if (methodNestingDepth == 0) {
            elementCount = 0;
        }
        methodNestingDepth++;
        super.visitMethod(method);
        methodNestingDepth--;
        if (methodNestingDepth == 0 && !MethodUtils.isAbstract(method)) {
            result = elementCount;
        }
    }

    @Override
    public void visitForStatement(PsiForStatement statement) {
        super.visitForStatement(statement);
        elementCount++;
    }

    @Override
    public void visitForeachStatement(PsiForeachStatement statement) {
        super.visitForeachStatement(statement);
        elementCount++;
    }

    @Override
    public void visitDoWhileStatement(PsiDoWhileStatement statement) {
        super.visitDoWhileStatement(statement);
        elementCount++;
    }

    @Override
    public void visitWhileStatement(PsiWhileStatement statement) {
        super.visitWhileStatement(statement);
        elementCount++;
    }

    public long getResult() {
        return result;
    }
}
