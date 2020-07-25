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
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiEmptyStatement;
import com.intellij.psi.PsiStatement;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.model.util.ClassUtils;

import static org.b333vv.metric.model.metric.MetricType.NCSS;

public class NonCommentingSourceStatementsVisitor extends JavaClassVisitor {
    private int statementsCount = 0;

    @Override
    public void visitClass(PsiClass psiClass) {
        metric = Metric.of(NCSS, Value.UNDEFINED);
        final int previous = statementsCount;
        if (!ClassUtils.isAnonymous(psiClass)) {
            statementsCount = 0;
        }
        super.visitClass(psiClass);
        if (!ClassUtils.isAnonymous(psiClass)) {
            if (!psiClass.isInterface()) {
                metric = Metric.of(NCSS, statementsCount);
            }
            statementsCount = previous;
        }
    }

    @Override
    public void visitStatement(PsiStatement statement) {
        super.visitStatement(statement);
        if (!(statement instanceof PsiComment) && !(statement instanceof PsiEmptyStatement)) {
            statementsCount++;
        }
    }
}

