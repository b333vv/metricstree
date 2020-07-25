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
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.util.CommonUtils;
import org.b333vv.metric.model.util.MethodUtils;

import static org.b333vv.metric.model.metric.MetricType.LOC;

public class LinesOfCodeVisitor extends JavaMethodVisitor {
    private int methodNestingDepth = 0;
    private long elementCount = 0;
    @Override
    public void visitMethod(PsiMethod method) {
        long linesOfCode = 0;
        super.visitMethod(method);
        if (methodNestingDepth == 0) {
            elementCount = 0;
        }
        methodNestingDepth++;
        elementCount = CommonUtils.countLines(method);
        super.visitMethod(method);
        methodNestingDepth--;
        if (methodNestingDepth == 0 && !MethodUtils.isAbstract(method)) {
            linesOfCode = elementCount;
        }
        metric = Metric.of(LOC, linesOfCode);
    }
}