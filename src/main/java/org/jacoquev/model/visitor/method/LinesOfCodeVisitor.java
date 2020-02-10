package org.jacoquev.model.visitor.method;

import com.intellij.psi.PsiMethod;
import org.jacoquev.model.metric.util.CommonUtils;
import org.jacoquev.model.metric.util.MethodUtils;
import org.jacoquev.model.metric.value.Value;

public class LinesOfCodeVisitor extends JavaMethodVisitor {
    @Override
    public void visitMethod(PsiMethod method) {
        int methodNestingDepth = 0;
        long elementCount = 0;
        long linesOfCode = 0;
        metric.setName("LOC");
        metric.setDescription("Lines Of Code");
        metric.setDescriptionUrl("/html/LinesOfCode.html");
        if (!MethodUtils.isAbstract(method)) {
            super.visitMethod(method);
            if (methodNestingDepth == 0) {
                elementCount = 0;
            }
            methodNestingDepth++;
            elementCount = CommonUtils.countLines(method);
            super.visitMethod(method);
            methodNestingDepth--;
        }
        if (methodNestingDepth == 0) {
            linesOfCode = elementCount;
        }
        metric.setValue(Value.of(linesOfCode));
    }
}