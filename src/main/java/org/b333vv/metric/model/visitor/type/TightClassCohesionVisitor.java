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
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.util.ClassUtils;
import org.b333vv.metric.model.metric.value.Value;

import java.util.HashSet;
import java.util.Set;

import static org.b333vv.metric.model.metric.MetricType.TCC;

public class TightClassCohesionVisitor extends JavaClassVisitor {

    @Override
    public void visitClass(PsiClass psiClass) {
        super.visitClass(psiClass);
        metric = Metric.of(TCC, Value.UNDEFINED);
        if (ClassUtils.isConcrete(psiClass)) {
            Set<PsiMethod> methods = new HashSet<>();
            for (PsiMethod psiMethod : CohesionUtils.getApplicableMethods(psiClass)) {
                if (!psiMethod.getModifierList().hasModifierProperty(PsiModifier.ABSTRACT)) {
                    methods.add(psiMethod);
                }
            }
            int connectedMethods = CohesionUtils.calculateConnectedMethods(methods);
            int methodsNumber = methods.size();
            int possibleConnectedMethods = methodsNumber * (methodsNumber - 1) / 2;
            if (possibleConnectedMethods == 0) {
                metric = Metric.of(TCC, Value.of(0.0));
            } else {
                metric = Metric.of(TCC, Value.of((double) connectedMethods)
                        .divide(Value.of((double) possibleConnectedMethods)));
            }
        }
    }
}