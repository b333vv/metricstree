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
import com.intellij.psi.*;
import org.b333vv.metric.model.code.ClassElement;
import org.b333vv.metric.model.code.CodeElement;
import org.b333vv.metric.model.code.PackageElement;
import org.b333vv.metric.model.code.ProjectElement;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.model.util.ClassUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.KtClass;
import org.jetbrains.kotlin.psi.KtClassOrObject;
import org.jetbrains.kotlin.psi.KtObjectDeclaration;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.b333vv.metric.model.metric.MetricType.*;
import static org.jetbrains.kotlin.asJava.LightClassUtilsKt.toLightClass;

/**
 * Calculates package-level metrics for packages visited in the given
 * {@link AnalysisScope}.<br>
 *
 * <h3>What is considered a package</h3>
 * Metrics are calculated only for packages that contain at least one
 * <b>non-anonymous</b> class visited while
 * traversing the {@link AnalysisScope}. This avoids producing misleading "all
 * zeros" metric sets for packages
 * that are present in {@link ProjectElement#allPackages()} but are outside the
 * scope being analyzed.
 *
 * <h3>Martin metrics</h3>
 * The following Robert C. Martin package metrics are computed:
 * <ul>
 * <li><b>Ce</b> (efferent coupling): number of unique external packages that
 * the package depends on.</li>
 * <li><b>Ca</b> (afferent coupling): number of unique external packages that
 * depend on the package.</li>
 * <li><b>I</b> (instability): {@code Ce / (Ca + Ce)}. When {@code Ca + Ce == 0}
 * treated as {@code 0.0}.</li>
 * <li><b>A</b> (abstractness): {@code Na / Nc}, where {@code Na} is number of
 * abstract classes + interfaces,
 * {@code Nc} is total number of classes + interfaces in the package. When
 * {@code Nc == 0} treated as {@code 0.0}.</li>
 * <li><b>D</b> (distance from main sequence): {@code | 1 - A - I |}.</li>
 * </ul>
 *
 * <p>
 * Couplings ({@code Ce}/{@code Ca}) are computed using <b>unique package
 * sets</b> aggregated across all classes
 * in a package, which prevents overcounting the same dependency multiple times.
 * </p>
 *
 * <h3>Aggregated package statistics</h3>
 * Additionally calculates package-aggregated sums/counters such as:
 * {@link MetricType#PNCSS}, {@link MetricType#PLOC}, counts of class kinds
 * (concrete/abstract/static/interfaces),
 * package-aggregated Halstead metrics (prefixed with {@code PA*}), and package
 * Maintainability Index ({@link MetricType#PAMI}).
 */
public class PackageMetricsSetCalculator {
    private final AnalysisScope scope;
    private final DependenciesBuilder dependenciesBuilder;
    private final ProjectElement projectElement;

    public PackageMetricsSetCalculator(AnalysisScope scope, DependenciesBuilder dependenciesBuilder,
            ProjectElement projectElement) {
        this.scope = scope;
        this.dependenciesBuilder = dependenciesBuilder;
        this.projectElement = projectElement;
    }

    public void calculate() {
        projectElement.allPackages()
                .forEach(this::handlePackage);
    }

    private void handlePackage(@NotNull PackageElement p) {
        ApplicationManager.getApplication().runReadAction(() -> {
            PsiPackage psiPackage = p.getPsiPackage();
            if (psiPackage == null) {
                return;
            }

            Set<PsiPackage> efferentPackages = new HashSet<>();
            Set<PsiPackage> afferentPackages = new HashSet<>();
            int classesNumber = 0;
            int abstractClassesNumber = 0;

            List<ClassElement> classElements = p.classes().collect(Collectors.toList());

            for (ClassElement classElement : classElements) {
                PsiClass psiClass = getPsiClass(classElement);
                if (psiClass == null || ClassUtils.isAnonymous(psiClass)) {
                    continue;
                }

                classesNumber++;
                if (psiClass.isInterface() || psiClass.hasModifierProperty(PsiModifier.ABSTRACT)) {
                    abstractClassesNumber++;
                }

                Set<PsiClass> dependentClasses = dependenciesBuilder.getDependentsSet(psiClass, psiPackage);
                if (dependentClasses != null && !dependentClasses.isEmpty()) {
                    for (PsiClass dependentClass : dependentClasses) {
                        if (dependentClass == null)
                            continue;
                        PsiPackage dependentPackage = ClassUtils.findPackage(dependentClass);
                        if (dependentPackage != null && !dependentPackage.equals(psiPackage)) {
                            afferentPackages.add(dependentPackage);
                        }
                    }
                }

                Set<PsiPackage> packageDependencies = dependenciesBuilder.getPackagesDependencies(psiClass);
                if (packageDependencies != null) {
                    for (PsiPackage dependentPackage : packageDependencies) {
                        if (dependentPackage != null && !dependentPackage.equals(psiPackage)) {
                            efferentPackages.add(dependentPackage);
                        }
                    }
                }
            }

            int afferentCoupling = afferentPackages.size();
            int efferentCoupling = efferentPackages.size();

            Value instability = (afferentCoupling + efferentCoupling) == 0
                    ? Value.of(0.0)
                    : Value.of((double) efferentCoupling)
                            .divide(Value.of((double) (afferentCoupling + efferentCoupling)));

            p.addMetric(Metric.of(Ce, efferentCoupling));
            p.addMetric(Metric.of(Ca, afferentCoupling));
            p.addMetric(Metric.of(I, instability));

            Value abstractness = classesNumber == 0
                    ? Value.of(0.0)
                    : Value.of((double) abstractClassesNumber).divide(Value.of((double) classesNumber));

            p.addMetric(Metric.of(A, abstractness));

            Value distance = Value.of(1.0).minus(instability).minus(abstractness).abs();
            p.addMetric(Metric.of(D, distance));

            addStatisticMetrics(p);
        });
    }

    private void addStatisticMetrics(PackageElement p) {
        List<ClassElement> classes = p.classes().collect(Collectors.toList());
        List<PsiClass> psiClasses = classes.stream()
                .map(this::getPsiClass)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        long concreteClassesNumber = 0;
        long abstractClassesNumber = 0;
        long staticClassesNumber = 0;
        long interfacesNumber = 0;

        long kotlinObjectsNumber = 0;
        long companionObjectsNumber = 0;
        long dataClassesNumber = 0;
        long sealedClassesNumber = 0;

        for (PsiClass psiClass : psiClasses) {
            PsiElement navElement = psiClass.getNavigationElement();
            boolean isKotlinObject = navElement instanceof KtObjectDeclaration;

            if (ClassUtils.isConcreteClass(psiClass)) {
                concreteClassesNumber++;
            }
            if (ClassUtils.isAbstractClass(psiClass)) {
                abstractClassesNumber++;
            }
            // For Kotlin, we exclude objects from "Static Classes" count to avoid double
            // counting,
            // as they are covered by PNOKOBJ and PNOKCO.
            // Static nested classes in Kotlin (nested classes without inner) will still be
            // counted here.
            if (ClassUtils.isStaticClass(psiClass) && !isKotlinObject) {
                staticClassesNumber++;
            }
            if (psiClass.isInterface()) {
                interfacesNumber++;
            }

            if (navElement instanceof KtClass) {
                KtClass ktClass = (KtClass) navElement;
                if (ktClass.isData()) {
                    dataClassesNumber++;
                }
                if (ktClass.isSealed()) {
                    sealedClassesNumber++;
                }
            } else if (isKotlinObject) {
                KtObjectDeclaration ktObj = (KtObjectDeclaration) navElement;
                if (ktObj.isCompanion()) {
                    companionObjectsNumber++;
                } else {
                    kotlinObjectsNumber++;
                }
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

        p.addMetric(Metric.of(PNOKOBJ, kotlinObjectsNumber));
        p.addMetric(Metric.of(PNOKCO, companionObjectsNumber));
        p.addMetric(Metric.of(PNOKDC, dataClassesNumber));
        p.addMetric(Metric.of(PNOKSC, sealedClassesNumber));

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

    private PsiClass getPsiClass(ClassElement classElement) {
        PsiClass psiClass = classElement.getPsiClass();
        if (psiClass != null) {
            return psiClass;
        }
        KtClassOrObject ktClass = classElement.getKtClassOrObject();
        if (ktClass != null) {
            return toLightClass(ktClass);
        }
        return null;
    }
}