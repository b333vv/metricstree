package org.jacoquev.model.visitor.pack;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiPackage;
import org.jacoquev.model.code.DependencyMap;
import org.jacoquev.model.code.JavaProject;
import org.jacoquev.model.metric.Metric;
import org.jacoquev.model.metric.util.BucketedCount;
import org.jacoquev.model.metric.util.ClassUtils;
import org.jacoquev.model.metric.value.Value;
import org.jacoquev.util.MetricsUtils;

import java.util.HashSet;
import java.util.Set;

import static org.jacoquev.exec.ProjectMetricsRunner.getDependencyMap;

public class PackageCouplingCalculator {
    private BucketedCount<PsiPackage> numExternalDependenciesPerPackage = new BucketedCount<>();
    private BucketedCount<PsiPackage> numExternalDependentsPerPackage = new BucketedCount<>();

    public void calculate(JavaProject javaProject) {

        javaProject.getAllClasses()
                .forEach(c -> c.getPsiClass().accept(new Visitor()));

        javaProject.getAllPackages()
                .forEach(p -> {
                    int afferentCoupling = numExternalDependentsPerPackage.getBucketValue(p.getPsiPackage());
                    int efferentCoupling = numExternalDependenciesPerPackage.getBucketValue(p.getPsiPackage());
                    double instability = (afferentCoupling + efferentCoupling) == 0 ? 0.0 :
                            (double) efferentCoupling / ((double) afferentCoupling + (double) efferentCoupling);
                    p.addMetric(Metric.of(
                            "Ce",
                            "Efferent Coupling",
                            "/html/EfferentCoupling.html",
                            efferentCoupling));
                    p.addMetric(Metric.of(
                            "Ca",
                            "Afferent Coupling",
                            "/html/AfferentCoupling.html",
                            afferentCoupling));
                    p.addMetric(Metric.of(
                            "I",
                            "Instability",
                            "/html/Instability.html",
                            instability));
                });
    }

    private class Visitor extends JavaRecursiveElementVisitor {

        @Override
        public void visitClass(PsiClass psiClass) {
            super.visitClass(psiClass);
            if (ClassUtils.isAnonymous(psiClass)) {
                return;
            }
            PsiPackage psiPackage = ClassUtils.findPackage(psiClass);
            if (psiPackage == null) {
                return;
            }
            DependencyMap dependencyMap = getDependencyMap();

            numExternalDependenciesPerPackage.createBucket(psiPackage);
            Set<PsiPackage> packageDependencies = dependencyMap.calculatePackageDependencies(psiClass);
            numExternalDependenciesPerPackage.incrementBucketValue(psiPackage, packageDependencies.size());

            numExternalDependentsPerPackage.createBucket(psiPackage);
            Set<PsiPackage> packageDependents = dependencyMap.calculatePackageDependents(psiClass);
            numExternalDependentsPerPackage.incrementBucketValue(psiPackage, packageDependents.size());
        }
    }
}
