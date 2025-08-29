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
import org.b333vv.metric.model.util.CommonUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class CohesionUtils {
    private CohesionUtils() {
    }

    private static final Set<String> boilerplateMethods = Set.of("toString", "equals", "hashCode", "finalize", "clone", "readObject",
            "writeObject");

    public static Set<String> getBoilerplateMethods() {
        return boilerplateMethods;
    }

    @NotNull
    public static Set<PsiMethod> getApplicableMethods(@NotNull final PsiClass aClass) {
        final PsiMethod[] methods = aClass.getMethods();
        return getPsiMethods(methods);
    }

    @NotNull
    public static Set<PsiMethod> getApplicableMethodsWithInherited(@NotNull final PsiClass aClass) {
        final PsiMethod[] methods = aClass.getAllMethods();
        return getPsiMethods(methods);
    }

    @NotNull
    private static Set<PsiMethod> getPsiMethods(PsiMethod[] methods) {
        final Set<PsiMethod> applicableMethods = new HashSet<>();
        for (PsiMethod method : methods) {
            // Only include non-static, non-constructor methods that are not boilerplate
            if (!method.isConstructor() && 
                !method.hasModifierProperty(PsiModifier.STATIC) && 
                !boilerplateMethods.contains(method.getName())) {
                applicableMethods.add(method);
            }
        }
        return applicableMethods;
    }

    public static Set<Set<PsiMethod>> calculateComponents(Set<PsiMethod> applicableMethods,
                                                          Map<PsiMethod, Set<PsiField>> fieldsPerMethod,
                                                          Map<PsiMethod, Set<PsiMethod>> linkedMethods) {
        final Set<Set<PsiMethod>> components = new HashSet<>();
        
        // Filter out methods that don't use any fields - they don't contribute to LCOM
        final Set<PsiMethod> methodsUsingFields = new HashSet<>();
        for (PsiMethod method : applicableMethods) {
            if (!fieldsPerMethod.get(method).isEmpty()) {
                methodsUsingFields.add(method);
            }
        }
        
        // If no methods use fields, LCOM should be 0
        if (methodsUsingFields.isEmpty()) {
            return components; // Empty set means 0 components
        }
        
        final Set<PsiMethod> unvisited = new HashSet<>(methodsUsingFields);
        while (!unvisited.isEmpty()) {
            final Set<PsiMethod> component = new HashSet<>();
            final Queue<PsiMethod> queue = new LinkedList<>();
            PsiMethod start = unvisited.iterator().next();
            queue.add(start);
            unvisited.remove(start);
            while (!queue.isEmpty()) {
                PsiMethod current = queue.poll();
                component.add(current);
                // Find methods connected through shared field usage or method calls
                for (PsiMethod method : methodsUsingFields) {
                    if (unvisited.contains(method)) {
                        if (CommonUtils.haveIntersection(fieldsPerMethod.get(method), fieldsPerMethod.get(current)) ||
                            CommonUtils.haveIntersection(linkedMethods.get(method), component)) {
                            queue.add(method);
                            unvisited.remove(method);
                        }
                    }
                }
            }
            components.add(component);
        }
        return components;
    }

    public static Map<PsiMethod, Set<PsiField>> calculateFieldUsage(Set<PsiMethod> applicableMethods) {
        final Map<PsiMethod, Set<PsiField>> fieldsPerMethod = new HashMap<>();
        for (PsiMethod method : applicableMethods) {
            final Set<PsiField> fields = calculateUsedFields(method);
            fieldsPerMethod.put(method, fields);
        }
        return fieldsPerMethod;
    }

    public static int calculateConnectedMethods(@NotNull final Set<PsiMethod> applicableMethods) {
        final PsiMethod[] methods = applicableMethods.toArray(new PsiMethod[0]);
        final Map<PsiMethod, Set<PsiField>> fieldUsage = calculateFieldUsage(applicableMethods);
        int connectedPairs = 0;
        for (int i = 0; i < methods.length; i++) {
            for (int j = i + 1; j < methods.length; j++) {
                if (CommonUtils.haveIntersection(fieldUsage.get(methods[i]), fieldUsage.get(methods[j]))) {
                    connectedPairs++;
                }
            }
        }
        return connectedPairs;
    }

    public static Map<PsiField, Set<PsiMethod>> calculateFieldToMethodUsage(@NotNull final Set<PsiField> applicableFields,
                                                                            @NotNull final Set<PsiMethod> applicableMethods) {
        final Map<PsiField, Set<PsiMethod>> methodsPerField = new HashMap<>();
        for (final PsiField field : applicableFields) {
            methodsPerField.put(field, new HashSet<>());
        }
        for (PsiMethod method : applicableMethods) {
            final Set<PsiField> fields = calculateUsedFields(method);
            for (final PsiField field : fields) {
                if (applicableFields.contains(field)) {
                    methodsPerField.get(field).add(method);
                }
            }
        }
        return methodsPerField;
    }

    public static Set<PsiField> calculateUsedFields(PsiMethod method) {
        try {
            final FieldsUsedVisitor visitor = new FieldsUsedVisitor();
            method.accept(visitor);
            return visitor.getFieldsUsed();
        } catch (Exception e) {
            // Return empty set if there's an issue with PSI traversal
            return new HashSet<>();
        }
    }



    public static Map<PsiMethod, Set<PsiMethod>> calculateMethodLinkage(Set<PsiMethod> applicableMethods) {
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

    public static Set<PsiMethod> calculateLinkedMethods(PsiMethod method, Set<PsiMethod> applicableMethods) {
        try {
            final MethodsUsedVisitor visitor = new MethodsUsedVisitor(applicableMethods);
            method.accept(visitor);
            return visitor.getMethodsUsed();
        } catch (Exception e) {
            // Return empty set if there's an issue with PSI traversal
            return new HashSet<>();
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
            try {
                final PsiMethod testMethod = callExpression.resolveMethod();
                if (applicableMethods.contains(testMethod)) {
                    methodsUsed.add(testMethod);
                }
                // Continue visiting children
                super.visitMethodCallExpression(callExpression);
            } catch (Exception e) {
                // Skip this method call if there's an issue with PSI traversal
                // This can happen with malformed PSI trees or during indexing
            }
        }

        public Set<PsiMethod> getMethodsUsed() {
            return methodsUsed;
        }
    }

    private static class FieldsUsedVisitor extends JavaRecursiveElementVisitor {
        private final Set<PsiField> fieldsUsed = new HashSet<>();

        FieldsUsedVisitor() {
        }

        @Override
        public void visitReferenceExpression(PsiReferenceExpression referenceExpression) {
            try {
                final PsiElement referent = referenceExpression.resolve();
                if (referent instanceof PsiField) {
                    final PsiField field = (PsiField) referent;
                    // Only include non-static instance fields
                    if (!field.hasModifierProperty(PsiModifier.STATIC)) {
                        fieldsUsed.add(field);
                    }
                }
                // Continue visiting children
                super.visitReferenceExpression(referenceExpression);
            } catch (Exception e) {
                // Skip this reference expression if there's an issue with PSI traversal
                // This can happen with malformed PSI trees or during indexing
            }
        }

        public Set<PsiField> getFieldsUsed() {
            return fieldsUsed;
        }
    }
}
