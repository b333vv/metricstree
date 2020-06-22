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

package org.b333vv.metric.model.metric.util;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.search.searches.SuperMethodsSearch;
import com.intellij.psi.util.MethodSignatureBackedByPsiMethod;
import com.intellij.util.Query;

public final class MethodUtils {

    private MethodUtils() {
        // Utility class
    }

    public static boolean hasConcreteSuperMethod(PsiMethod psiMethod) {
        final Query<MethodSignatureBackedByPsiMethod> search = SuperMethodsSearch.search(psiMethod,
                null, true, false);
        return !search.forEach(superMethod -> {
            return isAbstract(superMethod.getMethod());
        });
    }

    public static boolean isAbstract(PsiMethod psiMethod) {
        if (psiMethod.hasModifierProperty(PsiModifier.STATIC) || psiMethod.hasModifierProperty(PsiModifier.DEFAULT)) {
            return false;
        }
        if (psiMethod.hasModifierProperty(PsiModifier.ABSTRACT)) {
            return true;
        }
        final PsiClass containingClass = psiMethod.getContainingClass();
        return containingClass != null && containingClass.isInterface();
    }

    public static boolean isConcrete(PsiMethod psiMethod) {
        return psiMethod != null && !psiMethod.isConstructor() && !psiMethod.hasModifierProperty(PsiModifier.ABSTRACT) &&
                !psiMethod.hasModifierProperty(PsiModifier.STATIC) && !psiMethod.hasModifierProperty(PsiModifier.PRIVATE);
    }
}
