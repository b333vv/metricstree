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

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.util.ClassUtils;
import org.b333vv.metric.model.metric.value.Value;

import static org.b333vv.metric.model.metric.MetricType.SIZE2;

public class NumberOfAttributesAndMethodsVisitor extends JavaClassVisitor {
    @Override
    public void visitClass(PsiClass psiClass) {
        super.visitClass(psiClass);
        metric = Metric.of(SIZE2, Value.UNDEFINED);
        if (ClassUtils.isConcrete(psiClass)) {
            PsiMethod[] methods = psiClass.getAllMethods();
            PsiField[] fields = psiClass.getAllFields();
            int operationsNumber = 0;
            for (PsiMethod method : methods) {
                PsiClass containingClass = method.getContainingClass();
                if (containingClass != null && containingClass.equals(psiClass) ||
                        !method.hasModifierProperty(PsiModifier.STATIC)) {
                    operationsNumber++;
                }
            }
            int attributesNumber = 0;
            for (PsiField field : fields) {
                PsiClass containingClass = field.getContainingClass();
                if (containingClass != null && containingClass
                        .equals(psiClass) || !field.hasModifierProperty(PsiModifier.STATIC)) {
                    attributesNumber++;
                }
            }
            metric = Metric.of(SIZE2, (long) operationsNumber + attributesNumber);
        }
    }
}