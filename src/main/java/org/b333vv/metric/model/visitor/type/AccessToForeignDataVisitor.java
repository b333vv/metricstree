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
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PropertyUtil;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;

public class AccessToForeignDataVisitor extends JavaClassVisitor {
    private Set<PsiClass> usedClasses = new HashSet<>();
    private long size2; // Add size2 field to store the calculated value

    @Override
    public void visitClass(PsiClass psiClass) {
        usedClasses.clear();
        super.visitClass(psiClass);
        metric = Metric.of(MetricType.ATFD, Value.UNDEFINED);
        
        // Only count instance fields (non-static)
        long instanceFields = Arrays.stream(psiClass.getFields())
            .filter(f -> !f.hasModifierProperty(PsiModifier.STATIC))
            .count();
        
        // Only count instance methods (non-static)
        long instanceMethods = Arrays.stream(psiClass.getMethods())
            .filter(m -> !m.hasModifierProperty(PsiModifier.STATIC))
            .count();
        
        size2 = instanceFields + instanceMethods;
        
        usedClasses.remove(psiClass);
        for (PsiClass parentClass : psiClass.getSupers()) {
            usedClasses.remove(parentClass);
        }
        metric = Metric.of(MetricType.ATFD, usedClasses.size());
    }

    public long getSize2() {
        return size2;
    }

    @Override
    public void visitReferenceExpression(PsiReferenceExpression psiReferenceExpression) {
        if (psiReferenceExpression == null) {
            return;
        }
        
        try {
            super.visitReferenceExpression(psiReferenceExpression);
        } catch (Exception e) {
            // Handle potential stack underflow or other visitor issues
            // Continue processing without the super call
        }
        
        final PsiElement element = psiReferenceExpression.resolve();
        if (element instanceof PsiField) {
            final PsiField field = (PsiField) element;
            // Only count field usage if it's not static
            if (!field.hasModifierProperty(PsiModifier.STATIC)) {
                final PsiClass containingClass = field.getContainingClass();
                if (containingClass != null) {
                    usedClasses.add(containingClass);
                }
            }
        }
    }
}