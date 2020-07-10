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
import com.intellij.psi.PsiModifier;
import com.intellij.psi.util.PropertyUtil;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.util.ClassUtils;
import org.b333vv.metric.model.metric.value.Value;

import static org.b333vv.metric.model.metric.MetricType.WOC;

public class WeightOfAClassVisitor extends JavaClassVisitor {

    @Override
    public void visitClass(PsiClass psiClass) {
        metric = Metric.of(WOC, Value.UNDEFINED);
        if (ClassUtils.isConcrete(psiClass)) {
            long functionalPublicMethods = CohesionUtils.getApplicableMethods(psiClass).stream()
                    .filter(m -> !PropertyUtil.isSimpleGetter(m)
                            && !PropertyUtil.isSimpleSetter(m)
                            && !m.hasModifierProperty(PsiModifier.ABSTRACT))
                    .count();
            long allPublicMethods = CohesionUtils.getApplicableMethods(psiClass).stream()
                    .filter(m -> !m.hasModifierProperty(PsiModifier.ABSTRACT))
                    .count();
            if (allPublicMethods == 0L) {
                metric = Metric.of(WOC, 0.00);
            } else {
                metric = Metric.of(WOC, Value.of((double) functionalPublicMethods)
                        .divide(Value.of((double) allPublicMethods)));
            }
        }
    }
}