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

import static org.b333vv.metric.model.metric.MetricType.NOL;

public class NumberOfLoopsVisitor extends JavaMethodVisitor {
    private long methodNestingDepth = 0;
    private long elementCount = 0;
    private long numberOfLoops = 0;

    @Override
    public void visitMethod(PsiMethod method) {
        metric = Metric.of(NOL, Value.UNDEFINED);
        if (methodNestingDepth == 0) {
            elementCount = 0;
        }
        methodNestingDepth++;
        try {
            super.visitMethod(method);
        } catch (Exception e) {
            // Handle potential stack underflow or other visitor issues
            // Continue processing without the super call
        }
        methodNestingDepth--;
        if (methodNestingDepth == 0 && !MethodUtils.isAbstract(method)) {
            numberOfLoops = elementCount;
        }
        metric = Metric.of(NOL, numberOfLoops);
    }

    @Override
    public void visitForStatement(PsiForStatement statement) {
        try {
            super.visitForStatement(statement);
        } catch (Exception e) {
            // Handle potential stack underflow or other visitor issues
        }
        elementCount++;
    }

    @Override
    public void visitForeachStatement(PsiForeachStatement statement) {
        try {
            super.visitForeachStatement(statement);
        } catch (Exception e) {
            // Handle potential stack underflow or other visitor issues
        }
        elementCount++;
    }

    @Override
    public void visitDoWhileStatement(PsiDoWhileStatement statement) {
        try {
            super.visitDoWhileStatement(statement);
        } catch (Exception e) {
            // Handle potential stack underflow or other visitor issues
        }
        elementCount++;
    }

    @Override
    public void visitWhileStatement(PsiWhileStatement statement) {
        try {
            super.visitWhileStatement(statement);
        } catch (Exception e) {
            // Handle potential stack underflow or other visitor issues
        }
        elementCount++;
    }
}
