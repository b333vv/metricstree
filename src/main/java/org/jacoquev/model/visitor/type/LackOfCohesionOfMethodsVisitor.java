package org.jacoquev.model.visitor.type;

import com.intellij.psi.*;
import org.jacoquev.model.metric.util.ClassUtils;
import org.jacoquev.model.metric.value.Value;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LackOfCohesionOfMethodsVisitor extends JavaClassVisitor {

    @Override
    public void visitClass(PsiClass psiClass) {
        super.visitClass(psiClass);
        metric.setName("LCOM");
        metric.setDescription("Lack Of Cohesion Of Methods");
        metric.setDescriptionUrl("/html/LackOfCohesionOfMethods.html");
        if (ClassUtils.isConcrete(psiClass)) {
            Set<PsiMethod> applicableMethods = getApplicableMethods(psiClass);
            Map<PsiMethod, Set<PsiField>> fieldsPerMethod = calculateFieldUsage(applicableMethods);
            Map<PsiMethod, Set<PsiMethod>> linkedMethods = calculateMethodLinkage(applicableMethods);
            Set<Set<PsiMethod>> components = calculateComponents(applicableMethods,
                    fieldsPerMethod, linkedMethods);
            metric.setValue(Value.of(components.size()));
        }
    }

    @NotNull
    private final Set<String> boilerplateMethods = Set.of("toString", "equals", "hashCode", "finalize",
            "clone", "readObject", "writeObject");

    @NotNull
    public Set<PsiMethod> getApplicableMethods(@NotNull PsiClass aClass) {
        final PsiMethod[] methods = aClass.getMethods();
        final Set<PsiMethod> applicableMethods = new HashSet<>();
        for (PsiMethod method : methods) {
            final String methodName = method.getName();
            if (!method.isConstructor() && !boilerplateMethods.contains(methodName)) {
                applicableMethods.add(method);
            }
        }
        return applicableMethods;
    }

    public Map<PsiMethod, Set<PsiField>> calculateFieldUsage(Set<PsiMethod> applicableMethods) {
        final Map<PsiMethod, Set<PsiField>> fieldsPerMethod = new HashMap<>();
        for (PsiMethod method : applicableMethods) {
            final Set<PsiField> fields = calculateUsedFields(method);
            fieldsPerMethod.put(method, fields);
        }
        return fieldsPerMethod;
    }

    public Set<PsiField> calculateUsedFields(PsiMethod method) {
        final FieldsUsedVisitor visitor = new FieldsUsedVisitor();
        method.accept(visitor);
        return visitor.getFieldsUsed();
    }

    public Map<PsiMethod, Set<PsiMethod>> calculateMethodLinkage(Set<PsiMethod> applicableMethods) {
        final Map<PsiMethod, Set<PsiMethod>> linkages = new HashMap<>();
        for (PsiMethod method : applicableMethods) {
            final Set<PsiMethod> linkedMethods = calculateLinkedMethods(method, applicableMethods);
            linkages.put(method, linkedMethods);
        }
        for (PsiMethod method : applicableMethods) {
            final Set<PsiMethod> linkedMethods = linkages.get(method);
            for (PsiMethod linkedMethod : linkedMethods) {
                linkages.get(linkedMethod).add(method);
            }
        }
        return linkages;
    }

    public Set<PsiMethod> calculateLinkedMethods(PsiMethod method, Set<PsiMethod> applicableMethods) {
        final MethodsUsedVisitor visitor = new MethodsUsedVisitor(applicableMethods);
        method.accept(visitor);
        return visitor.getMethodsUsed();
    }

    private static class FieldsUsedVisitor extends JavaRecursiveElementVisitor {
        private final Set<PsiField> fieldsUsed = new HashSet<>();

        FieldsUsedVisitor() {
        }

        @Override
        public void visitReferenceExpression(PsiReferenceExpression referenceExpression) {
            super.visitReferenceExpression(referenceExpression);
            final PsiElement referent = referenceExpression.resolve();
            if (!(referent instanceof PsiField)) {
                return;
            }
            final PsiField field = (PsiField) referent;
            fieldsUsed.add(field);
        }

        @SuppressWarnings({"ReturnOfCollectionOrArrayField"})
        public Set<PsiField> getFieldsUsed() {
            return fieldsUsed;
        }
    }

    private static class MethodsUsedVisitor extends JavaRecursiveElementVisitor {
        private final Set<PsiMethod> applicableMethods;
        private final Set<PsiMethod> methodsUsed = new HashSet<>();

        MethodsUsedVisitor(Set<PsiMethod> applicableMethods) {
            this.applicableMethods = applicableMethods;
        }

        @Override
        public void visitMethodCallExpression(PsiMethodCallExpression callExpression) {
            super.visitMethodCallExpression(callExpression);
            final PsiMethod testMethod = callExpression.resolveMethod();
            if (applicableMethods.contains(testMethod)) {
                methodsUsed.add(testMethod);
            }
        }

        @SuppressWarnings({"ReturnOfCollectionOrArrayField"})
        public Set<PsiMethod> getMethodsUsed() {
            return methodsUsed;
        }
    }

    public Set<Set<PsiMethod>> calculateComponents(Set<PsiMethod> applicableMethods,
                                                   Map<PsiMethod, Set<PsiField>> fieldsPerMethod,
                                                   Map<PsiMethod, Set<PsiMethod>> linkedMethods) {
        final Set<Set<PsiMethod>> components = new HashSet<>();
        while (applicableMethods.size() > 0) {
            final Set<PsiMethod> component = new HashSet<>();
            final Set<PsiField> fieldsUsed = new HashSet<>();
            final PsiMethod testMethod = applicableMethods.iterator().next();
            applicableMethods.remove(testMethod);
            component.add(testMethod);
            fieldsUsed.addAll(fieldsPerMethod.get(testMethod));
            while (true) {
                final Set<PsiMethod> methodsToAdd = new HashSet<>();
                for (PsiMethod method : applicableMethods) {
                    if (haveIntersection(fieldsPerMethod.get(method), fieldsUsed) ||
                            haveIntersection(linkedMethods.get(method), component)) {
                        methodsToAdd.add(method);
                        fieldsUsed.addAll(fieldsPerMethod.get(method));
                    }
                }
                if (methodsToAdd.size() == 0) {
                    break;
                }
                applicableMethods.removeAll(methodsToAdd);
                component.addAll(methodsToAdd);
            }
            components.add(component);
        }
        return components;
    }

    @Contract("null, _ -> false; _, null -> false")
    public <T> boolean haveIntersection(final Set<T> a, final Set<T> b) {
        if (a == null || b == null) {
            return false;
        }
        final Set<T> intersection  = new HashSet<>(a);
        intersection.retainAll(b);
        return !intersection.isEmpty();
    }
}
