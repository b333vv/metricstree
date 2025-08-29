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

package org.b333vv.metric.model.util;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
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
            isLocalClass(psiClass));
    }
    
    /**
     * Checks if a class is a local class (defined inside a method or initializer block).
     * This is more precise than checking for PsiDeclarationStatement parent,
     * as it specifically targets classes that shouldn't have metrics calculated.
     */
    private static boolean isLocalClass(PsiClass psiClass) {
        // Local classes are defined inside methods, constructors, or initializer blocks
        // They have PsiDeclarationStatement as parent AND that statement is inside a code block
        PsiElement parent = psiClass.getParent();
        if (!(parent instanceof PsiDeclarationStatement)) {
            return false;
        }
        
        // Check if the declaration statement is inside a method, constructor, or initializer
        PsiElement grandParent = parent.getParent();
        while (grandParent != null) {
            if (grandParent instanceof PsiMethod || 
                grandParent instanceof PsiClassInitializer ||
                grandParent instanceof PsiCodeBlock) {
                return true;  // This is a local class inside executable code
            }
            if (grandParent instanceof PsiClass) {
                return false; // This is an inner class, not a local class
            }
            grandParent = grandParent.getParent();
        }
        
        return false; // Default to allowing the class
    }

    public static boolean isAbstractClass(PsiClass aClass) {
        return !aClass.isInterface() && aClass.hasModifierProperty(PsiModifier.ABSTRACT);
    }

    public static boolean isStaticClass(PsiClass aClass) {
        return aClass.hasModifierProperty(PsiModifier.STATIC);
    }

    public static boolean isConcreteClass(PsiClass aClass) {
        return !aClass.isInterface() && !aClass.isEnum() && !aClass.hasModifierProperty(PsiModifier.ABSTRACT);
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
