package org.b333vv.metric.model.calculator;

import com.intellij.analysis.AnalysisScope;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.util.Query;
import org.b333vv.metric.exec.ProjectMetricsRunner;
import org.b333vv.metric.model.metric.util.ClassUtils;
import org.b333vv.metric.model.builder.DependenciesBuilder;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.util.Bag;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MoodMetricsSetCalculator {
    private final AnalysisScope scope;
    private int numAttributes = 0;
    private int numPublicAttributes = 0;
    private int numClasses = 0;
    private int totalAttributesVisibility = 0;
    private Bag<String> classesPerPackage = new Bag<>();
    private Bag<String> packageVisibleAttributesPerPackage = new Bag<>();
    private Map<PsiClass, Integer> subclassesPerClass = new HashMap<>();

    private int availableFields = 0;
    private int inheritedFields = 0;

    private int totalCoupling = 0;

    private int numMethods = 0;
    private int numPublicMethods = 0;
    private int totalMethodsVisibility = 0;
    private Bag<String> packageVisibleMethodsPerPackage = new Bag<>();

    private int availableMethods = 0;
    private int inheritedMethods = 0;

    private int numOverridingMethods = 0;
    private int numOverridePotentials = 0;

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
        double polymorphismFactor = numOverridePotentials == 0 ? 1.0 :
                (double) numOverridingMethods / (double) numOverridePotentials;

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
        totalMethodsVisibility += numPublicMethods * (numClasses - 1);
        final Set<String> packages = classesPerPackage.getContents();
        for (String aPackage : packages) {
            final int visibleMethods = packageVisibleMethodsPerPackage.getCountForObject(aPackage);
            final int classes = classesPerPackage.getCountForObject(aPackage);
            totalMethodsVisibility += visibleMethods * (classes - 1);
        }
        final int denominator = numMethods * (numClasses - 1);
        final int numerator = denominator - totalMethodsVisibility;
        double methodHidingFactor = (double) numerator / (double) denominator;

        javaProject.addMetric(Metric.of(
                "MHF",
                "Method Hiding Factor",
                "/html/MethodHidingFactor.html",
                methodHidingFactor));
    }

    private void addCouplingFactor(JavaProject javaProject) {
        final int denominator = (numClasses * (numClasses - 1)) / 2;
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
        totalAttributesVisibility += numPublicAttributes * (numClasses - 1);
        final Set<String> packages = classesPerPackage.getContents();
        for (String aPackage : packages) {
            final int visibleAttributes = packageVisibleAttributesPerPackage.getCountForObject(aPackage);
            final int classes = classesPerPackage.getCountForObject(aPackage);
            totalAttributesVisibility += visibleAttributes * (classes - 1);
        }
        final int ahfDenominator = numAttributes * (numClasses - 1);
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
        public void visitFile(PsiFile psiFile) {
            super.visitFile(psiFile);
        }

        @Override
        public void visitClass(PsiClass aClass) {
            super.visitClass(aClass);

            processAttributeInheritanceFactor(aClass);

            processAttributeAndMethodHidingFactor(aClass);

            processCouplingFactor(aClass);

            processMethodInheritanceFactor(aClass);

            processPolymorphismFactor(aClass);
        }

        private void processPolymorphismFactor(PsiClass aClass) {
            int newMethodsCount = 0;
            int overriddenMethodsCount = 0;
            final PsiMethod[] methods = aClass.getMethods();
            for (PsiMethod method : methods) {
                final PsiMethod[] superMethods = method.findSuperMethods();
                if (superMethods.length == 0) {
                    newMethodsCount++;
                } else {
                    overriddenMethodsCount++;
                }
            }
            numOverridePotentials += newMethodsCount * getSubclassCount(aClass);
            numOverridingMethods += overriddenMethodsCount;
        }

        private void processMethodInheritanceFactor(PsiClass aClass) {
            final PsiMethod[] allMethods = aClass.getAllMethods();
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
                if (containingClass != null) {
                    if (containingClass.equals(aClass)) {
                        availableMethods++;
                    } else if (classIsInLibrary(containingClass)) {

                    } else if (!method.hasModifierProperty(PsiModifier.PRIVATE)) {
                        availableMethods++;
                        inheritedMethods++;
                    }
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

        public boolean classIsInLibrary(@NotNull PsiClass aClass) {
            PsiFile file = aClass.getContainingFile();
            if (file == null) {
                return false;
            }
            String fileName = file.getName();
            return !fileName.endsWith(".java");
        }

        private void processCouplingFactor(PsiClass aClass) {
            final DependenciesBuilder dependenciesBuilder = ProjectMetricsRunner.getDependenciesBuilder();
            final Set<PsiClass> dependencies = dependenciesBuilder.getClassesDependencies(aClass);
            totalCoupling += dependencies.stream()
                    .filter(c -> !aClass.isInheritor(c, true))
                    .count();
        }

        private void processAttributeAndMethodHidingFactor(PsiClass aClass) {
            numClasses++;
            final String packageName = ClassUtils.calculatePackageName(aClass);
            classesPerPackage.add(packageName);
        }

        private void processAttributeInheritanceFactor(PsiClass aClass) {
            final PsiField[] allFields = aClass.getAllFields();
            for (PsiField field : allFields) {
                final PsiClass containingClass = field.getContainingClass();
                if (containingClass == null) {
                    continue;
                }
                final String className = containingClass.getName();
                if (containingClass.equals(aClass)) {
                    availableFields++;
                } else if ("java.lang.Object".equals(className)) {

                } else if (!field.hasModifierProperty(PsiModifier.PRIVATE)) {
                    availableFields++;
                    inheritedFields++;
                }
            }
        }

        @Override
        public void visitMethod(PsiMethod method) {
            super.visitMethod(method);
            numMethods++;
            final PsiClass containingClass = method.getContainingClass();

            if (method.hasModifierProperty(PsiModifier.PRIVATE) ||
                    containingClass.hasModifierProperty(PsiModifier.PRIVATE)) {
            } else if (method.hasModifierProperty(PsiModifier.PROTECTED) ||
                    containingClass.hasModifierProperty(PsiModifier.PROTECTED)) {
                totalMethodsVisibility += getSubclassCount(containingClass);
            } else if ((method.hasModifierProperty(PsiModifier.PUBLIC) || containingClass.isInterface()) &&
                    containingClass.hasModifierProperty(PsiModifier.PUBLIC)) {
                numPublicMethods++;
            } else {
                final String packageName = ClassUtils.calculatePackageName(containingClass);
                packageVisibleMethodsPerPackage.add(packageName);
            }
        }

        @Override
        public void visitField(PsiField field) {
            super.visitField(field);
            numAttributes++;
            final PsiClass containingClass = field.getContainingClass();

            if (field.hasModifierProperty(PsiModifier.PRIVATE) ||
                    containingClass.hasModifierProperty(PsiModifier.PRIVATE)) {
            } else if (field.hasModifierProperty(PsiModifier.PROTECTED) ||
                    containingClass.hasModifierProperty(PsiModifier.PROTECTED)) {
                totalAttributesVisibility += getSubclassCount(containingClass);
            } else if ((field.hasModifierProperty(PsiModifier.PUBLIC) || containingClass.isInterface()) &&
                    containingClass.hasModifierProperty(PsiModifier.PUBLIC)) {
                numPublicAttributes++;
            } else {
                final String packageName = ClassUtils.calculatePackageName(containingClass);
                packageVisibleAttributesPerPackage.add(packageName);
            }
        }
    }

    private int getSubclassCount(final PsiClass aClass) {
        if (subclassesPerClass.containsKey(aClass)) {
            return subclassesPerClass.get(aClass);
        }
        int numSubclasses = 0;
        final GlobalSearchScope globalScope = GlobalSearchScope.allScope(scope.getProject());
        final Query<PsiClass> query = ClassInheritorsSearch.search(
                aClass, globalScope, true, true, true);
        for (final PsiClass inheritor : query) {
            if (!inheritor.isInterface()) {
                numSubclasses++;
            }
        }
        subclassesPerClass.put(aClass, numSubclasses);
        return numSubclasses;
    }
}
