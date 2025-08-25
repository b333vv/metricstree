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
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.code.JavaCode;
import org.b333vv.metric.model.code.JavaPackage;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.util.BucketedCount;
import org.b333vv.metric.model.util.ClassUtils;
import org.b333vv.metric.model.metric.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.b333vv.metric.model.metric.MetricType.*;

public class PackageMetricsSetCalculator {
    private final AnalysisScope scope;
    private final DependenciesBuilder dependenciesBuilder;
    private final JavaProject javaProject;

    private final BucketedCount<PsiPackage> externalDependenciesPerPackageNumber = new BucketedCount<>();
    private final Map<PsiPackage, HashSet<PsiClass>> dependents = new ConcurrentHashMap<>();
    private final BucketedCount<PsiPackage> abstractClassesPerPackageNumber = new BucketedCount<>();
    private final BucketedCount<PsiPackage> classesPerPackageNumber = new BucketedCount<>();

    public PackageMetricsSetCalculator(AnalysisScope scope, DependenciesBuilder dependenciesBuilder, JavaProject javaProject) {
        this.scope = scope;
        this.dependenciesBuilder = dependenciesBuilder;
        this.javaProject = javaProject;
    }

    public void calculate() {
        scope.accept(new Visitor());
        javaProject.allPackages().forEach(this::handlePackage);
    }

    private void handlePackage(@NotNull JavaPackage p) {
        int afferentCoupling = dependents.getOrDefault(p.getPsiPackage(), new HashSet<>()).size();
        int efferentCoupling = externalDependenciesPerPackageNumber.getBucketValue(p.getPsiPackage());
        Value instability = (afferentCoupling + efferentCoupling) == 0 ? Value.of(1.0) :
                Value.of((double) efferentCoupling)
                        .divide(Value.of((double) (afferentCoupling + efferentCoupling)));
        p.addMetric(Metric.of(Ce, efferentCoupling));
        p.addMetric(Metric.of(Ca, afferentCoupling));
        p.addMetric(Metric.of(I, instability));

        int classesNumber = classesPerPackageNumber.getBucketValue(p.getPsiPackage());
        int abstractClassesNumber = abstractClassesPerPackageNumber.getBucketValue(p.getPsiPackage());
        Value abstractness = classesNumber == 0 ? Value.of(1.0) :
                Value.of((double) abstractClassesNumber).divide(Value.of((double) classesNumber));
        p.addMetric(Metric.of(A, abstractness));

        Value distance = Value.of(1.0).minus(instability).minus(abstractness).abs();
        p.addMetric(Metric.of(D, distance));

        ApplicationManager.getApplication().runReadAction(() -> addStatisticMetrics(p));
    }

    private void addStatisticMetrics(JavaPackage p) {
        List<PsiClass> psiClasses = p.classes().map(JavaClass::getPsiClass).collect(Collectors.toList());
        long concreteClassesNumber = 0;
        long abstractClassesNumber = 0;
        long staticClassesNumber = 0;
        long interfacesNumber = 0;

        for (PsiClass psiClass: psiClasses) {
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

        long nonCommentingSourceStatements = p
                .classes()
                .flatMap(JavaCode::metrics)
                .filter(metric -> metric.getType() == NCSS)
                .map(Metric::getValue)
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .longValue();

        long linesOfCode = p
                .classes()
                .flatMap(JavaClass::methods)
                .map(javaMethod -> javaMethod.metric(LOC).getPsiValue())
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .longValue();

        double halsteadVolume = p
                .classes()
                .flatMap(JavaCode::metrics)
                .filter(metric -> metric.getType() == CHVL)
                .map(Metric::getValue)
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .doubleValue();

        double halsteadDifficulty = p
                .classes()
                .flatMap(JavaCode::metrics)
                .filter(metric -> metric.getType() == CHD)
                .map(Metric::getValue)
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .doubleValue();

        long halsteadLength = p
                .classes()
                .flatMap(JavaCode::metrics)
                .filter(metric -> metric.getType() == CHL)
                .map(Metric::getValue)
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .longValue();

        double halsteadEffort = p
                .classes()
                .flatMap(JavaCode::metrics)
                .filter(metric -> metric.getType() == CHEF)
                .map(Metric::getValue)
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .doubleValue();

        long halsteadVocabulary = p
                .classes()
                .flatMap(JavaCode::metrics)
                .filter(metric -> metric.getType() == CHVC)
                .map(Metric::getValue)
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .longValue();

        double halsteadErrors = p
                .classes()
                .flatMap(JavaCode::metrics)
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

        long packageCC = p
                .classes().flatMap(JavaClass::methods)
                .flatMap(JavaCode::metrics)
                .filter(metric -> metric.getType() == CC)
                .map(Metric::getValue)
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .longValue();

        double maintainabilityIndex = 0.0;
        if (packageCC > 0L && linesOfCode > 0L) {
            maintainabilityIndex = Math.max(0, (171 - 5.2 * Math.log(halsteadVolume)
                    - 0.23 * Math.log(packageCC) - 16.2 * Math.log(linesOfCode)) * 100 / 171);
        }

        p.addMetric(Metric.of(MetricType.PAMI, maintainabilityIndex));
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

            dependents.computeIfAbsent(psiPackage, k -> new HashSet<>())
                    .addAll(dependenciesBuilder.getDependentsSet(psiClass, psiPackage));

            externalDependenciesPerPackageNumber.createBucket(psiPackage);
            Set<PsiPackage> packageDependencies = dependenciesBuilder.getPackagesDependencies(psiClass)
                    .stream()
                    .filter(p -> !p.equals(psiPackage))
                    .collect(Collectors.toSet());
            externalDependenciesPerPackageNumber.incrementBucketValue(psiPackage, packageDependencies.size());

            if (psiClass.isInterface() || psiClass.hasModifierProperty(PsiModifier.ABSTRACT)) {
                abstractClassesPerPackageNumber.incrementBucketValue(psiPackage);
            }
            classesPerPackageNumber.incrementBucketValue(psiPackage);
        }
    }
}
