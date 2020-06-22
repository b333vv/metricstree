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

import com.intellij.psi.*;
import org.b333vv.metric.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ClassUtils {

    private ClassUtils() {
        // Utility class
    }

    public static boolean isConcrete(PsiClass psiClass) {
    return !(psiClass.isInterface() ||
            psiClass.isEnum() ||
            psiClass instanceof PsiAnonymousClass ||
            psiClass instanceof PsiTypeParameter ||
            psiClass.getParent() instanceof PsiDeclarationStatement);
    }

    public static boolean isAnonymous(PsiClass aClass) {
        return aClass instanceof PsiAnonymousClass || aClass instanceof PsiTypeParameter ||
                aClass.getParent() instanceof PsiDeclarationStatement;
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

    @NotNull
    public static String calculatePackageName(PsiElement element) {
        final PsiFile file = element.getContainingFile();
        if (!(file instanceof PsiJavaFile)) {
            return "";
        }
        final PsiJavaFile javaFile = (PsiJavaFile) file;
        return javaFile.getPackageName();
    }

    @NotNull
    public static List<PsiPackage> getPackagesRecursive(PsiFile psiFile) {
        final PsiDirectory directory = psiFile.getContainingDirectory();
        final List<PsiPackage> packageList = new ArrayList<>();
        PsiPackage psiPackage = JavaDirectoryService.getInstance().getPackage(directory);
        if (psiPackage == null) {
            return List.of();
        }
        while (psiPackage.getParentPackage() != null) {
            packageList.add(psiPackage);
            psiPackage = psiPackage.getParentPackage();
        }
        Collections.reverse(packageList);
        return packageList;
    }
}
