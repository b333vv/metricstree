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

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.b333vv.metric.model.util.Bag;
import org.b333vv.metric.model.util.ClassUtils;
import org.b333vv.metric.model.util.ConcurrentStack;
import org.jetbrains.annotations.Nullable;
import static org.jetbrains.kotlin.asJava.LightClassUtilsKt.toLightClass;
import org.jetbrains.kotlin.psi.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DependenciesBuilder {

    private final Map<PsiClass, Bag<PsiClass>> classesDependencies = new ConcurrentHashMap<>();
    private final Map<PsiClass, Bag<PsiClass>> classesDependents = new ConcurrentHashMap<>();
    private final Map<PsiClass, Bag<PsiPackage>> packagesDependencies = new ConcurrentHashMap<>();
    private final Map<PsiClass, Bag<PsiPackage>> packagesDependents = new ConcurrentHashMap<>();

    // Track unresolved type dependencies (e.g., standard library classes)
    private final Map<PsiClass, Set<String>> unresolvedDependencies = new ConcurrentHashMap<>();

    public void build(PsiElement psiElement) {
        psiElement.accept(new PsiRecursiveElementVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                if (element instanceof PsiJavaFile) {
                    element.accept(new DependenciesVisitor());
                } else if (element instanceof KtFile) {
                    element.accept(new KotlinDependenciesVisitor());
                } else {
                    super.visitElement(element);
                }
            }
        });
    }

    public Set<PsiClass> getClassesDependents(PsiClass psiClass) {
        Optional<Bag<PsiClass>> classesDependentsForClass = Optional.ofNullable(classesDependents.get(psiClass));
        return classesDependentsForClass
                .map(Bag::getContents)
                .orElse(Collections.emptySet());
    }

    public Set<PsiClass> getDependentsSet(PsiClass psiClass, PsiPackage psiPackage) {
        if (classesDependents.get(psiClass) == null) {
            return Set.of();
        }
        return classesDependents.get(psiClass).getContents()
                .stream()
                .filter(c -> !Objects.equals(ClassUtils.findPackage(c), psiPackage))
                .collect(Collectors.toSet());
    }

    public Set<PsiClass> getClassesDependencies(PsiClass psiClass) {
        Optional<Bag<PsiClass>> classesDependenciesForClass = Optional.ofNullable(classesDependencies.get(psiClass));
        return classesDependenciesForClass
                .map(Bag::getContents)
                .orElse(Collections.emptySet());
    }

    public Set<PsiPackage> getPackagesDependencies(PsiClass psiClass) {
        Optional<Bag<PsiPackage>> packagesDependenciesForClass = Optional
                .ofNullable(packagesDependencies.get(psiClass));
        return packagesDependenciesForClass
                .map(Bag::getContents)
                .orElse(Collections.emptySet());
    }

    public int getTotalCouplingCount(PsiClass psiClass) {
        Set<PsiClass> dependencies = getClassesDependencies(psiClass);
        Set<PsiClass> dependents = getClassesDependents(psiClass);
        Set<PsiClass> union = new HashSet<>(dependencies);
        union.addAll(dependents);

        // Add count of unresolved dependencies (standard library classes)
        Set<String> unresolvedDeps = unresolvedDependencies.getOrDefault(psiClass, Collections.emptySet());

        return union.size() + unresolvedDeps.size();
    }

    private <K, V> void add(K k, V v, Map<K, Bag<V>> map) {
        map.computeIfAbsent(k, (unused) -> new Bag<>()).add(v);
    }

    private void addDependencyForClass(PsiClass currentClass, PsiClass referencedClass) {
        if (currentClass == null || referencedClass == null || referencedClass.equals(currentClass)) {
            return;
        }

        if (referencedClass instanceof PsiAnonymousClass || referencedClass instanceof PsiTypeParameter) {
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

    private void addUnresolvedTypeDependency(PsiClass currentClass, PsiClassType classType) {
        if (currentClass == null) {
            return;
        }

        String typeName = classType.getCanonicalText();

        if (typeName == null || typeName.contains("<") || typeName.contains("[")) {
            if (typeName != null && typeName.contains("<")) {
                int genericStart = typeName.indexOf('<');
                typeName = typeName.substring(0, genericStart);
            }
        }

        if (typeName != null && !typeName.isEmpty()) {
            unresolvedDependencies.computeIfAbsent(currentClass, k -> new HashSet<>()).add(typeName);
        }
    }

    private void addDependencyForType(PsiClass currentClass, @Nullable PsiType psiType) {
        if (psiType == null) {
            return;
        }
        final PsiType baseType = psiType.getDeepComponentType();
        if (!(baseType instanceof PsiClassType)) {
            if (baseType instanceof PsiWildcardType) {
                final PsiWildcardType wildcardType = (PsiWildcardType) baseType;
                addDependencyForType(currentClass, wildcardType.getBound());
            }
            return;
        }
        final PsiClassType classType = (PsiClassType) baseType;
        addDependencyForTypes(currentClass, classType.getParameters());

        PsiClass resolvedClass = classType.resolve();
        if (resolvedClass != null) {
            addDependencyForClass(currentClass, resolvedClass);
        } else {
            addUnresolvedTypeDependency(currentClass, classType);
        }
    }

    private void addDependencyForTypes(PsiClass currentClass, PsiType[] psiTypes) {
        for (PsiType type : psiTypes) {
            addDependencyForType(currentClass, type);
        }
    }

    private void addDependencyForTypeParameters(PsiClass currentClass, PsiTypeParameter[] psiTypeParameters) {
        for (PsiTypeParameter parameter : psiTypeParameters) {
            final PsiReferenceList extendsList = parameter.getExtendsList();
            addDependencyForTypes(currentClass, extendsList.getReferencedTypes());
        }
    }

    private class DependenciesVisitor extends JavaRecursiveElementVisitor {

        private final ConcurrentStack<PsiClass> classStack = new ConcurrentStack<>();
        private PsiClass currentClass = null;

        @Override
        public void visitClass(PsiClass psiClass) {
            if (!ClassUtils.isAnonymous(psiClass)) {
                classStack.push(currentClass);
                currentClass = psiClass;
                addDependencyForTypes(currentClass, psiClass.getSuperTypes());
                addDependencyForTypeParameters(currentClass, psiClass.getTypeParameters());
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
            addDependencyForClass(currentClass, method.getContainingClass());
            addDependencyForTypes(currentClass, psiMethodCallExpression.getTypeArguments());
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
                addDependencyForClass(currentClass, field.getContainingClass());
            } else if (element instanceof PsiClass) {
                addDependencyForClass(currentClass, (PsiClass) element);
            }
        }

        @Override
        public void visitMethod(PsiMethod psiMethod) {
            super.visitMethod(psiMethod);
            addDependencyForType(currentClass, psiMethod.getReturnType());
            addDependencyForTypeParameters(currentClass, psiMethod.getTypeParameters());
            final PsiReferenceList throwsList = psiMethod.getThrowsList();
            addDependencyForTypes(currentClass, throwsList.getReferencedTypes());
        }

        @Override
        public void visitNewExpression(PsiNewExpression psiNewExpression) {
            super.visitNewExpression(psiNewExpression);
            addDependencyForType(currentClass, psiNewExpression.getType());
            addDependencyForTypes(currentClass, psiNewExpression.getTypeArguments());
        }

        @Override
        public void visitVariable(PsiVariable psiVariable) {
            super.visitVariable(psiVariable);
            addDependencyForType(currentClass, psiVariable.getType());
        }

        @Override
        public void visitClassObjectAccessExpression(PsiClassObjectAccessExpression psiClassObjectAccessExpression) {
            super.visitClassObjectAccessExpression(psiClassObjectAccessExpression);
            final PsiTypeElement operand = psiClassObjectAccessExpression.getOperand();
            addDependencyForType(currentClass, operand.getType());
        }

        @Override
        public void visitInstanceOfExpression(PsiInstanceOfExpression psiInstanceOfExpression) {
            super.visitInstanceOfExpression(psiInstanceOfExpression);
            final PsiTypeElement checkType = psiInstanceOfExpression.getCheckType();
            if (checkType == null) {
                return;
            }
            addDependencyForType(currentClass, checkType.getType());
        }

        @Override
        public void visitTypeCastExpression(PsiTypeCastExpression psiTypeCastExpression) {
            super.visitTypeCastExpression(psiTypeCastExpression);
            final PsiTypeElement castType = psiTypeCastExpression.getCastType();
            if (castType == null) {
                return;
            }
            addDependencyForType(currentClass, castType.getType());
        }

        @Override
        public void visitLambdaExpression(PsiLambdaExpression psiLambdaExpression) {
            super.visitLambdaExpression(psiLambdaExpression);
            addDependencyForType(currentClass, psiLambdaExpression.getFunctionalInterfaceType());
        }
    }

    private class KotlinDependenciesVisitor extends KtTreeVisitorVoid {
        private final ConcurrentStack<PsiClass> classStack = new ConcurrentStack<>();
        private PsiClass currentClass = null;

        @Override
        public void visitClass(KtClass ktClass) {
            handleClass(ktClass, () -> super.visitClass(ktClass));
        }

        @Override
        public void visitObjectDeclaration(KtObjectDeclaration declaration) {
            handleClass(declaration, () -> super.visitObjectDeclaration(declaration));
        }

        private void handleClass(KtClassOrObject classOrObject, Runnable superCall) {
            PsiClass psiClass = toLightClass(classOrObject);
            if (psiClass != null && !ClassUtils.isAnonymous(psiClass)) {
                classStack.push(currentClass);
                currentClass = psiClass;
                addDependencyForTypes(currentClass, psiClass.getSuperTypes());
                addDependencyForTypeParameters(currentClass, psiClass.getTypeParameters());
            }

            superCall.run();

            if (psiClass != null && !ClassUtils.isAnonymous(psiClass)) {
                currentClass = classStack.pop();
            }
        }

        @Override
        public void visitReferenceExpression(KtReferenceExpression expression) {
            super.visitReferenceExpression(expression);
            PsiElement resolved = resolveReference(expression);
            if (resolved != null) {
                addDependency(resolved);
            }
        }

        @Override
        public void visitNamedFunction(KtNamedFunction function) {
            super.visitNamedFunction(function);

            // Handle return type
            KtTypeReference returnTypeRef = function.getTypeReference();
            if (returnTypeRef != null) {
                handleTypeReference(returnTypeRef);
            }

            // Handle parameter types
            for (KtParameter parameter : function.getValueParameters()) {
                KtTypeReference paramTypeRef = parameter.getTypeReference();
                if (paramTypeRef != null) {
                    handleTypeReference(paramTypeRef);
                }
            }

            // Handle type parameters
            for (KtTypeParameter typeParameter : function.getTypeParameters()) {
                KtTypeReference extendsBound = typeParameter.getExtendsBound();
                if (extendsBound != null) {
                    handleTypeReference(extendsBound);
                }
            }
        }

        @Override
        public void visitProperty(KtProperty property) {
            super.visitProperty(property);

            // Handle property type
            KtTypeReference typeRef = property.getTypeReference();
            if (typeRef != null) {
                handleTypeReference(typeRef);
            }
        }

        @Override
        public void visitCallExpression(KtCallExpression expression) {
            super.visitCallExpression(expression);

            // Resolve the called function/constructor
            KtExpression calleeExpression = expression.getCalleeExpression();
            if (calleeExpression != null) {
                PsiElement resolved = resolveReference(calleeExpression);
                if (resolved != null) {
                    addDependency(resolved);
                }
            }

            // Handle type arguments
            for (KtTypeProjection typeArg : expression.getTypeArguments()) {
                KtTypeReference typeRef = typeArg.getTypeReference();
                if (typeRef != null) {
                    handleTypeReference(typeRef);
                }
            }
        }

        @Override
        public void visitTypeReference(KtTypeReference typeReference) {
            super.visitTypeReference(typeReference);
            handleTypeReference(typeReference);
        }

        @Override
        public void visitBinaryWithTypeRHSExpression(KtBinaryExpressionWithTypeRHS expression) {
            super.visitBinaryWithTypeRHSExpression(expression);

            // Handle 'as' and 'as?' type casts
            KtTypeReference typeRef = expression.getRight();
            if (typeRef != null) {
                handleTypeReference(typeRef);
            }
        }

        @Override
        public void visitIsExpression(KtIsExpression expression) {
            super.visitIsExpression(expression);

            // Handle 'is' and '!is' type checks
            KtTypeReference typeRef = expression.getTypeReference();
            if (typeRef != null) {
                handleTypeReference(typeRef);
            }
        }

        private void handleTypeReference(KtTypeReference typeReference) {
            if (typeReference == null) {
                return;
            }

            // Handle the referenced class directly via the type element
            KtTypeElement typeElement = typeReference.getTypeElement();
            if (typeElement instanceof KtUserType) {
                handleUserType((KtUserType) typeElement);
            }
        }

        private void handleUserType(KtUserType userType) {
            if (userType == null) {
                return;
            }

            // Resolve the type reference
            KtSimpleNameExpression referenceExpression = userType.getReferenceExpression();
            if (referenceExpression != null) {
                PsiElement resolved = resolveReference(referenceExpression);
                if (resolved != null) {
                    addDependency(resolved);
                }
            }

            // Handle type arguments recursively
            for (KtTypeProjection typeArg : userType.getTypeArguments()) {
                KtTypeReference typeArgRef = typeArg.getTypeReference();
                if (typeArgRef != null) {
                    handleTypeReference(typeArgRef);
                }
            }

            // Handle qualified types (e.g., Outer.Inner)
            KtUserType qualifier = userType.getQualifier();
            if (qualifier != null) {
                handleUserType(qualifier);
            }
        }

        private PsiElement resolveReference(PsiElement element) {
            if (element instanceof KtReferenceExpression) {
                KtReferenceExpression refExpr = (KtReferenceExpression) element;
                // Get references and resolve the first one
                PsiReference[] references = refExpr.getReferences();
                if (references.length > 0) {
                    return references[0].resolve();
                }
            }
            return null;
        }

        private void addDependency(PsiElement resolved) {
            if (resolved == null) {
                return;
            }

            if (resolved instanceof PsiClass) {
                addDependencyForClass(currentClass, (PsiClass) resolved);
            } else if (resolved instanceof KtClassOrObject) {
                PsiClass psiClass = toLightClass((KtClassOrObject) resolved);
                if (psiClass != null) {
                    addDependencyForClass(currentClass, psiClass);
                }
            } else if (resolved instanceof PsiMember) {
                addDependencyForClass(currentClass, ((PsiMember) resolved).getContainingClass());
            } else if (resolved instanceof KtDeclaration) {
                KtClassOrObject containingClass = PsiTreeUtil.getParentOfType(resolved, KtClassOrObject.class);
                if (containingClass != null) {
                    PsiClass psiClass = toLightClass(containingClass);
                    if (psiClass != null) {
                        addDependencyForClass(currentClass, psiClass);
                    }
                }
            }
        }
    }
}
