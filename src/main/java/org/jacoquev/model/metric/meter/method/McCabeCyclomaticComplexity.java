package org.jacoquev.model.metric.meter.method;

import com.google.common.collect.ImmutableSet;
import com.intellij.psi.PsiMethod;
import org.jacoquev.model.code.JavaMethod;
import org.jacoquev.model.metric.Meter;
import org.jacoquev.model.metric.Metric;
import org.jacoquev.model.visitor.method.MethodComplexityVisitor;
import org.jacoquev.util.MetricsUtils;

import java.util.Set;

public class McCabeCyclomaticComplexity implements Meter<JavaMethod> {
    @Override
    public Set<Metric> meter(JavaMethod javaMethod) {
        PsiMethod psiMethod = javaMethod.getPsiMethod();
        long mcCabeCyclomaticComplexity = MetricsUtils.callInReadAction(() -> getMcCabeCyclomaticComplexity(psiMethod));
        return ImmutableSet.of(
                Metric.of("CC", "McCabe Cyclomatic Complexity",
                        "/html/McCabeCyclomaticComplexity.html", mcCabeCyclomaticComplexity)
        );
    }

    private long getMcCabeCyclomaticComplexity(PsiMethod psiMethod) {
        MethodComplexityVisitor visitor = new MethodComplexityVisitor();
        psiMethod.accept(visitor);
        return visitor.getMethodComplexity();
    }
}
