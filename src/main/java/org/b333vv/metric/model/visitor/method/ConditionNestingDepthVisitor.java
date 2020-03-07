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

import com.intellij.psi.PsiIfStatement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiStatement;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.util.MethodUtils;

public class ConditionNestingDepthVisitor extends JavaMethodVisitor {
    private long methodNestingCount = 0;
    private long maximumDepth = 0;
    private long currentDepth = 0;

    @Override
    public void visitMethod(PsiMethod method) {
        metric = Metric.of("CND", "Condition Nesting Depth",
                "/html/ConditionNestingDepth.html", Value.UNDEFINED);
        long conditionNestingDepth = 0;
        if (methodNestingCount == 0) {
            maximumDepth = 0;
            currentDepth = 0;
        }
        methodNestingCount++;
        super.visitMethod(method);
        methodNestingCount--;
        if (methodNestingCount == 0 && !MethodUtils.isAbstract(method)) {
            conditionNestingDepth = maximumDepth;
        }
        metric = Metric.of("CND", "Condition Nesting Depth",
                "/html/ConditionNestingDepth.html", conditionNestingDepth);
    }

    @Override
    public void visitIfStatement(PsiIfStatement statement) {
        boolean isAlreadyCounted = false;
        if (statement.getParent() instanceof PsiIfStatement) {
            final PsiIfStatement parent = (PsiIfStatement) statement.getParent();
            final PsiStatement elseBranch = parent.getElseBranch();
            if (statement.equals(elseBranch)) {
                isAlreadyCounted = true;
            }
        }
        if (!isAlreadyCounted) {
            enterScope();
        }
        super.visitIfStatement(statement);

        if (!isAlreadyCounted) {
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
