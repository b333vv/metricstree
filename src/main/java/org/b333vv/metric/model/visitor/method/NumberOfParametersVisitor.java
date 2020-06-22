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

import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.value.Value;

import static org.b333vv.metric.model.metric.MetricType.NOPM;

public class NumberOfParametersVisitor extends JavaMethodVisitor {
    private int methodNestingDepth = 0;

    @Override
    public void visitMethod(PsiMethod method) {
        metric = Metric.of(NOPM, Value.UNDEFINED);
        if (methodNestingDepth == 0) {
            final PsiParameterList parameterList = method.getParameterList();
            final PsiParameter[] parameters = parameterList.getParameters();
            metric = Metric.of(NOPM, parameters.length);
        }
        methodNestingDepth++;
        super.visitMethod(method);
        methodNestingDepth--;
    }
}