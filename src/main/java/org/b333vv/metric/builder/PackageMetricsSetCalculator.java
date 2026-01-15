/*
 * Copyright 2020 b333vv
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.b333vv.metric.builder;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiPackage;
import org.b333vv.metric.model.code.ClassElement;
import org.b333vv.metric.model.code.CodeElement;
import org.b333vv.metric.model.code.PackageElement;
import org.b333vv.metric.model.code.ProjectElement;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.model.util.BucketedCount;
import org.b333vv.metric.model.util.ClassUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static org.b333vv.metric.model.metric.MetricType.*;

/**
 * Calculates package-level metrics for packages visited in the given {@link AnalysisScope}.<br>
 *
 * <h3>What is considered a package</h3>
 * Metrics are calculated only for packages that contain at least one <b>non-anonymous</b> class visited while
 * traversing the {@link AnalysisScope}. This avoids producing misleading "all zeros" metric sets for packages
 * that are present in {@link ProjectElement#allPackages()} but are outside the scope being analyzed.
 *
 * <h3>Martin metrics</h3>
 * The following Robert C. Martin package metrics are computed:
 * <ul>
 *   <li><b>Ce</b> (efferent coupling): number of unique external packages that the package depends on.</li>
 *   <li><b>Ca</b> (afferent coupling): number of unique external packages that depend on the package.</li>
 *   <li><b>I</b> (instability): {@code Ce / (Ca + Ce)}. When {@code Ca + Ce == 0} treated as {@code 0.0}.</li>
 *   <li><b>A</b> (abstractness): {@code Na / Nc}, where {@code Na} is number of abstract classes + interfaces,
 *       {@code Nc} is total number of classes + interfaces in the package. When {@code Nc == 0} treated as {@code 0.0}.</li>
 *   <li><b>D</b> (distance from main sequence): {@code | 1 - A - I |}.</li>
 * </ul>
 *
 * <p>
 * Couplings ({@code Ce}/{@code Ca}) are computed using <b>unique package sets</b> aggregated across all classes
 * in a package, which prevents overcounting the same dependency multiple times.
 * </p>
 *
 * <h3>Aggregated package statistics</h3>
 * Additionally calculates package-aggregated sums/counters such as:
 * {@link MetricType#PNCSS}, {@link MetricType#PLOC}, counts of class kinds (concrete/abstract/static/interfaces),
 * package-aggregated Halstead metrics (prefixed with {@code PA*}), and package Maintainability Index ({@link MetricType#PAMI}).
 */
public class PackageMetricsSetCalculator {
    private final AnalysisScope scope;
    private final DependenciesBuilder dependenciesBuilder;
    private final ProjectElement projectElement;

    /**
     * For each package P: set of unique external packages that P depends on (Ce basis).
     */
    private final ConcurrentMap<PsiPackage, Set<PsiPackage>> efferentPackages = new ConcurrentHashMap<>();

    /**
     * For each package P: set of unique external packages that depend on P (Ca basis).
     */
    private final ConcurrentMap<PsiPackage, Set<PsiPackage>> afferentPackages = new ConcurrentHashMap<>();

    /**
     * Packages that were actually visited while traversing the analysis scope.
     */
    private final Set<PsiPackage> visitedPackages = ConcurrentHashMap.newKeySet();

    private final BucketedCount<PsiPackage> abstractClassesPerPackageNumber = new BucketedCount<>();
    private final BucketedCount<PsiPackage> classesPerPackageNumber = new BucketedCount<>();

    public PackageMetricsSetCalculator(AnalysisScope scope, DependenciesBuilder dependenciesBuilder, ProjectElement projectElement) {
        this.scope = scope;
        this.dependenciesBuilder = dependenciesBuilder;
        this.projectElement = projectElement;
    }

    public void calculate() {
        scope.accept(new Visitor());
        projectElement.allPackages()
                .filter(p -> {
                    PsiPackage psiPackage = p.getPsiPackage();
                    return psiPackage != null && visitedPackages.contains(psiPackage);
                })
                .forEach(this::handlePackage);
    }

    private void handlePackage(@NotNull PackageElement p) {
        PsiPackage psiPackage = p.getPsiPackage();
        if (psiPackage == null) {
            return;
        }

        int afferentCoupling = afferentPackages.getOrDefault(psiPackage, Collections.emptySet()).size();
        int efferentCoupling = efferentPackages.getOrDefault(psiPackage, Collections.emptySet()).size();

        Value instability = (afferentCoupling + efferentCoupling) == 0
                ? Value.of(0.0)
                : Value.of((double) efferentCoupling)
                .divide(Value.of((double) (afferentCoupling + efferentCoupling)));

        p.addMetric(Metric.of(Ce, efferentCoupling));
        p.addMetric(Metric.of(Ca, afferentCoupling));
        p.addMetric(Metric.of(I, instability));

        int classesNumber = classesPerPackageNumber.getBucketValue(psiPackage);
        int abstractClassesNumber = abstractClassesPerPackageNumber.getBucketValue(psiPackage);

        Value abstractness = classesNumber == 0
                ? Value.of(0.0)
                : Value.of((double) abstractClassesNumber).divide(Value.of((double) classesNumber));

        p.addMetric(Metric.of(A, abstractness));

        Value distance = Value.of(1.0).minus(instability).minus(abstractness).abs();
        p.addMetric(Metric.of(D, distance));

        ApplicationManager.getApplication().runReadAction(() -> addStatisticMetrics(p));
    }

    private void addStatisticMetrics(PackageElement p) {
        List<ClassElement> classes = p.classes().collect(Collectors.toList());
        List<PsiClass> psiClasses = classes.stream()
                .map(ClassElement::getPsiClass)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        long concreteClassesNumber = 0;
        long abstractClassesNumber = 0;
        long staticClassesNumber = 0;
        long interfacesNumber = 0;

        for (PsiClass psiClass : psiClasses) {
            if (ClassUtils.isConcreteClass(psiClass)) {
                concreteClassesNumber++;
            }
            if (ClassUtils.isAbstractClass(psiClass)) {
                abstractClassesNumber++;
            }
            if (ClassUtils.isStaticClass(psiClass)) {
                staticClassesNumber++;
            }
            if (psiClass.isInterface()) {
                interfacesNumber++;
            }
        }

        long nonCommentingSourceStatements = classes.stream()
                .flatMap(CodeElement::metrics)
                .filter(metric -> metric.getType() == NCSS)
                .map(Metric::getValue)
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .longValue();

        long linesOfCode = classes.stream()
                .flatMap(ClassElement::methods)
                .map(javaMethod -> javaMethod.metric(LOC))
                .filter(Objects::nonNull)
                .map(Metric::getPsiValue)
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .longValue();

        double halsteadVolume = classes.stream()
                .flatMap(CodeElement::metrics)
                .filter(metric -> metric.getType() == CHVL)
                .map(Metric::getValue)
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .doubleValue();

        double halsteadDifficulty = classes.stream()
                .flatMap(CodeElement::metrics)
                .filter(metric -> metric.getType() == CHD)
                .map(Metric::getValue)
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .doubleValue();

        long halsteadLength = classes.stream()
                .flatMap(CodeElement::metrics)
                .filter(metric -> metric.getType() == CHL)
                .map(Metric::getValue)
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .longValue();

        double halsteadEffort = classes.stream()
                .flatMap(CodeElement::metrics)
                .filter(metric -> metric.getType() == CHEF)
                .map(Metric::getValue)
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .doubleValue();

        long halsteadVocabulary = classes.stream()
                .flatMap(CodeElement::metrics)
                .filter(metric -> metric.getType() == CHVC)
                .map(Metric::getValue)
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .longValue();

        double halsteadErrors = classes.stream()
                .flatMap(CodeElement::metrics)
                .filter(metric -> metric.getType() == CHER)
                .map(Metric::getValue)
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .doubleValue();

        p.addMetric(Metric.of(PNCSS, nonCommentingSourceStatements));
        p.addMetric(Metric.of(PLOC, linesOfCode));
        p.addMetric(Metric.of(PNOCC, concreteClassesNumber));
        p.addMetric(Metric.of(PNOAC, abstractClassesNumber));
        p.addMetric(Metric.of(PNOSC, staticClassesNumber));
        p.addMetric(Metric.of(PNOI, interfacesNumber));

        p.addMetric(Metric.of(PAHVL, halsteadVolume));
        p.addMetric(Metric.of(PAHD, halsteadDifficulty));
        p.addMetric(Metric.of(PACHL, halsteadLength));
        p.addMetric(Metric.of(PACHEF, halsteadEffort));
        p.addMetric(Metric.of(PACHVC, halsteadVocabulary));
        p.addMetric(Metric.of(PACHER, halsteadErrors));

        long packageCC = classes.stream()
                .flatMap(ClassElement::methods)
                .flatMap(CodeElement::metrics)
                .filter(metric -> metric.getType() == CC)
                .map(Metric::getValue)
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .longValue();

        double maintainabilityIndex = 0.0;
        if (halsteadVolume > 0.0 && packageCC > 0L && linesOfCode > 0L) {
            maintainabilityIndex = Math.max(0, (171
                    - 5.2 * Math.log(halsteadVolume)
                    - 0.23 * Math.log(packageCC)
                    - 16.2 * Math.log(linesOfCode)) * 100 / 171);
        }

        p.addMetric(Metric.of(MetricType.PAMI, maintainabilityIndex));
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

            visitedPackages.add(psiPackage);

            // Ensure buckets exist (defensive).
            classesPerPackageNumber.createBucket(psiPackage);
            abstractClassesPerPackageNumber.createBucket(psiPackage);

            // Afferent coupling: unique external packages that depend on this package.
            // dependenciesBuilder.getDependentsSet(...) is assumed to return classes that depend on psiClass.
            Set<PsiClass> dependentClasses = dependenciesBuilder.getDependentsSet(psiClass, psiPackage);
            if (dependentClasses != null && !dependentClasses.isEmpty()) {
                Set<PsiPackage> afferentSet =
                        afferentPackages.computeIfAbsent(psiPackage, k -> ConcurrentHashMap.newKeySet());
                for (PsiClass dependentClass : dependentClasses) {
                    if (dependentClass == null) {
                        continue;
                    }
                    PsiPackage dependentPackage = ClassUtils.findPackage(dependentClass);
                    if (dependentPackage != null && !dependentPackage.equals(psiPackage)) {
                        afferentSet.add(dependentPackage);
                    }
                }
            }

            // Efferent coupling: unique external packages that this package depends on.
            Set<PsiPackage> efferentSet =
                    efferentPackages.computeIfAbsent(psiPackage, k -> ConcurrentHashMap.newKeySet());

            Set<PsiPackage> packageDependencies = dependenciesBuilder.getPackagesDependencies(psiClass)
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(p -> !p.equals(psiPackage))
                    .collect(Collectors.toSet());

            efferentSet.addAll(packageDependencies);

            if (psiClass.isInterface() || psiClass.hasModifierProperty(PsiModifier.ABSTRACT)) {
                abstractClassesPerPackageNumber.incrementBucketValue(psiPackage);
            }
            classesPerPackageNumber.incrementBucketValue(psiPackage);
        }
    }
}
