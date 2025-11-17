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
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.util.Query;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.kotlin.asJava.LightClassUtilsKt;
import org.jetbrains.kotlin.psi.KtClassOrObject;
import org.jetbrains.kotlin.psi.KtDeclaration;
import org.jetbrains.kotlin.psi.KtFile;
import org.b333vv.metric.model.code.ClassElement;
import org.b333vv.metric.model.code.CodeElement;
import org.b333vv.metric.model.code.ProjectElement;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.util.Bag;
import org.b333vv.metric.model.util.ClassUtils;
import org.b333vv.metric.model.metric.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static org.b333vv.metric.model.metric.MetricType.*;

public class ProjectMetricsSetCalculator {
    private final AnalysisScope scope;
    private final DependenciesBuilder dependenciesBuilder;
    private final ProjectElement projectElement;
    private ProgressIndicator indicator;
    private int filesCount;
    private int progress = 0;
    private int attributesNumber = 0;
    private int publicAttributesNumber = 0;
    private int classesNumber = 0;
    private Value totalAttributesVisibility = Value.of(0.0);
    private final Bag<String> classesPerPackage = new Bag<>();
    private final Bag<String> packageVisibleAttributesPerPackage = new Bag<>();
    private final Map<PsiClass, Integer> subclassesPerClass = new HashMap<>();

    private int availableFields = 0;
    private int inheritedFields = 0;

    private int totalCoupling = 0;

    private int methodsNumber = 0;
    private int publicMethodsNumber = 0;
    private Value totalMethodsVisibility = Value.of(0.0);
    private final Bag<String> packageVisibleMethodsPerPackage = new Bag<>();

    private int availableMethods = 0;
    private int inheritedMethods = 0;

    private int overridingMethodsNumber = 0;
    private int overridePotentialsNumber = 0;

    private long concreteClassesNumber = 0;
    private long abstractClassesNumber = 0;
    private long staticClassesNumber = 0;
    private long interfacesNumber = 0;
    private long linesOfCode = 0;
    double halsteadVolume = 0.0;


    public ProjectMetricsSetCalculator(AnalysisScope scope, DependenciesBuilder dependenciesBuilder, ProjectElement projectElement) {
        this.scope = scope;
        this.dependenciesBuilder = dependenciesBuilder;
        this.projectElement = projectElement;
    }

    public void calculate() {
        indicator = ProgressManager.getInstance().getProgressIndicator();
        indicator.setText("Initializing");
        filesCount = scope.getFileCount();

        scope.accept(new Visitor());

        indicator.setText("Calculating metrics");

        calculateMood();
        calculateStatistics();
        calculateQmood();
        calculateHalstead();
        calculateMaintainabilityIndex();
    }

    private void calculateQmood() {
        double zCoupling = calculateZCoupling();
        double zCohesion = calculateZCohesion();
        double zMessaging = calculateZMessaging();
        double zDesignSize = calculateZDesignSize();
        double zEncapsulation = 1.0;
        double zComposition = calculateZComposition();
        double zPolymorphism = calculateZPolymorphism();
        double zAbstraction = calculateZAbstraction();
        double zComplexity = calculateZComplexity();
        double zHierarchies = calculateZHierarchies();
        double zInheritance = calculateZInheritance();

        double Reusability = -0.25 * zCoupling + 0.25 * zCohesion + 0.5 * zMessaging + 0.5 * zDesignSize;
        double Flexibility = 0.25 * zEncapsulation -0.25 * zCoupling + 0.5 * zComposition + 0.5 * zPolymorphism;
        double Understandability = -0.33 * zAbstraction + 0.33 * zEncapsulation - 0.33 * zCoupling
                + 0.33 * zCohesion - 0.33 * zPolymorphism - 0.33 * zComplexity - 0.33 * zDesignSize;
        double Functionality = 0.12 * zCohesion + 0.22 * zPolymorphism + 0.22 * zMessaging + 0.22 * zDesignSize
                + 0.22 * zHierarchies;
        double Extendibility = 0.5 * zAbstraction - 0.5 * zCoupling + 0.5 * zInheritance + 0.5 * zPolymorphism;
        double Effectiveness = 0.2 * zAbstraction + 0.2 * zEncapsulation + 0.2 * zComposition
                + 0.2 * zInheritance + 0.2 * zPolymorphism;

        projectElement.addMetric(Metric.of(MetricType.Reusability, Reusability));
        projectElement.addMetric(Metric.of(MetricType.Flexibility, Flexibility));
        projectElement.addMetric(Metric.of(MetricType.Understandability, Understandability));
        projectElement.addMetric(Metric.of(MetricType.Functionality, Functionality));
        projectElement.addMetric(Metric.of(MetricType.Extendibility, Extendibility));
        projectElement.addMetric(Metric.of(MetricType.Effectiveness, Effectiveness));
    }

    private void calculateStatistics() {
        addClassesCounters();
        addClassesNonCommentingSourceStatements();
        addLinesOfCode();
    }

    private void calculateMood() {
        addAttributeHidingFactor();
        addAttributeInheritanceFactor();
        addCouplingFactor();
        addMethodHidingFactor();
        addMethodInheritanceFactor();
        addPolymorphismFactor();
    }

    private void calculateMaintainabilityIndex() {
        long projectCC = projectElement
                .allClasses().flatMap(ClassElement::methods)
                .flatMap(CodeElement::metrics)
                .filter(metric -> metric.getType() == CC)
                .map(Metric::getValue)
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .longValue();

        double maintainabilityIndex = 0.0;
        if (projectCC > 0L && linesOfCode > 0L) {
            maintainabilityIndex = Math.max(0, (171 - 5.2 * Math.log(halsteadVolume)
                    - 0.23 * Math.log(projectCC) - 16.2 * Math.log(linesOfCode)) * 100 / 171);
        }

        projectElement.addMetric(Metric.of(MetricType.PRMI, maintainabilityIndex));
    }
    private void calculateHalstead() {

        halsteadVolume = projectElement
                .allPackages().flatMap(CodeElement::metrics)
                .filter(metric -> metric.getType() == PAHVL)
                .map(Metric::getValue)
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .doubleValue();

        double halsteadDifficulty = projectElement
                .allPackages().flatMap(CodeElement::metrics)
                .filter(metric -> metric.getType() == PAHD)
                .map(Metric::getValue)
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .doubleValue();

        long halsteadLength = projectElement
                .allPackages().flatMap(CodeElement::metrics)
                .filter(metric -> metric.getType() == PACHL)
                .map(Metric::getValue)
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .longValue();

        double halsteadEffort = projectElement
                .allPackages().flatMap(CodeElement::metrics)
                .filter(metric -> metric.getType() == PACHEF)
                .map(Metric::getValue)
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .doubleValue();

        long halsteadVocabulary = projectElement
                .allPackages().flatMap(CodeElement::metrics)
                .filter(metric -> metric.getType() == PACHVC)
                .map(Metric::getValue)
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .longValue();

        double halsteadErrors = projectElement
                .allPackages().flatMap(CodeElement::metrics)
                .filter(metric -> metric.getType() == PACHER)
                .map(Metric::getValue)
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .doubleValue();

        projectElement.addMetric(Metric.of(PRHVL, halsteadVolume));
        projectElement.addMetric(Metric.of(PRHD, halsteadDifficulty));
        projectElement.addMetric(Metric.of(PRCHL, halsteadLength));
        projectElement.addMetric(Metric.of(PRCHEF, halsteadEffort));
        projectElement.addMetric(Metric.of(PRCHVC, halsteadVocabulary));
        projectElement.addMetric(Metric.of(PRCHER, halsteadErrors));
    }

    private double calculateZCoupling() {
        return calculateZScore(
                projectElement.allPackages().flatMap(CodeElement::metrics)
                    .filter(metric -> metric.getType() == Ce)
                    .map(Metric::getValue)
                    .collect(Collectors.toUnmodifiableList())
        );
    }

    private double calculateZCohesion() {
        return 1.0/calculateZScore(
                projectElement.allClasses().flatMap(CodeElement::metrics)
                    .filter(metric -> metric.getType() == LCOM)
                    .map(Metric::getValue)
                    .collect(Collectors.toUnmodifiableList())
        );
    }

    private double calculateZMessaging() {
        return calculateZScore(
                projectElement.allClasses().flatMap(CodeElement::metrics)
                    .filter(metric -> metric.getType() == NOM)
                    .map(Metric::getValue)
                    .collect(Collectors.toUnmodifiableList())
        );
    }

    private double calculateZDesignSize() {
        return calculateZScore(
                projectElement.allPackages().flatMap(CodeElement::metrics)
                        .filter(metric -> metric.getType() == PNOCC)
                        .map(Metric::getValue)
                        .collect(Collectors.toUnmodifiableList())
        );
    }

    private double calculateZComposition() {
            return calculateZScore(
                    projectElement.allClasses().flatMap(CodeElement::metrics)
                            .filter(metric -> metric.getType() == NOA)
                            .map(Metric::getValue)
                            .collect(Collectors.toUnmodifiableList())
            );
    }

    private double calculateZPolymorphism() {
        return calculateZScore(
                projectElement.allClasses().flatMap(CodeElement::metrics)
                        .filter(metric -> metric.getType() == NOOM)
                        .map(Metric::getValue)
                        .collect(Collectors.toUnmodifiableList())
        );
    }

    private double calculateZAbstraction() {
        return calculateZScore(
                projectElement.allPackages().flatMap(CodeElement::metrics)
                        .filter(metric -> metric.getType() == A)
                        .map(Metric::getValue)
                        .collect(Collectors.toUnmodifiableList())
        );
    }

    private double calculateZComplexity() {
        return calculateZScore(
                projectElement.allClasses().flatMap(CodeElement::metrics)
                        .filter(metric -> metric.getType() == WMC)
                        .map(Metric::getValue)
                        .collect(Collectors.toUnmodifiableList())
        );
    }

    private double calculateZHierarchies() {
        return calculateZScore(
                projectElement.allClasses().flatMap(CodeElement::metrics)
                        .filter(metric -> metric.getType() == DIT)
                        .map(Metric::getValue)
                        .collect(Collectors.toUnmodifiableList())
        );
    }

    private double calculateZInheritance() {
        List<CodeElement> classes = projectElement.allClasses()
                .collect(Collectors.toUnmodifiableList());
        double zInheritance = 0.0;
        for (CodeElement aClass : classes) {
            if (aClass.metric(MetricType.NOM) == null) {
                continue;
            }
            Value nom = aClass.metric(NOM).getPsiValue();
            Metric noomMetric = aClass.metric(NOOM);
            Value noom = noomMetric != null ? noomMetric.getPsiValue() : Value.ZERO;
            if (nom.isGreaterThan(Value.of(0))) {
                zInheritance = zInheritance + (noom.divide(nom.times(Value.of(100)))).doubleValue();
            }
        }
        return zInheritance;
    }

    private double calculateZScore(List<Value> source) {
        Value max = source.stream().max(Value::compareTo).orElse(Value.ZERO);
        Value avg = source.stream().reduce(Value::plus).orElse(Value.ZERO)
                .divide(Value.of(source.size()));
        Value dispersion = source.stream()
                .map(v -> v.minus(avg).pow(2))
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .divide(Value.of(source.size()));
        Value std = Value.of(Math.sqrt(dispersion.doubleValue()));
        Value zScore = max.minus(avg).divide(std);
        return zScore.doubleValue();
    }

    private void addClassesNonCommentingSourceStatements() {
        long nonCommentingSourceStatements = projectElement.allClasses().flatMap(CodeElement::metrics)
                .filter(metric -> metric.getType() == NCSS)
                .map(Metric::getValue)
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .longValue();
        projectElement.addMetric(Metric.of(PNCSS, nonCommentingSourceStatements));
    }

    private void addLinesOfCode() {
        linesOfCode = projectElement.allClasses()
                .flatMap(ClassElement::methods)
                .map(javaMethod -> {
                    Metric m = javaMethod.metric(LOC);
                    return m != null ? m.getPsiValue() : Value.ZERO;
                })
                .reduce(Value::plus)
                .orElse(Value.ZERO)
                .longValue();
        projectElement.addMetric(Metric.of(PLOC, linesOfCode));
    }

    private void addClassesCounters() {
        projectElement.addMetric(Metric.of(PNOCC, concreteClassesNumber));
        projectElement.addMetric(Metric.of(PNOAC, abstractClassesNumber));
        projectElement.addMetric(Metric.of(PNOSC, staticClassesNumber));
        projectElement.addMetric(Metric.of(PNOI, interfacesNumber));
    }

    private void addPolymorphismFactor() {
        Value polymorphismFactor = overridePotentialsNumber == 0 ? Value.of(1.0) :
                Value.of((double) overridingMethodsNumber).divide(Value.of((double) overridePotentialsNumber));
        projectElement.addMetric(Metric.of(PF, polymorphismFactor));
    }

    private void addMethodInheritanceFactor() {
        Value methodInheritanceFactor = Value.ZERO;
        if (availableMethods > 0) {
            methodInheritanceFactor = Value.of((double) inheritedMethods).divide(Value.of((double) availableMethods));
        }
        projectElement.addMetric(Metric.of(MIF, methodInheritanceFactor));
    }

    private void addMethodHidingFactor() {
        totalMethodsVisibility = totalMethodsVisibility
                .plus((Value.of(publicMethodsNumber)
                        .times(Value.of(classesNumber - 1))));
        final Set<String> packages = classesPerPackage.getContents();
        for (String aPackage : packages) {
            final int visibleMethods = packageVisibleMethodsPerPackage.getCountForObject(aPackage);
            final int classes = classesPerPackage.getCountForObject(aPackage);
            totalMethodsVisibility = totalMethodsVisibility
                    .plus((Value.of(visibleMethods)
                            .times(Value.of(classes - 1))));
        }
        final Value denominator = Value.of(methodsNumber).times(Value.of(classesNumber - 1));
        Value methodHidingFactor = Value.ZERO;
        if (!denominator.equals(Value.ZERO)) {
            final Value numerator = denominator.minus(totalMethodsVisibility);
            methodHidingFactor = numerator.divide(denominator);
        }

        projectElement.addMetric(Metric.of(MHF, methodHidingFactor));
    }

    private void addCouplingFactor() {
        Value numerator = Value.of((double) totalCoupling);
        Value denominator = Value.of((double) classesNumber)
                .times(Value.of((double) (classesNumber - 1))).divide(Value.of(2.0));
        Value couplingFactor = Value.ZERO;
        if (!denominator.equals(Value.ZERO)) {
            couplingFactor = numerator.divide(denominator);
        }

        projectElement.addMetric(Metric.of(CF, couplingFactor));
    }

    private void addAttributeInheritanceFactor() {
        Value attributeInheritanceFactor = Value.ZERO;
        if (availableFields > 0) {
            attributeInheritanceFactor = Value.of((double) inheritedFields)
                    .divide(Value.of((double) availableFields));
        }

        projectElement.addMetric(Metric.of(AIF, attributeInheritanceFactor));
    }

    private void addAttributeHidingFactor() {
        totalAttributesVisibility = totalAttributesVisibility
                .plus((Value.of(publicAttributesNumber)
                        .times(Value.of(classesNumber - 1))));
        final Set<String> packages = classesPerPackage.getContents();
        for (String aPackage : packages) {
            final int visibleAttributes = packageVisibleAttributesPerPackage.getCountForObject(aPackage);
            final int classes = classesPerPackage.getCountForObject(aPackage);
            totalAttributesVisibility = totalAttributesVisibility
                    .plus((Value.of(visibleAttributes)
                            .times(Value.of(classes - 1))));
        }
        final Value denominator = Value.of(attributesNumber).times(Value.of(classesNumber - 1));
        Value attributeHidingFactor = Value.ZERO;
        if (!denominator.equals(Value.ZERO)) {
            final Value numerator = denominator.minus(totalAttributesVisibility);
            attributeHidingFactor = numerator.divide(denominator);
        }

        projectElement.addMetric(Metric.of(AHF, attributeHidingFactor));
    }

    private class Visitor extends JavaRecursiveElementVisitor {
        @Override
        public void visitFile(PsiFile psiFile) {
            super.visitFile(psiFile);
            // Include Kotlin files by converting Kt classes to light Java PsiClass
            if (psiFile instanceof KtFile) {
                KtFile ktFile = (KtFile) psiFile;
                for (KtDeclaration decl : ktFile.getDeclarations()) {
                    if (decl instanceof KtClassOrObject) {
                        PsiClass light = LightClassUtilsKt.toLightClass((KtClassOrObject) decl);
                        if (light != null) {
                            indicator.checkCanceled();

                            // Process same as for Java classes
                            processAttributeInheritanceFactor(light);
                            processAttributeAndMethodHidingFactor(light);
                            processCouplingFactor(light);
                            processMethodInheritanceFactor(light);
                            processPolymorphismFactor(light);
                            processStatisticMetrics(light);

                            // Simulate visit of methods to collect visibility stats
                            for (PsiMethod psiMethod : light.getMethods()) {
                                methodsNumber++;
                                final PsiClass containingClass = psiMethod.getContainingClass();
                                if (psiMethod.hasModifierProperty(PsiModifier.PRIVATE) ||
                                        (containingClass != null && containingClass.hasModifierProperty(PsiModifier.PRIVATE))) {
                                    // private: not visible outside
                                } else if (psiMethod.hasModifierProperty(PsiModifier.PROTECTED) ||
                                        (containingClass != null && containingClass.hasModifierProperty(PsiModifier.PROTECTED))) {
                                    totalMethodsVisibility = totalMethodsVisibility.plus(Value.of(getSubclassCount(containingClass)));
                                } else if ((psiMethod.hasModifierProperty(PsiModifier.PUBLIC) || (containingClass != null && containingClass.isInterface())) &&
                                        (containingClass != null && containingClass.hasModifierProperty(PsiModifier.PUBLIC))) {
                                    publicMethodsNumber++;
                                } else if (containingClass != null) {
                                    final String packageName = ClassUtils.calculatePackageName(containingClass);
                                    packageVisibleMethodsPerPackage.add(packageName);
                                }
                            }

                            // Simulate visit of fields to collect visibility stats
                            for (PsiField psiField : light.getFields()) {
                                attributesNumber++;
                                final PsiClass containingClass = psiField.getContainingClass();
                                if (psiField.hasModifierProperty(PsiModifier.PRIVATE) ||
                                        (containingClass != null && containingClass.hasModifierProperty(PsiModifier.PRIVATE))) {
                                    // private: not visible outside
                                } else if (psiField.hasModifierProperty(PsiModifier.PROTECTED) ||
                                        (containingClass != null && containingClass.hasModifierProperty(PsiModifier.PROTECTED))) {
                                    totalAttributesVisibility = totalAttributesVisibility.plus(Value.of(getSubclassCount(containingClass)));
                                } else if ((psiField.hasModifierProperty(PsiModifier.PUBLIC) || (containingClass != null && containingClass.isInterface())) &&
                                        (containingClass != null && containingClass.hasModifierProperty(PsiModifier.PUBLIC))) {
                                    publicAttributesNumber++;
                                } else if (containingClass != null) {
                                    final String packageName = ClassUtils.calculatePackageName(containingClass);
                                    packageVisibleAttributesPerPackage.add(packageName);
                                }
                            }

                            indicator.setText("Calculating metrics on project level: processing class " + light.getName() + "...");
                            progress++;
                            indicator.setIndeterminate(false);
                            indicator.setFraction((double) progress / (double) filesCount);
                        }
                    }
                }
            }
        }
        @Override
        public void visitClass(PsiClass aClass) {
            super.visitClass(aClass);

            indicator.checkCanceled();

            processAttributeInheritanceFactor(aClass);
            processAttributeAndMethodHidingFactor(aClass);
            processCouplingFactor(aClass);
            processMethodInheritanceFactor(aClass);
            processPolymorphismFactor(aClass);

            processStatisticMetrics(aClass);

            indicator.setText("Calculating metrics on project level: processing class " + aClass.getName() + "...");
            progress++;
            indicator.setIndeterminate(false);
            indicator.setFraction((double) progress / (double) filesCount);
        }

        private void processStatisticMetrics(@NotNull PsiClass psiClass) {
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

        private void processPolymorphismFactor(@NotNull PsiClass psiClass) {
            int newMethodsCount = 0;
            int overriddenMethodsCount = 0;
            final PsiMethod[] methods = psiClass.getMethods();
            for (PsiMethod method : methods) {
                final PsiMethod[] superMethods = method.findSuperMethods();
                if (superMethods.length == 0) {
                    newMethodsCount++;
                } else {
                    overriddenMethodsCount++;
                }
            }
            overridePotentialsNumber += newMethodsCount * getSubclassCount(psiClass);
            overridingMethodsNumber += overriddenMethodsCount;
        }

        private void processMethodInheritanceFactor(@NotNull PsiClass psiClass) {
            final PsiMethod[] allMethods = psiClass.getAllMethods();
            final Set<PsiMethod> nonOverriddenMethods = new HashSet<>();
            for (PsiMethod method : allMethods) {
                boolean overrideFound = false;
                for (PsiMethod testMethod : allMethods) {
                    if (overrides(testMethod, method)) {
                        overrideFound = true;
                        break;
                    }
                }
                if (!overrideFound) {
                    nonOverriddenMethods.add(method);
                }
            }
            for (PsiMethod method : nonOverriddenMethods) {
                final PsiClass containingClass = method.getContainingClass();
                if (containingClass == null) {
                    continue;
                }
                if (containingClass.equals(psiClass)) {
                    availableMethods++;
                } else if (!classIsInLibrary(containingClass) && !method.hasModifierProperty(PsiModifier.PRIVATE)) {
                    availableMethods++;
                    inheritedMethods++;
                }
            }
        }

        private boolean overrides(PsiMethod testMethod, PsiMethod method) {
            if (testMethod.equals(method)) {
                return false;
            }
            final PsiMethod[] superMethods = testMethod.findSuperMethods();
            for (PsiMethod superMethod : superMethods) {
                if (superMethod.equals(method)) {
                    return true;
                }
            }
            return false;
        }

        public boolean classIsInLibrary(@NotNull PsiClass psiClass) {
            PsiFile file = psiClass.getContainingFile();
            if (file == null) {
                // If we cannot resolve a file for the class, treat it as library to be safe
                return true;
            }
            VirtualFile vFile = file.getVirtualFile();
            if (vFile == null) {
                return true;
            }
            ProjectFileIndex index = ProjectRootManager.getInstance(file.getProject()).getFileIndex();
            // Class is considered in library only if it is indexed as library; Kotlin sources are in content/source
            boolean inLibrary = index.isInLibrary(vFile);
            boolean inContentOrSource = index.isInContent(vFile) || index.isInSource(vFile);
            return inLibrary || !inContentOrSource;
        }

        private void processCouplingFactor(PsiClass psiClass) {
            final Set<PsiClass> dependencies = dependenciesBuilder.getClassesDependencies(psiClass);
            totalCoupling += dependencies.stream()
                    .filter(c -> !psiClass.isInheritor(c, true))
                    .count();
        }

        private void processAttributeAndMethodHidingFactor(PsiClass psiClass) {
            classesNumber++;
            final String packageName = ClassUtils.calculatePackageName(psiClass);
            classesPerPackage.add(packageName);
        }

        private void processAttributeInheritanceFactor(PsiClass psiClass) {
            final PsiField[] allFields = psiClass.getAllFields();
            for (PsiField field : allFields) {
                final PsiClass containingClass = field.getContainingClass();
                if (containingClass == null) {
                    continue;
                }
                final String className = containingClass.getName();
                if (containingClass.equals(psiClass)) {
                    availableFields++;
                } else if (!"java.lang.Object".equals(className) && !field.hasModifierProperty(PsiModifier.PRIVATE)) {
                    availableFields++;
                    inheritedFields++;
                }
            }
        }

        @Override
        public void visitMethod(PsiMethod psiMethod) {
            super.visitMethod(psiMethod);
            methodsNumber++;
            final PsiClass containingClass = psiMethod.getContainingClass();

            if (psiMethod.hasModifierProperty(PsiModifier.PRIVATE) ||
                    Objects.requireNonNull(containingClass).hasModifierProperty(PsiModifier.PRIVATE)) {
            } else if (psiMethod.hasModifierProperty(PsiModifier.PROTECTED) ||
                    containingClass.hasModifierProperty(PsiModifier.PROTECTED)) {
                totalMethodsVisibility = totalMethodsVisibility.plus(Value.of(getSubclassCount(containingClass)));
            } else if ((psiMethod.hasModifierProperty(PsiModifier.PUBLIC) || containingClass.isInterface()) &&
                    containingClass.hasModifierProperty(PsiModifier.PUBLIC)) {
                publicMethodsNumber++;
            } else {
                final String packageName = ClassUtils.calculatePackageName(containingClass);
                packageVisibleMethodsPerPackage.add(packageName);
            }
        }

        @Override
        public void visitField(PsiField psiField) {
            super.visitField(psiField);
            attributesNumber++;
            final PsiClass containingClass = psiField.getContainingClass();

            if (psiField.hasModifierProperty(PsiModifier.PRIVATE) ||
                    Objects.requireNonNull(containingClass).hasModifierProperty(PsiModifier.PRIVATE)) {
            } else if (psiField.hasModifierProperty(PsiModifier.PROTECTED) ||
                    containingClass.hasModifierProperty(PsiModifier.PROTECTED)) {
                totalAttributesVisibility = totalAttributesVisibility.plus(Value.of(getSubclassCount(containingClass)));
            } else if ((psiField.hasModifierProperty(PsiModifier.PUBLIC) || containingClass.isInterface()) &&
                    containingClass.hasModifierProperty(PsiModifier.PUBLIC)) {
                publicAttributesNumber++;
            } else {
                final String packageName = ClassUtils.calculatePackageName(containingClass);
                packageVisibleAttributesPerPackage.add(packageName);
            }
        }

        private int getSubclassCount(final PsiClass psiClass) {
            if (subclassesPerClass.containsKey(psiClass)) {
                return subclassesPerClass.get(psiClass);
            }
            int subclassesNumber = 0;
            final GlobalSearchScope globalScope = GlobalSearchScope.allScope(scope.getProject());
            final Query<PsiClass> query = ClassInheritorsSearch.search(
                    psiClass, globalScope, true, true, true);
            for (final PsiClass inheritor : query) {
                if (!inheritor.isInterface()) {
                    subclassesNumber++;
                }
            }
            subclassesPerClass.put(psiClass, subclassesNumber);
            return subclassesNumber;
        }
    }
}
