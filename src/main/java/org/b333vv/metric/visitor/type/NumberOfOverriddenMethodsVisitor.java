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

package org.b333vv.metric.visitor.type;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.b333vv.metric.model.util.ClassUtils;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.util.MethodUtils;

import static org.b333vv.metric.model.metric.MetricType.NOOM;

public class NumberOfOverriddenMethodsVisitor extends JavaClassVisitor {
    @Override
    public void visitClass(PsiClass psiClass) {
        super.visitClass(psiClass);
        long overriddenMethodsNumber = 0;
        metric = Metric.of(NOOM, Value.UNDEFINED);
        if (ClassUtils.isConcrete(psiClass)) {
            PsiMethod[] methods = psiClass.getMethods();
            for (PsiMethod method : methods) {
                if (!MethodUtils.isConcrete(method)) {
                    continue;
                }
                if (MethodUtils.hasConcreteSuperMethod(method)) {
                    overriddenMethodsNumber++;
                }
            }
            metric = Metric.of(NOOM, overriddenMethodsNumber);
        }
    }
}