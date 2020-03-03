package org.b333vv.metric.model.visitor.method;

import com.intellij.psi.PsiMethod;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.util.CommonUtils;
import org.b333vv.metric.model.metric.util.MethodUtils;

public class LinesOfCodeVisitor extends JavaMethodVisitor {
    @Override
    public void visitMethod(PsiMethod method) {
        int methodNestingDepth = 0;
        long elementCount = 0;
        long linesOfCode = 0;
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
        metric = Metric.of("LOC", "Lines Of Code",
                "/html/LinesOfCode.html", linesOfCode);
    }
}