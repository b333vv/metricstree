package org.jacoquev.model.metric.meter.method;

import com.google.common.collect.ImmutableSet;
import com.intellij.psi.PsiMethod;
import org.jacoquev.model.code.JavaMethod;
import org.jacoquev.model.metric.Meter;
import org.jacoquev.model.metric.Metric;
import org.jacoquev.model.visitor.method.NumberOfConditionsVisitor;
import org.jacoquev.model.visitor.method.NumberOfLoopsVisitor;
import org.jacoquev.util.MetricsUtils;

import java.util.Set;

public class NumberOfLoops implements Meter<JavaMethod> {
    @Override
    public Set<Metric> meter(JavaMethod javaMethod) {
        PsiMethod psiMethod = javaMethod.getPsiMethod();
        long numberOfLoops = MetricsUtils.callInReadAction(() -> getLinesOfCode(psiMethod));
        return ImmutableSet.of(
                Metric.of("NOLps", "Number Of Loops",
                        "/html/NumberOfLoops.html", numberOfLoops)
        );
    }

    private long getLinesOfCode(PsiMethod psiMethod) {
        NumberOfLoopsVisitor visitor = new NumberOfLoopsVisitor();
        psiMethod.accept(visitor);
        return visitor.getResult();
    }
}