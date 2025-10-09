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
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.model.util.ClassUtils;

import java.util.HashSet;
import java.util.Set;

import static org.b333vv.metric.model.metric.MetricType.CBO;

public class CouplingBetweenObjectsVisitor extends JavaClassVisitor {
    @Override
    public void visitClass(PsiClass psiClass) {
        super.visitClass(psiClass);
        metric = Metric.of(CBO, Value.UNDEFINED);
        if (shouldProcess(psiClass)) {
            Set<String> coupledClasses = new HashSet<>();

            addSuperTypes(psiClass, coupledClasses);
            addImports(psiClass, coupledClasses);

            psiClass.accept(new JavaRecursiveElementVisitor() {
                @Override
                public void visitTypeElement(PsiTypeElement typeElement) {
                    super.visitTypeElement(typeElement);
                    addTypeReference(typeElement.getType(), psiClass, coupledClasses);
                }

                @Override
                public void visitAnnotation(PsiAnnotation annotation) {
                    super.visitAnnotation(annotation);
                    PsiJavaCodeReferenceElement referenceElement = annotation.getNameReferenceElement();
                    if (referenceElement != null) {
                        PsiElement resolved = referenceElement.resolve();
                        if (resolved instanceof PsiClass) {
                            addResolvedClass((PsiClass) resolved, psiClass, coupledClasses);
                        } else {
                            addQualifiedName(referenceElement.getQualifiedName(), psiClass, coupledClasses);
                        }
                    }
                }

                @Override
                public void visitReferenceElement(PsiJavaCodeReferenceElement referenceElement) {
                    super.visitReferenceElement(referenceElement);
                    PsiElement resolved = referenceElement.resolve();
                    if (resolved instanceof PsiClass) {
                        addResolvedClass((PsiClass) resolved, psiClass, coupledClasses);
                    }
                }

                @Override
                public void visitMethodCallExpression(PsiMethodCallExpression methodCallExpression) {
                    super.visitMethodCallExpression(methodCallExpression);
                    PsiMethod resolvedMethod = methodCallExpression.resolveMethod();
                    if (resolvedMethod != null) {
                        addResolvedClass(resolvedMethod.getContainingClass(), psiClass, coupledClasses);
                    } else {
                        PsiExpression qualifierExpression = methodCallExpression.getMethodExpression().getQualifierExpression();
                        if (qualifierExpression instanceof PsiReferenceExpression) {
                            PsiElement resolvedQualifier = ((PsiReferenceExpression) qualifierExpression).resolve();
                            if (resolvedQualifier instanceof PsiClass) {
                                addResolvedClass((PsiClass) resolvedQualifier, psiClass, coupledClasses);
                            } else {
                                String inferred = inferTypeFromStaticCall(((PsiReferenceExpression) qualifierExpression).getReferenceName());
                                addQualifiedName(inferred, psiClass, coupledClasses);
                            }
                        } else if (qualifierExpression != null) {
                            PsiType qualifierType = qualifierExpression.getType();
                            if (qualifierType instanceof PsiClassType) {
                                addResolvedClass(((PsiClassType) qualifierType).resolve(), psiClass, coupledClasses);
                            }
                            String inferred = inferTypeFromStaticCall(qualifierExpression.getText());
                            addQualifiedName(inferred, psiClass, coupledClasses);
                        }
                    }
                }

                @Override
                public void visitMethodReferenceExpression(PsiMethodReferenceExpression expression) {
                    super.visitMethodReferenceExpression(expression);
                    PsiElement resolved = expression.resolve();
                    if (resolved instanceof PsiMethod) {
                        addResolvedClass(((PsiMethod) resolved).getContainingClass(), psiClass, coupledClasses);
                        return;
                    }
                    PsiElement qualifier = expression.getQualifier();
                    if (qualifier instanceof PsiReferenceExpression) {
                        PsiElement resolvedQualifier = ((PsiReferenceExpression) qualifier).resolve();
                        if (resolvedQualifier instanceof PsiClass) {
                            addResolvedClass((PsiClass) resolvedQualifier, psiClass, coupledClasses);
                        } else {
                            String inferred = inferTypeFromStaticCall(((PsiReferenceExpression) qualifier).getReferenceName());
                            addQualifiedName(inferred, psiClass, coupledClasses);
                        }
                    }
                }

                @Override
                public void visitNewExpression(PsiNewExpression newExpression) {
                    super.visitNewExpression(newExpression);
                    addTypeReference(newExpression.getType(), psiClass, coupledClasses);
                }
            });

            metric = Metric.of(CBO, coupledClasses.size());
        }
    }

    private boolean shouldProcess(PsiClass psiClass) {
        return !psiClass.isEnum() && !psiClass.isAnnotationType() && !ClassUtils.isAnonymous(psiClass);
    }

    private void addSuperTypes(PsiClass psiClass, Set<String> coupledClasses) {
        for (PsiType superType : psiClass.getSuperTypes()) {
            addTypeReference(superType, psiClass, coupledClasses);
        }
    }

    private void addImports(PsiClass psiClass, Set<String> coupledClasses) {
        PsiFile containingFile = psiClass.getContainingFile();
        if (!(containingFile instanceof PsiJavaFile)) {
            return;
        }

        PsiImportList importList = ((PsiJavaFile) containingFile).getImportList();
        if (importList == null) {
            return;
        }

        for (PsiImportStatementBase importStatement : importList.getAllImportStatements()) {
            if (importStatement.isOnDemand()) {
                continue;
            }
            PsiJavaCodeReferenceElement importReference = importStatement.getImportReference();
            if (importReference == null) {
                continue;
            }
            PsiElement resolved = importReference.resolve();
            if (resolved instanceof PsiClass) {
                addResolvedClass((PsiClass) resolved, psiClass, coupledClasses);
            } else {
                addQualifiedName(importReference.getQualifiedName(), psiClass, coupledClasses);
            }
        }
    }

    private void addResolvedClass(PsiClass referencedClass, PsiClass owner, Set<String> coupledClasses) {
        if (referencedClass == null || referencedClass.equals(owner)) {
            return;
        }
        if (referencedClass instanceof PsiAnonymousClass || referencedClass instanceof PsiTypeParameter) {
            return;
        }
        addQualifiedName(referencedClass.getQualifiedName(), owner, coupledClasses);
    }

    private void addQualifiedName(String qualifiedName, PsiClass owner, Set<String> coupledClasses) {
        if (qualifiedName == null || qualifiedName.isEmpty()) {
            return;
        }
        String ownerQualifiedName = owner.getQualifiedName();
        if (qualifiedName.equals(ownerQualifiedName)) {
            return;
        }
        coupledClasses.add(qualifiedName);
    }

    private void addTypeReference(PsiType type, PsiClass owner, Set<String> coupledClasses) {
        if (type == null) {
            return;
        }
        if (type instanceof PsiArrayType) {
            addTypeReference(((PsiArrayType) type).getComponentType(), owner, coupledClasses);
            return;
        }
        if (type instanceof PsiWildcardType) {
            addTypeReference(((PsiWildcardType) type).getBound(), owner, coupledClasses);
            return;
        }
        if (!(type instanceof PsiClassType)) {
            return;
        }

        PsiClassType classType = (PsiClassType) type;
        addResolvedClass(classType.resolve(), owner, coupledClasses);
        for (PsiType parameter : classType.getParameters()) {
            addTypeReference(parameter, owner, coupledClasses);
        }
    }

    private String inferTypeFromStaticCall(String scopeText) {
        if (scopeText == null) {
            return null;
        }
        switch (scopeText) {
            case "Objects":
                return "java.util.Objects";
            case "Comparator":
                return "java.util.Comparator";
            case "Collections":
                return "java.util.Collections";
            case "Arrays":
                return "java.util.Arrays";
            case "String":
                return "java.lang.String";
            case "Math":
                return "java.lang.Math";
            case "System":
                return "java.lang.System";
            case "Optional":
                return "java.util.Optional";
            case "Stream":
                return "java.util.stream.Stream";
            default:
                return null;
        }
    }
}
