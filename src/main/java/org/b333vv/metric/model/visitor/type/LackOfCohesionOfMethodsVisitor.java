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
import org.b333vv.metric.model.metric.util.ClassUtils;
import org.b333vv.metric.model.metric.value.Value;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.b333vv.metric.model.metric.MetricType.LCOM;

public class LackOfCohesionOfMethodsVisitor extends JavaClassVisitor {

    @Override
    public void visitClass(PsiClass psiClass) {
        super.visitClass(psiClass);
        metric = Metric.of(LCOM, Value.UNDEFINED);
        if (ClassUtils.isConcrete(psiClass)) {
            Set<PsiMethod> applicableMethods = CohesionUtils.getApplicableMethods(psiClass);
            Map<PsiMethod, Set<PsiField>> fieldsPerMethod = CohesionUtils.calculateFieldUsage(applicableMethods);
            Map<PsiMethod, Set<PsiMethod>> linkedMethods = CohesionUtils.calculateMethodLinkage(applicableMethods);
            Set<Set<PsiMethod>> components = CohesionUtils.calculateComponents(applicableMethods,
                    fieldsPerMethod, linkedMethods);
            metric = Metric.of(LCOM, components.size());
        }
    }
}
