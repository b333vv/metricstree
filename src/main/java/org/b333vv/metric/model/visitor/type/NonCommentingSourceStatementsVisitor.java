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
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiIfStatement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiStatement;
import com.intellij.psi.PsiBlockStatement;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.model.util.ClassUtils;

import static org.b333vv.metric.model.metric.MetricType.NCSS;

public class NonCommentingSourceStatementsVisitor extends JavaClassVisitor {
    private int statementsCount = 0;
    // Diagnostics counters (no effect on final metric; used for integration test logging)
    private int diagBaseStatements = 0;
    private int diagElseCount = 0;
    private int diagCatchCount = 0;
    private int diagFinallyCount = 0;
    private int diagSwitchEntryCount = 0;
    private int diagMethodDecls = 0;
    private int diagCtorDecls = 0;
    private int diagFieldDecls = 0;
    private int diagClassDecl = 0;

    @Override
    public void visitClass(PsiClass psiClass) {
        metric = Metric.of(NCSS, Value.UNDEFINED);
        final int previous = statementsCount;
        if (!ClassUtils.isAnonymous(psiClass)) {
            statementsCount = 0;
        }
        // Count the declaration of this class/interface/enum itself
        if (!ClassUtils.isAnonymous(psiClass)) {
            statementsCount++;
            diagClassDecl++;
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
        if (!(statement instanceof PsiComment)
                && !(statement instanceof PsiEmptyStatement)
                && !(statement instanceof PsiBlockStatement)
                && !(statement instanceof com.intellij.psi.PsiSwitchLabelStatement)) {
            statementsCount++;
            diagBaseStatements++;
        }
    }

    @Override
    public void visitMethod(PsiMethod method) {
        // Count method/constructor declaration itself
        statementsCount++;
        if (method.isConstructor()) {
            diagCtorDecls++;
        } else {
            diagMethodDecls++;
        }
        super.visitMethod(method);
    }

    @Override
    public void visitField(PsiField field) {
        // Count field declaration
        statementsCount++;
        diagFieldDecls++;
        super.visitField(field);
    }

    @Override
    public void visitIfStatement(PsiIfStatement statement) {
        super.visitIfStatement(statement);
        // Count 'else' as a separate statement if present
        if (statement.getElseBranch() != null) {
            statementsCount++;
            diagElseCount++;
        }
    }

    @Override
    public void visitTryStatement(com.intellij.psi.PsiTryStatement statement) {
        super.visitTryStatement(statement);
        // Count each catch and finally as a statement
        var catchSections = statement.getCatchSections();
        if (catchSections != null) {
            statementsCount += catchSections.length;
            diagCatchCount += catchSections.length;
        }
        if (statement.getFinallyBlock() != null) {
            statementsCount++;
            diagFinallyCount++;
        }
    }

    @Override
    public void visitSwitchLabelStatement(com.intellij.psi.PsiSwitchLabelStatement statement) {
        super.visitSwitchLabelStatement(statement);
        // Each case/default label counts as a statement
        statementsCount++;
        diagSwitchEntryCount++;
    }
}

