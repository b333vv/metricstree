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

import com.intellij.psi.*;
import com.intellij.psi.util.PropertyUtil;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.util.ClassUtils;
import org.b333vv.metric.model.metric.value.Value;

import java.util.Arrays;

import static org.b333vv.metric.model.metric.MetricType.NOAC;

public class NumberOfAccessorMethodsVisitor extends JavaClassVisitor {

    @Override
    public void visitClass(PsiClass psiClass) {
        metric = Metric.of(NOAC, Value.UNDEFINED);
        if (ClassUtils.isConcrete(psiClass)) {
            metric = Metric.of(NOAC, Arrays.stream(psiClass.getMethods())
                    .filter(m -> (PropertyUtil.isSimpleGetter(m) || PropertyUtil.isSimpleSetter(m))
                            && !m.hasModifierProperty(PsiModifier.STATIC))
                    .count());
        }
    }
}