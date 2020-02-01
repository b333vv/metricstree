package org.jacoquev.model.metric.util;

import com.intellij.psi.*;
import org.jetbrains.annotations.Nullable;

public class TypeUtils {
    private TypeUtils(){
//        Util class
    }

    public static boolean isConcrete(PsiClass psiClass) {
    return !(psiClass.isInterface() ||
            psiClass.isEnum() ||
            psiClass instanceof PsiAnonymousClass ||
            psiClass instanceof PsiTypeParameter ||
            psiClass.getParent() instanceof PsiDeclarationStatement);
    }
}
