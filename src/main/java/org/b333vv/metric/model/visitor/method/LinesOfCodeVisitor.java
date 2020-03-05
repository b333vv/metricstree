package org.b333vv.metric.model.visitor.method;

import com.intellij.psi.PsiMethod;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.util.CommonUtils;
import org.b333vv.metric.model.metric.util.MethodUtils;

public class LinesOfCodeVisitor extends JavaMethodVisitor {
    private int methodNestingDepth = 0;
    private long elementCount = 0;
    @Override
    public void visitMethod(PsiMethod method) {
        long linesOfCode = 0;
        super.visitMethod(method);
        if (methodNestingDepth == 0) {
            elementCount = 0;
        }
        methodNestingDepth++;
        elementCount = CommonUtils.countLines(method);
        super.visitMethod(method);
        methodNestingDepth--;
        if (methodNestingDepth == 0 && !MethodUtils.isAbstract(method)) {
            linesOfCode = elementCount;
        }
        metric = Metric.of("LOC", "Lines Of Code",
                "/html/LinesOfCode.html", linesOfCode);
    }
}