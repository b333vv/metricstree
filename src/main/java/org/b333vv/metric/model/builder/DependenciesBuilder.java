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

package org.b333vv.metric.model.builder;

import com.intellij.psi.*;
import org.b333vv.metric.model.metric.util.Bag;
import org.b333vv.metric.model.metric.util.ClassUtils;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class DependenciesBuilder {

    private final Map<PsiClass, Bag<PsiClass>> classesDependencies = new HashMap<>();
    private final Map<PsiClass, Bag<PsiClass>> classesDependents = new HashMap<>();
    private final Map<PsiClass, Bag<PsiPackage>> packagesDependencies = new HashMap<>();
    private final Map<PsiClass, Bag<PsiPackage>> packagesDependents = new HashMap<>();

    public void build(PsiElement psiElement) {
        final DependenciesVisitor visitor = new DependenciesVisitor();
        psiElement.accept(visitor);
    }

    public Set<PsiClass> getClassesDependents(PsiClass psiClass) {
        Optional<Bag<PsiClass>> classesDependentsForClass = Optional.ofNullable(classesDependents.get(psiClass));
        return classesDependentsForClass
                .map(Bag::getContents)
                .orElse(Collections.emptySet());
    }

    public Set<PsiPackage> getPackagesDependents(PsiClass psiClass) {
        Optional<Bag<PsiPackage>> packagesDependentsForClass = Optional.ofNullable(packagesDependents.get(psiClass));
        return packagesDependentsForClass
                .map(Bag::getContents)
                .orElse(Collections.emptySet());
    }

    public Set<PsiClass> getDependentsSet(PsiClass psiClass, PsiPackage psiPackage) {
        if (classesDependents.get(psiClass) == null) {
            return Set.of();
        }
        return classesDependents.get(psiClass).getContents()
                .stream()
                .filter(c -> !ClassUtils.findPackage(c).equals(psiPackage))
                .collect(Collectors.toSet());
    }

    public Set<PsiClass> getClassesDependencies(PsiClass psiClass) {
        Optional<Bag<PsiClass>> classesDependenciesForClass = Optional.ofNullable(classesDependencies.get(psiClass));
        return classesDependenciesForClass
                .map(Bag::getContents)
                .orElse(Collections.emptySet());
    }

    public Set<PsiPackage> getPackagesDependencies(PsiClass psiClass) {
        Optional<Bag<PsiPackage>> packagesDependenciesForClass = Optional.ofNullable(packagesDependencies.get(psiClass));
        return packagesDependenciesForClass
                .map(Bag::getContents)
                .orElse(Collections.emptySet());
    }

    private class DependenciesVisitor extends JavaRecursiveElementVisitor {

        private final Stack<PsiClass> classStack = new Stack<>();
        private PsiClass currentClass = null;

        @Override
        public void visitClass(PsiClass psiClass) {
            if (!ClassUtils.isAnonymous(psiClass)) {
                classStack.push(currentClass);
                currentClass = psiClass;
                addDependencyForTypes(psiClass.getSuperTypes());
                addDependencyForTypeParameters(psiClass.getTypeParameters());
            }
            super.visitClass(psiClass);
            if (!ClassUtils.isAnonymous(psiClass)) {
                currentClass = classStack.pop();
            }
        }

        @Override
        public void visitMethodCallExpression(PsiMethodCallExpression psiMethodCallExpression) {
            super.visitMethodCallExpression(psiMethodCallExpression);
            final PsiMethod method = psiMethodCallExpression.resolveMethod();
            if (method == null) {
                return;
            }
            addDependencyForClass(method.getContainingClass());
            addDependencyForTypes(psiMethodCallExpression.getTypeArguments());
        }

        @Override
        public void visitReferenceExpression(PsiReferenceExpression psiReferenceExpression) {
            super.visitReferenceExpression(psiReferenceExpression);
            final PsiElement element = psiReferenceExpression.resolve();
            if (element == null) {
                return;
            }
            if (element instanceof PsiField) {
                final PsiField field = (PsiField) element;
                addDependencyForClass(field.getContainingClass());
            } else if (element instanceof PsiClass) {
                addDependencyForClass((PsiClass) element);
            }
        }

        @Override
        public void visitMethod(PsiMethod psiMethod) {
            super.visitMethod(psiMethod);
            addDependencyForType(psiMethod.getReturnType());
            addDependencyForTypeParameters(psiMethod.getTypeParameters());
            final PsiReferenceList throwsList = psiMethod.getThrowsList();
            addDependencyForTypes(throwsList.getReferencedTypes());
        }

        @Override
        public void visitNewExpression(PsiNewExpression psiNewExpression) {
            super.visitNewExpression(psiNewExpression);
            addDependencyForType(psiNewExpression.getType());
            addDependencyForTypes(psiNewExpression.getTypeArguments());
        }

        @Override
        public void visitVariable(PsiVariable psiVariable) {
            super.visitVariable(psiVariable);
            addDependencyForType(psiVariable.getType());
        }

        @Override
        public void visitClassObjectAccessExpression(PsiClassObjectAccessExpression psiClassObjectAccessExpression) {
            super.visitClassObjectAccessExpression(psiClassObjectAccessExpression);
            final PsiTypeElement operand = psiClassObjectAccessExpression.getOperand();
            addDependencyForType(operand.getType());
        }

        @Override
        public void visitInstanceOfExpression(PsiInstanceOfExpression psiInstanceOfExpression) {
            super.visitInstanceOfExpression(psiInstanceOfExpression);
            final PsiTypeElement checkType = psiInstanceOfExpression.getCheckType();
            if (checkType == null) {
                return;
            }
            addDependencyForType(checkType.getType());
        }

        @Override
        public void visitTypeCastExpression(PsiTypeCastExpression psiTypeCastExpression) {
            super.visitTypeCastExpression(psiTypeCastExpression);
            final PsiTypeElement castType = psiTypeCastExpression.getCastType();
            if (castType == null) {
                return;
            }
            addDependencyForType(castType.getType());
        }

        @Override
        public void visitLambdaExpression(PsiLambdaExpression psiLambdaExpression) {
            super.visitLambdaExpression(psiLambdaExpression);
            addDependencyForType(psiLambdaExpression.getFunctionalInterfaceType());
        }

        private void addDependencyForTypeParameters(PsiTypeParameter[] psiTypeParameters) {
            for (PsiTypeParameter parameter : psiTypeParameters) {
                final PsiReferenceList extendsList = parameter.getExtendsList();
                addDependencyForTypes(extendsList.getReferencedTypes());
            }
        }

        private void addDependencyForTypes(PsiType[] psiTypes) {
            for (PsiType type : psiTypes) {
                addDependencyForType(type);
            }
        }

        private void addDependencyForType(@Nullable PsiType psiType) {
            if (psiType == null) {
                return;
            }
            final PsiType baseType = psiType.getDeepComponentType();
            if (!(baseType instanceof PsiClassType)) {
                if (baseType instanceof PsiWildcardType) {
                    final PsiWildcardType wildcardType = (PsiWildcardType) baseType;
                    addDependencyForType(wildcardType.getBound());
                }
                return;
            }
            final PsiClassType classType = (PsiClassType) baseType;
            addDependencyForTypes(classType.getParameters());
            addDependencyForClass(classType.resolve());
        }

        private void addDependencyForClass(PsiClass referencedClass) {
            if (currentClass == null || referencedClass == null || referencedClass.equals(currentClass)) {
                return;
            }
            if (referencedClass instanceof PsiCompiledElement || referencedClass instanceof PsiAnonymousClass ||
                    referencedClass instanceof PsiTypeParameter) {
                return;
            }
            add(currentClass, referencedClass, classesDependencies);
            add(referencedClass, currentClass, classesDependents);

            final PsiPackage dependencyPackage = ClassUtils.findPackage(referencedClass);
            if (dependencyPackage != null) {
                add(currentClass, dependencyPackage, packagesDependencies);
            }

            final PsiPackage aPackage = ClassUtils.findPackage(currentClass);
            if (aPackage != null) {
                add(referencedClass, aPackage, packagesDependents);
            }
        }

        private <K, V> void add(K k, V v, Map<K, Bag<V>> map) {
            Bag<V> bag = map.get(k);
            if (bag == null) {
                bag = new Bag<>();
                map.put(k, bag);
            }
            bag.add(v);
        }
    }
}
