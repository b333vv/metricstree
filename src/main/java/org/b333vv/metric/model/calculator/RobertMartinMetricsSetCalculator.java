package org.b333vv.metric.model.calculator;

import com.intellij.analysis.AnalysisScope;
import com.intellij.psi.*;
import org.b333vv.metric.exec.ProjectMetricsRunner;
import org.b333vv.metric.model.builder.DependenciesBuilder;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.util.BucketedCount;
import org.b333vv.metric.model.metric.util.ClassUtils;

import java.util.Set;

public class RobertMartinMetricsSetCalculator {
    private final AnalysisScope scope;

    private final BucketedCount<PsiPackage> externalDependenciesPerPackageNumber = new BucketedCount<>();
    private final BucketedCount<PsiPackage> externalDependentsPerPackageNumber = new BucketedCount<>();

    private final BucketedCount<PsiPackage> abstractClassesPerPackageNumber = new BucketedCount<>();
    private final BucketedCount<PsiPackage> classesPerPackageNumber = new BucketedCount<>();

    public RobertMartinMetricsSetCalculator(AnalysisScope scope) {
        this.scope = scope;
    }

    public void calculate(JavaProject javaProject) {

        scope.accept(new Visitor());

        javaProject.getAllPackages()
                .forEach(p -> {
                    int afferentCoupling = externalDependentsPerPackageNumber.getBucketValue(p.getPsiPackage());
                    int efferentCoupling = externalDependenciesPerPackageNumber.getBucketValue(p.getPsiPackage());
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

                    int classesNumber = classesPerPackageNumber.getBucketValue(p.getPsiPackage());
                    int abstractClassesNumber = abstractClassesPerPackageNumber.getBucketValue(p.getPsiPackage());
                    double abstractness = classesNumber == 0 ? 1.0 :
                            (double) abstractClassesNumber / (double) classesNumber;
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

            DependenciesBuilder dependenciesBuilder = ProjectMetricsRunner.getDependenciesBuilder();

            externalDependenciesPerPackageNumber.createBucket(psiPackage);
            Set<PsiPackage> packageDependencies = dependenciesBuilder.getPackagesDependencies(psiClass);
            externalDependenciesPerPackageNumber.incrementBucketValue(psiPackage, packageDependencies.size());

            externalDependentsPerPackageNumber.createBucket(psiPackage);
            Set<PsiPackage> packageDependents = dependenciesBuilder.getPackagesDependents(psiClass);
            externalDependentsPerPackageNumber.incrementBucketValue(psiPackage, packageDependents.size());

            if (psiClass.isInterface() || psiClass.hasModifierProperty(PsiModifier.ABSTRACT)) {
                abstractClassesPerPackageNumber.incrementBucketValue(psiPackage);
            }
            classesPerPackageNumber.incrementBucketValue(psiPackage);
        }
    }
}
