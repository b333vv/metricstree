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
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Query;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.value.Value;

public class FanInVisitor extends JavaMethodVisitor {
    private PsiMethod currentMethod;
    private int methodNestingDepth = 0;
    private int result = 0;

    @Override
    public void visitMethod(PsiMethod method) {
        metric = Metric.of("FIN", "Fan-In",
                "/html/FanIn.html", Value.UNDEFINED);
        if (methodNestingDepth == 0) {
            result = 0;
            currentMethod = method;
            final Query<PsiReference> references = ReferencesSearch.search(method);
            for (PsiReference reference : references) {
                PsiElement element = reference.getElement();
                if (element.getParent() instanceof PsiCallExpression) {
                    result++;
                }
            }
        }

        methodNestingDepth++;
        super.visitMethod(method);
        methodNestingDepth--;

        if (methodNestingDepth == 0) {
            metric = Metric.of("FIN", "Fan-In",
                    "/html/FanIn.html", result);
        }
    }

    @Override
    public void visitMethodCallExpression(PsiMethodCallExpression expression) {
        PsiMethod method = expression.resolveMethod();
        if (currentMethod != null && currentMethod.equals(method)) {
            result--;
        }
        super.visitMethodCallExpression(expression);
    }
}
