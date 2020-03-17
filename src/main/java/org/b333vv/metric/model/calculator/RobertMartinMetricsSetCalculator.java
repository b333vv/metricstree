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

package org.b333vv.metric.model.calculator;

import com.intellij.analysis.AnalysisScope;
import com.intellij.psi.*;
import org.b333vv.metric.exec.ProjectMetricsRunner;
import org.b333vv.metric.model.builder.DependenciesBuilder;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.util.BucketedCount;
import org.b333vv.metric.model.metric.util.ClassUtils;
import org.b333vv.metric.model.metric.value.Value;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RobertMartinMetricsSetCalculator {
    private final AnalysisScope scope;

    private final BucketedCount<PsiPackage> externalDependenciesPerPackageNumber = new BucketedCount<>();
    private final Map<PsiPackage, HashSet<PsiClass>> dependents = new HashMap<>();
    private final BucketedCount<PsiPackage> abstractClassesPerPackageNumber = new BucketedCount<>();
    private final BucketedCount<PsiPackage> classesPerPackageNumber = new BucketedCount<>();

    public RobertMartinMetricsSetCalculator(AnalysisScope scope) {
        this.scope = scope;
    }

    public void calculate(JavaProject javaProject) {

        scope.accept(new Visitor());

        javaProject.getAllPackages()
                .forEach(p -> {
                    int afferentCoupling = dependents.getOrDefault(p.getPsiPackage(), new HashSet<>()).size();
                    int efferentCoupling = externalDependenciesPerPackageNumber.getBucketValue(p.getPsiPackage());
                    Value instability = (afferentCoupling + efferentCoupling) == 0 ? Value.of(1.0) :
                            Value.of((double) efferentCoupling)
                                    .divide(Value.of((double) (afferentCoupling + efferentCoupling)));
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
                    Value abstractness = classesNumber == 0 ? Value.of(1.0) :
                            Value.of((double) abstractClassesNumber).divide(Value.of((double) classesNumber));
                    p.addMetric(Metric.of(
                            "A",
                            "Abstractness",
                            "/html/Abstractness.html",
                            abstractness));

                    Value distance = Value.of(1.0).minus(instability).minus(abstractness).abs();
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
