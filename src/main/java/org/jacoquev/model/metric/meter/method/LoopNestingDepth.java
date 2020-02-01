package org.jacoquev.model.metric.meter.method;

import com.google.common.collect.ImmutableSet;
import com.intellij.psi.PsiMethod;
import org.jacoquev.model.code.JavaMethod;
import org.jacoquev.model.metric.Meter;
import org.jacoquev.model.metric.Metric;
import org.jacoquev.model.visitor.method.LoopNestingDepthVisitor;
import org.jacoquev.util.MetricsUtils;

import java.util.Set;

public class LoopNestingDepth implements Meter<JavaMethod> {
    @Override
    public Set<Metric> meter(JavaMethod javaMethod) {
        PsiMethod psiMethod = javaMethod.getPsiMethod();
        long loopNestingDepth = MetricsUtils.callInReadAction(() -> getLinesOfCode(psiMethod));
        return ImmutableSet.of(
                Metric.of("LND", "Loop Nesting Depth",
                        "/html/LoopNestingDepth.html", loopNestingDepth)
        );
    }

    private long getLinesOfCode(PsiMethod psiMethod) {
        LoopNestingDepthVisitor visitor = new LoopNestingDepthVisitor();
        psiMethod.accept(visitor);
        return visitor.getResult();
    }
}