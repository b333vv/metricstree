package org.jacoquev.model.visitor.method;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiMethod;
import org.jacoquev.model.metric.util.CommonUtils;
import org.jacoquev.model.metric.util.MethodUtils;

public class LinesOfCodeVisitor extends JavaRecursiveElementVisitor {
    private long result = 0;
    private int methodNestingDepth = 0;
    private long elementCount = 0;
    @Override
    public void visitMethod(PsiMethod method) {
        if (methodNestingDepth == 0) {
            elementCount = 0;
        }
        methodNestingDepth++;
        elementCount = CommonUtils.countLines(method);
        super.visitMethod(method);
        methodNestingDepth--;
        if (methodNestingDepth == 0 && !MethodUtils.isAbstract(method)) {
            result = elementCount;
        }
    }

    public long getResult() {
        return result;
    }
}