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

package org.b333vv.metric.model.visitor.type;

import com.intellij.psi.*;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.util.ClassUtils;
import org.b333vv.metric.model.metric.value.Value;
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
        metric = Metric.of("LCOM", "Lack Of Cohesion Of Methods",
                "/html/LackOfCohesionOfMethods.html", Value.UNDEFINED);
        if (ClassUtils.isConcrete(psiClass)) {
            Set<PsiMethod> applicableMethods = getApplicableMethods(psiClass);
            Map<PsiMethod, Set<PsiField>> fieldsPerMethod = calculateFieldUsage(applicableMethods);
            Map<PsiMethod, Set<PsiMethod>> linkedMethods = calculateMethodLinkage(applicableMethods);
            Set<Set<PsiMethod>> components = calculateComponents(applicableMethods,
                    fieldsPerMethod, linkedMethods);
            metric = Metric.of("LCOM", "Lack Of Cohesion Of Methods",
                    "/html/LackOfCohesionOfMethods.html", components.size());
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

        public Set<PsiMethod> getMethodsUsed() {
            return methodsUsed;
        }
    }

    public Set<Set<PsiMethod>> calculateComponents(Set<PsiMethod> applicableMethods,
                                                   Map<PsiMethod, Set<PsiField>> fieldsPerMethod,
                                                   Map<PsiMethod, Set<PsiMethod>> linkedMethods) {
        final Set<Set<PsiMethod>> components = new HashSet<>();
        while (!applicableMethods.isEmpty()) {
            final Set<PsiMethod> component = new HashSet<>();
            final PsiMethod testMethod = applicableMethods.iterator().next();
            applicableMethods.remove(testMethod);
            component.add(testMethod);
            final Set<PsiField> fieldsUsed = new HashSet<>(fieldsPerMethod.get(testMethod));
            while (true) {
                final Set<PsiMethod> methodsToAdd = new HashSet<>();
                for (PsiMethod method : applicableMethods) {
                    if (haveIntersection(fieldsPerMethod.get(method), fieldsUsed) ||
                            haveIntersection(linkedMethods.get(method), component)) {
                        methodsToAdd.add(method);
                        fieldsUsed.addAll(fieldsPerMethod.get(method));
                    }
                }
                if (methodsToAdd.isEmpty()) {
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