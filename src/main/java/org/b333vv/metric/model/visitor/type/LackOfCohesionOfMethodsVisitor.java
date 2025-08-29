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
import org.b333vv.metric.model.util.ClassUtils;
import org.b333vv.metric.model.metric.value.Value;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.b333vv.metric.model.metric.MetricType.LCOM;

public class LackOfCohesionOfMethodsVisitor extends JavaClassVisitor {

    @Override
    public void visitClass(PsiClass psiClass) {
        super.visitClass(psiClass);
        metric = Metric.of(LCOM, Value.UNDEFINED);
        if (ClassUtils.isConcrete(psiClass)) {
            Set<PsiMethod> applicableMethods = CohesionUtils.getApplicableMethods(psiClass);
            
            // Special case: if no applicable methods (e.g., only static methods), LCOM should be 0
            if (applicableMethods.isEmpty()) {
                metric = Metric.of(LCOM, 0);
                return;
            }
            
            Map<PsiMethod, Set<PsiField>> fieldsPerMethod = CohesionUtils.calculateFieldUsage(applicableMethods);
            
            // Count methods that actually use instance fields
            long methodsUsingFields = fieldsPerMethod.values().stream()
                .mapToLong(fields -> fields.isEmpty() ? 0 : 1)
                .sum();
                
            // If no methods use instance fields, LCOM should be 0
            if (methodsUsingFields == 0) {
                metric = Metric.of(LCOM, 0);
                return;
            }
            
            Map<PsiMethod, Set<PsiMethod>> linkedMethods = CohesionUtils.calculateMethodLinkage(applicableMethods);
            Set<Set<PsiMethod>> components = CohesionUtils.calculateComponents(applicableMethods,
                    fieldsPerMethod, linkedMethods);
            metric = Metric.of(LCOM, components.size());
        }
    }
}
