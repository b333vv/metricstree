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
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.util.Query;
import org.b333vv.metric.exec.ProjectMetricsRunner;
import org.b333vv.metric.model.builder.DependenciesBuilder;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.util.Bag;
import org.b333vv.metric.model.metric.util.ClassUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MoodMetricsSetCalculator {
    private final AnalysisScope scope;
    private int attributesNumber = 0;
    private int publicAttributesNumber = 0;
    private int classesNumber = 0;
    private int totalAttributesVisibility = 0;
    private final Bag<String> classesPerPackage = new Bag<>();
    private final Bag<String> packageVisibleAttributesPerPackage = new Bag<>();
    private final Map<PsiClass, Integer> subclassesPerClass = new HashMap<>();

    private int availableFields = 0;
    private int inheritedFields = 0;

    private int totalCoupling = 0;

    private int methodsNumber = 0;
    private int publicMethodsNumber = 0;
    private int totalMethodsVisibility = 0;
    private final Bag<String> packageVisibleMethodsPerPackage = new Bag<>();

    private int availableMethods = 0;
    private int inheritedMethods = 0;

    private int overridingMethodsNumber = 0;
    private int overridePotentialsNumber = 0;

    public MoodMetricsSetCalculator(AnalysisScope scope) {
        this.scope = scope;
    }

    public void calculate(JavaProject javaProject) {

        scope.accept(new Visitor());

        addAttributeHidingFactor(javaProject);

        addAttributeInheritanceFactor(javaProject);

        addCouplingFactor(javaProject);

        addMethodHidingFactor(javaProject);

        addMethodInheritanceFactor(javaProject);

        addPolymorphismFactor(javaProject);
    }

    private void addPolymorphismFactor(JavaProject javaProject) {
        double polymorphismFactor = overridePotentialsNumber == 0 ? 1.0 :
                (double) overridingMethodsNumber / (double) overridePotentialsNumber;

        javaProject.addMetric(Metric.of(
                "PF",
                "Polymorphism Factor",
                "/html/PolymorphismFactor.html",
                polymorphismFactor));
    }

    private void addMethodInheritanceFactor(JavaProject javaProject) {

        double methodInheritanceFactor = (double) inheritedMethods / (double) availableMethods;

        javaProject.addMetric(Metric.of(
                "MIF",
                "Method Inheritance Factor",
                "/html/MethodInheritanceFactor.html",
                methodInheritanceFactor));
    }

    private void addMethodHidingFactor(JavaProject javaProject) {
        totalMethodsVisibility += publicMethodsNumber * (classesNumber - 1);
        final Set<String> packages = classesPerPackage.getContents();
        for (String aPackage : packages) {
            final int visibleMethods = packageVisibleMethodsPerPackage.getCountForObject(aPackage);
            final int classes = classesPerPackage.getCountForObject(aPackage);
            totalMethodsVisibility += visibleMethods * (classes - 1);
        }
        final int denominator = methodsNumber * (classesNumber - 1);
        final int numerator = denominator - totalMethodsVisibility;
        double methodHidingFactor = (double) numerator / (double) denominator;

        javaProject.addMetric(Metric.of(
                "MHF",
                "Method Hiding Factor",
                "/html/MethodHidingFactor.html",
                methodHidingFactor));
    }

    private void addCouplingFactor(JavaProject javaProject) {
        final int denominator = (classesNumber * (classesNumber - 1)) / 2;
        final int numerator = totalCoupling;
        double couplingFactor = (double) numerator / (double) denominator;

        javaProject.addMetric(Metric.of(
                "CF",
                "Coupling Factor",
                "/html/CouplingFactor.html",
                couplingFactor));
    }

    private void addAttributeInheritanceFactor(JavaProject javaProject) {
        double attributeInheritanceFactor = (double) inheritedFields / (double) availableFields;

        javaProject.addMetric(Metric.of(
                "AIF",
                "Attribute Inheritance Factor",
                "/html/AttributeInheritanceFactor.html",
                attributeInheritanceFactor));
    }

    private void addAttributeHidingFactor(JavaProject javaProject) {
        totalAttributesVisibility += publicAttributesNumber * (classesNumber - 1);
        final Set<String> packages = classesPerPackage.getContents();
        for (String aPackage : packages) {
            final int visibleAttributes = packageVisibleAttributesPerPackage.getCountForObject(aPackage);
            final int classes = classesPerPackage.getCountForObject(aPackage);
            totalAttributesVisibility += visibleAttributes * (classes - 1);
        }
        final int ahfDenominator = attributesNumber * (classesNumber - 1);
        final int ahfNumerator = ahfDenominator - totalAttributesVisibility;
        double attributeHidingFactor = (double) ahfNumerator / (double) ahfDenominator;

        javaProject.addMetric(Metric.of(
                "AHF",
                "Attribute Hiding Factor",
                "/html/AttributeHidingFactor.html",
                attributeHidingFactor));
    }

    private class Visitor extends JavaRecursiveElementVisitor {
        @Override
        public void visitClass(PsiClass aClass) {
            super.visitClass(aClass);
            processAttributeInheritanceFactor(aClass);
            processAttributeAndMethodHidingFactor(aClass);
            processCouplingFactor(aClass);
            processMethodInheritanceFactor(aClass);
            processPolymorphismFactor(aClass);
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
                return false;
            }
            String fileName = file.getName();
            return !fileName.endsWith(".java");
        }

        private void processCouplingFactor(PsiClass psiClass) {
            final DependenciesBuilder dependenciesBuilder = ProjectMetricsRunner.getDependenciesBuilder();
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
                totalMethodsVisibility += getSubclassCount(containingClass);
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
                totalAttributesVisibility += getSubclassCount(containingClass);
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
