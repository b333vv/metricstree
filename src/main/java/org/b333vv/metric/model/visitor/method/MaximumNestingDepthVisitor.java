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

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiMethod;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.util.MethodUtils;
import org.b333vv.metric.model.metric.value.Value;

import static org.b333vv.metric.model.metric.MetricType.MND;

public class MaximumNestingDepthVisitor extends JavaMethodVisitor {
    private int methodNestingCount = 0;
    private int maximumDepth = 0;
    private int currentDepth = 0;

    @Override
    public void visitMethod(PsiMethod method) {
        metric = Metric.of(MND, Value.UNDEFINED);
        methodNestingCount++;
        super.visitMethod(method);
        methodNestingCount--;
        if (methodNestingCount == 0) {
            if (!MethodUtils.isAbstract(method)) {
                metric = Metric.of(MND, maximumDepth - 1);
            }
            maximumDepth = 0;
            currentDepth = 0;
        }
    }

    @Override
    public void visitCodeBlock(PsiCodeBlock block) {
        if (methodNestingCount != 0) {
            enterScope();
        }
        super.visitCodeBlock(block);
        if (methodNestingCount != 0) {
            exitScope();
        }
    }

    @Override
    public void visitClass(PsiClass aClass) {
        if (methodNestingCount != 0) {
            enterScope();
        }
        super.visitClass(aClass);
        if (methodNestingCount != 0) {
            exitScope();
        }
    }

    private void enterScope() {
        currentDepth++;
        maximumDepth = Math.max(maximumDepth, currentDepth);
    }

    private void exitScope() {
        currentDepth--;
    }
}
