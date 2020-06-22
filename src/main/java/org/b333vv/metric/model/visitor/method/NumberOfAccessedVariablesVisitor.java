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

package org.b333vv.metric.model.visitor.method;

import com.intellij.psi.*;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.value.Value;

import java.util.HashSet;
import java.util.Set;

import static org.b333vv.metric.model.metric.MetricType.NOAV;

public class NumberOfAccessedVariablesVisitor extends JavaMethodVisitor {
    private Set<PsiVariable> accessedVariables = new HashSet<>();

    @Override
    public void visitMethod(PsiMethod method) {
        metric = Metric.of(NOAV, Value.UNDEFINED);
        if (!method.hasModifierProperty(PsiModifier.ABSTRACT)) {
            accessedVariables.clear();
            super.visitMethod(method);
            metric = Metric.of(NOAV, Value.of(accessedVariables.size()));
        }
    }

    @Override
    public void visitReferenceExpression(PsiReferenceExpression psiReferenceExpression) {
        super.visitReferenceExpression(psiReferenceExpression);
        final PsiElement element = psiReferenceExpression.resolve();
        if (element == null) {
            return;
        }
        if (element instanceof PsiVariable) {
            accessedVariables.add((PsiVariable) element);
        }
    }
}