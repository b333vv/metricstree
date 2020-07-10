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

package org.b333vv.metric.visitor.method;

import com.intellij.psi.*;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.value.Value;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static org.b333vv.metric.model.metric.MetricType.CINT;

public class CouplingIntensityVisitor extends JavaMethodVisitor {
    private Set<PsiMethod> usedMethods = new HashSet<>();

    @Override
    public void visitMethod(PsiMethod method) {
        usedMethods.clear();
        metric = Metric.of(CINT, Value.UNDEFINED);
        super.visitMethod(method);
        long usedMethodsNumber = usedMethods.stream()
                .filter(m -> !Objects.equals(m.getContainingClass(), method.getContainingClass()))
                .count();
        metric = Metric.of(CINT, usedMethodsNumber);
    }

    @Override
    public void visitMethodCallExpression(PsiMethodCallExpression psiMethodCallExpression) {
        super.visitMethodCallExpression(psiMethodCallExpression);
        final PsiMethod method = psiMethodCallExpression.resolveMethod();
        if (method == null) {
            return;
        }
        usedMethods.add(method);
    }
}