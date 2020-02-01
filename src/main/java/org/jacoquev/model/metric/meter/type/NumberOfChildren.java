package org.jacoquev.model.metric.meter.type;

import com.google.common.collect.ImmutableSet;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import org.jacoquev.model.code.JavaClass;
import org.jacoquev.model.metric.Meter;
import org.jacoquev.model.metric.Metric;
import org.jacoquev.util.MetricsUtils;

import java.util.Set;

public class NumberOfChildren implements Meter<JavaClass> {
    @Override
    public Set<Metric> meter(JavaClass javaClass) {
        PsiClass psiClass = javaClass.getPsiClass();
        long numberOfChildren = MetricsUtils.callInReadAction(() -> {
            long result = 0;
            if (!(psiClass.hasModifierProperty(PsiModifier.FINAL) ||
                    psiClass.isInterface() ||
                    psiClass.isEnum()
            )) {
                result = ClassInheritorsSearch.search(psiClass, false).findAll().size();
            }
            return result;
        });
        return ImmutableSet.of(
                Metric.of("NOC", "Number Of Children",
                        "/html/NumberOfChildren.html", numberOfChildren)
        );
    }
}
