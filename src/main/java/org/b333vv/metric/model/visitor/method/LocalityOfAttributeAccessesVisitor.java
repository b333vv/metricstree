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
import com.intellij.psi.util.PropertyUtil;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.value.Value;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static org.b333vv.metric.model.metric.MetricType.LAA;

public class LocalityOfAttributeAccessesVisitor extends JavaMethodVisitor {
    private Set<PsiField> accessedFields = new HashSet<>();

    @Override
    public void visitMethod(PsiMethod method) {
        metric = Metric.of(LAA, Value.UNDEFINED);
        accessedFields.clear();
        super.visitMethod(method);
        int accessedFieldsNumber = accessedFields.size();
        long accessedOwnFieldsNumber = accessedFields.stream()
                .filter(f -> Objects.equals(f.getContainingClass(), method.getContainingClass())).count();
        if (accessedFieldsNumber == 0) {
            metric = Metric.of(LAA, Value.of(0.0));
        } else {
            metric = Metric.of(LAA, Value.of((double) accessedOwnFieldsNumber)
                    .divide(Value.of((double) accessedFieldsNumber)));
        }
    }

    @Override
    public void visitMethodCallExpression(PsiMethodCallExpression psiMethodCallExpression) {
        super.visitMethodCallExpression(psiMethodCallExpression);
        final PsiMethod method = psiMethodCallExpression.resolveMethod();
        if (method == null) {
            return;
        }
        if (PropertyUtil.isSimpleGetter(method)) {
            accessedFields.add(PropertyUtil.getFieldOfGetter(method));
            return;
        }
        if (PropertyUtil.isSimpleSetter(method)) {
            accessedFields.add(PropertyUtil.getFieldOfSetter(method));
        }
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
            accessedFields.add(field);
        }
    }
}