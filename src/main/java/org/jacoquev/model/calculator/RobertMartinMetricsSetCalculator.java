package org.jacoquev.model.calculator;

import com.intellij.analysis.AnalysisScope;
import com.intellij.psi.*;
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

public class RobertMartinMetricsSetCalculator {
    private final AnalysisScope scope;

    private BucketedCount<PsiPackage> numExternalDependenciesPerPackage = new BucketedCount<>();
    private BucketedCount<PsiPackage> numExternalDependentsPerPackage = new BucketedCount<>();

    private BucketedCount<PsiPackage> numAbstractClassesPerPackage = new BucketedCount<>();
    private BucketedCount<PsiPackage> numClassesPerPackage = new BucketedCount<>();

    public RobertMartinMetricsSetCalculator(AnalysisScope scope) {
        this.scope = scope;
    }

    public void calculate(JavaProject javaProject) {

        scope.accept(new Visitor());

        javaProject.getAllPackages()
                .forEach(p -> {
                    int afferentCoupling = numExternalDependentsPerPackage.getBucketValue(p.getPsiPackage());
                    int efferentCoupling = numExternalDependenciesPerPackage.getBucketValue(p.getPsiPackage());
                    double instability = (afferentCoupling + efferentCoupling) == 0 ? 1.0 :
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

                    int numClasses = numClassesPerPackage.getBucketValue(p.getPsiPackage());
                    int numAbstractClasses = numAbstractClassesPerPackage.getBucketValue(p.getPsiPackage());
                    double abstractness = numClasses == 0 ? 1.0 :
                            (double) numAbstractClasses / (double) numClasses;
                    p.addMetric(Metric.of(
                            "A",
                            "Abstractness",
                            "/html/Abstractness.html",
                            abstractness));

                    double distance = Math.abs(1.0 - instability - abstractness);
                    p.addMetric(Metric.of(
                            "D",
                            "Normalized Distance From Main Sequence",
                            "/html/NormalizedDistanceFromMainSequence.html",
                            distance));
                });
    }

    private class Visitor extends JavaRecursiveElementVisitor {

        @Override
        public void visitFile(PsiFile psiFile) {
            super.visitFile(psiFile);
        }

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

            if (psiClass.isInterface() || psiClass.hasModifierProperty(PsiModifier.ABSTRACT)) {
                numAbstractClassesPerPackage.incrementBucketValue(psiPackage);
            }
            numClassesPerPackage.incrementBucketValue(psiPackage);
        }
    }
}
