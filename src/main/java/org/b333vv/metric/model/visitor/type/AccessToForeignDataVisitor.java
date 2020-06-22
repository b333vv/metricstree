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
import com.intellij.psi.util.PropertyUtil;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.util.ClassUtils;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.util.MetricsUtils;

import java.util.HashSet;
import java.util.Set;

import static org.b333vv.metric.model.metric.MetricType.ATFD;

public class AccessToForeignDataVisitor extends JavaClassVisitor {
    private Set<PsiClass> usedClasses = new HashSet<>();

    @Override
    public void visitClass(PsiClass psiClass) {
        usedClasses.clear();
        super.visitClass(psiClass);
        metric = Metric.of(ATFD, Value.UNDEFINED);
        if (ClassUtils.isConcrete(psiClass)) {
            usedClasses.remove(psiClass);
            for (PsiClass parentClass : psiClass.getSupers()) {
                usedClasses.remove(parentClass);
            }
            metric = Metric.of(ATFD, usedClasses.size());
        }
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