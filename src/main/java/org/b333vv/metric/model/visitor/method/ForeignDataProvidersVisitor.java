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

import static org.b333vv.metric.model.metric.MetricType.FDP;

public class ForeignDataProvidersVisitor extends JavaMethodVisitor {
    private Set<PsiClass> usedClasses = new HashSet<>();

    @Override
    public void visitMethod(PsiMethod method) {
        usedClasses.clear();
        metric = Metric.of(FDP, Value.UNDEFINED);
        super.visitMethod(method);
        usedClasses.remove(method.getContainingClass());
        for (PsiClass parentClass : Objects.requireNonNull(method.getContainingClass()).getSupers()) {
            usedClasses.remove(parentClass);
        }
        metric = Metric.of(FDP, usedClasses.size());
    }

    @Override
    public void visitMethodCallExpression(PsiMethodCallExpression psiMethodCallExpression) {
        super.visitMethodCallExpression(psiMethodCallExpression);
        final PsiMethod method = psiMethodCallExpression.resolveMethod();
        if (method == null) {
            return;
        }
        if (PropertyUtil.isSimpleGetter(method) || PropertyUtil.isSimpleSetter(method)) {
            usedClasses.add(method.getContainingClass());
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
            usedClasses.add(field.getContainingClass());
        }
    }
}