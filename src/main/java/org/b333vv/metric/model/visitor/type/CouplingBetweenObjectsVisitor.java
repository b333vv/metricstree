package org.b333vv.metric.model.visitor.type;

import com.intellij.psi.PsiClass;
import org.b333vv.metric.exec.ProjectMetricsRunner;
import org.b333vv.metric.model.builder.DependenciesBuilder;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.util.ClassUtils;
import org.b333vv.metric.model.metric.value.Value;

import java.util.HashSet;
import java.util.Set;

public class CouplingBetweenObjectsVisitor extends JavaClassVisitor {
    @Override
    public void visitClass(PsiClass psiClass) {
        super.visitClass(psiClass);
        metric = Metric.of("CBO", "Coupling Between Objects",
                "/html/CouplingBetweenObjects.html", Value.UNDEFINED);
        if (ClassUtils.isConcrete(psiClass)) {
            DependenciesBuilder dependenciesBuilder = ProjectMetricsRunner.getDependenciesBuilder();
            Set<PsiClass> dependencies = dependenciesBuilder.getClassesDependencies(psiClass);
            Set<PsiClass> dependents = dependenciesBuilder.getClassesDependents(psiClass);
            Set<PsiClass> union = new HashSet<>(dependencies);
            union.addAll(dependents);
            metric = Metric.of("CBO", "Coupling Between Objects",
                    "/html/CouplingBetweenObjects.html", union.size());
        }
    }
}