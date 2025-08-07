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

import com.intellij.psi.*;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.util.MethodUtils;

import static org.b333vv.metric.model.metric.MetricType.LND;

public class LoopNestingDepthVisitor extends JavaMethodVisitor {
    private long loopNestingDepth = 0;
    private long methodNestingCount = 0;
    private long maximumDepth = 0;
    private long currentDepth = 0;

    @Override
    public void visitMethod(PsiMethod method) {
        metric = Metric.of(LND, Value.UNDEFINED);
        if (methodNestingCount == 0) {
            maximumDepth = 0;
            currentDepth = 0;
        }
        methodNestingCount++;
        try {
            super.visitMethod(method);
        } catch (Exception e) {
            // Handle potential stack underflow or other visitor issues
        }
        methodNestingCount--;
        if (methodNestingCount == 0 && !MethodUtils.isAbstract(method)) {
            loopNestingDepth = maximumDepth;
        }
        metric = Metric.of(LND, loopNestingDepth);
    }

    @Override
    public void visitDoWhileStatement(PsiDoWhileStatement statement) {
        enterScope();
        try {
            super.visitDoWhileStatement(statement);
        } catch (Exception e) {
            // Handle potential stack underflow or other visitor issues
        }
        exitScope();
    }

    @Override
    public void visitWhileStatement(PsiWhileStatement statement) {
        enterScope();
        try {
            super.visitWhileStatement(statement);
        } catch (Exception e) {
            // Handle potential stack underflow or other visitor issues
        }
        exitScope();
    }

    @Override
    public void visitForStatement(PsiForStatement statement) {
        enterScope();
        try {
            super.visitForStatement(statement);
        } catch (Exception e) {
            // Handle potential stack underflow or other visitor issues
        }
        exitScope();
    }

    @Override
    public void visitForeachStatement(PsiForeachStatement statement) {
        enterScope();
        try {
            super.visitForeachStatement(statement);
        } catch (Exception e) {
            // Handle potential stack underflow or other visitor issues
        }
        exitScope();
    }

    private void enterScope() {
        currentDepth++;
        maximumDepth = Math.max(maximumDepth, currentDepth);
    }

    private void exitScope() {
        currentDepth--;
    }
}