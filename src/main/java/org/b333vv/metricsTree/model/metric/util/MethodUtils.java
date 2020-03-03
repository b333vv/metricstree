package org.b333vv.metricsTree.model.metric.util;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.search.searches.SuperMethodsSearch;
import com.intellij.psi.util.MethodSignatureBackedByPsiMethod;
import com.intellij.util.Processor;
import com.intellij.util.Query;

public class MethodUtils {
    private MethodUtils() {}

    public static boolean hasConcreteSuperMethod(PsiMethod method) {
        final Query<MethodSignatureBackedByPsiMethod> search = SuperMethodsSearch.search(method, null, true, false);
        return !search.forEach(new Processor<MethodSignatureBackedByPsiMethod>() {

            @Override
            public boolean process(MethodSignatureBackedByPsiMethod superMethod) {
                return isAbstract(superMethod.getMethod());
            }
        });
    }

    public static boolean isAbstract(PsiMethod method) {
        if (method.hasModifierProperty(PsiModifier.STATIC) || method.hasModifierProperty(PsiModifier.DEFAULT)) {
            return false;
        }
        if (method.hasModifierProperty(PsiModifier.ABSTRACT)) {
            return true;
        }
        final PsiClass containingClass = method.getContainingClass();
        return containingClass != null && containingClass.isInterface();
    }

    public static boolean isConcrete(PsiMethod method) {
        return method != null && !method.isConstructor() && !method.hasModifierProperty(PsiModifier.ABSTRACT) &&
                !method.hasModifierProperty(PsiModifier.STATIC) && !method.hasModifierProperty(PsiModifier.PRIVATE);
    }
}
