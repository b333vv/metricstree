package org.jacoquev.model.metric.util;

import com.intellij.psi.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClassUtils {
    private ClassUtils(){
//        Util class
    }

    public static boolean isConcrete(PsiClass psiClass) {
    return !(psiClass.isInterface() ||
            psiClass.isEnum() ||
            psiClass instanceof PsiAnonymousClass ||
            psiClass instanceof PsiTypeParameter ||
            psiClass.getParent() instanceof PsiDeclarationStatement);
    }

    @Nullable
    public static PsiPackage findPackage(PsiElement element) {
        if (element == null) {
            return null;
        }
        final PsiFile file = element.getContainingFile();
        final PsiDirectory directory = file.getContainingDirectory();
        if (directory == null) {
            return null;
        }
        return JavaDirectoryService.getInstance().getPackage(directory);
    }

    public static PsiPackage[] calculatePackagesRecursive(PsiElement element) {
        PsiPackage aPackage = findPackage(element);
        final List<PsiPackage> out = new ArrayList<>();
        while (aPackage != null) {
            out.add(aPackage);
            aPackage = aPackage.getParentPackage();
        }
        return out.toArray(new PsiPackage[out.size()]);
    }

    @Nullable
    public static List<PsiPackage> getPackagesRecursive(PsiFile psiFile) {
        final PsiDirectory directory = psiFile.getContainingDirectory();
        final List<PsiPackage> packageList = new ArrayList<>();
        PsiPackage psiPackage = JavaDirectoryService.getInstance().getPackage(directory);
        while (psiPackage.getParentPackage() != null) {
            packageList.add(psiPackage);
            psiPackage = psiPackage.getParentPackage();
        }
        Collections.reverse(packageList);
        return packageList;
    }
}