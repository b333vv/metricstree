package org.jacoquev.model.metric.meter.method;

import com.google.common.collect.ImmutableSet;
import com.intellij.psi.PsiMethod;
import org.jacoquev.model.code.JavaMethod;
import org.jacoquev.model.metric.Meter;
import org.jacoquev.model.metric.Metric;
import org.jacoquev.model.visitor.method.LinesOfCodeVisitor;
import org.jacoquev.util.MetricsUtils;

import java.util.Set;

public class LinesOfCode implements Meter<JavaMethod> {
    @Override
    public Set<Metric> meter(JavaMethod javaMethod) {
        PsiMethod psiMethod = javaMethod.getPsiMethod();
        long linesOfCode = MetricsUtils.callInReadAction(() -> getLinesOfCode(psiMethod));
        return ImmutableSet.of(
                Metric.of("LOC", "Lines Of Code",
                        "/html/LinesOfCode.html", linesOfCode)
        );
    }

    private long getLinesOfCode(PsiMethod psiMethod) {
        LinesOfCodeVisitor visitor = new LinesOfCodeVisitor();
        psiMethod.accept(visitor);
        return visitor.getResult();
    }
}
