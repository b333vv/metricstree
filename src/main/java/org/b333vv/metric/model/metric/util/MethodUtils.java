package org.b333vv.metric.model.metric.util;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.search.searches.SuperMethodsSearch;
import com.intellij.psi.util.MethodSignatureBackedByPsiMethod;
import com.intellij.util.Query;

public class MethodUtils {
    private MethodUtils() {}

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
