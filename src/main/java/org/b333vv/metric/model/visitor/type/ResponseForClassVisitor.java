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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiCallExpression;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.b333vv.metric.model.util.ClassUtils;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.model.metric.Metric;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.b333vv.metric.model.metric.MetricType.RFC;

public class ResponseForClassVisitor extends JavaClassVisitor {
    @Override
    public void visitClass(PsiClass psiClass) {
        metric = Metric.of(RFC, Value.UNDEFINED);
        if (ClassUtils.isConcrete(psiClass)) {
            Set<PsiMethod> methodsCalled = new HashSet<>();
            super.visitClass(psiClass);
            Collections.addAll(methodsCalled, psiClass.getMethods());
            psiClass.acceptChildren(new JavaRecursiveElementVisitor() {

                @Override
                public void visitClass(PsiClass psiClass) {}

                @Override
                public void visitCallExpression(PsiCallExpression callExpression) {
                    super.visitCallExpression(callExpression);
                    final PsiMethod target = callExpression.resolveMethod();
                    if (target != null) {
                        methodsCalled.add(target);
                    }
                }
            });
            metric = Metric.of(RFC, methodsCalled.size());
        }
    }
}