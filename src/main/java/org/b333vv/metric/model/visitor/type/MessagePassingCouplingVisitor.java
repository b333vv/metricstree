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
import com.intellij.psi.PsiMethodCallExpression;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.util.ClassUtils;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.util.MetricsUtils;

public class MessagePassingCouplingVisitor extends JavaClassVisitor {
    private int methodCallsNumber = 0;

    @Override
    public void visitClass(PsiClass psiClass) {
        metric = Metric.of("MPC", "Message Passing Coupling",
                "/html/MessagePassingCoupling.html", Value.UNDEFINED);
        if (!ClassUtils.isConcrete(psiClass)) {
            return;
        }
        if (ClassUtils.isConcrete(psiClass) && !ClassUtils.isAnonymous(psiClass)) {
            methodCallsNumber = 0;
        }
        super.visitClass(psiClass);
        metric = Metric.of("MPC", "Message Passing Coupling",
                "/html/MessagePassingCoupling.html", methodCallsNumber);
    }

    @Override
    public void visitMethodCallExpression(PsiMethodCallExpression expression) {
        super.visitMethodCallExpression(expression);
        methodCallsNumber++;
    }
}