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

package org.b333vv.metric.builder;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.impl.file.PsiPackageImpl;
import org.b333vv.metric.model.code.*;
import org.b333vv.metric.model.code.FileElement;
import org.b333vv.metric.model.code.ProjectElement;
import org.b333vv.metric.model.util.ClassUtils;
import org.b333vv.metric.service.CacheService;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class PackagesModelBuilder extends ModelBuilder {

    private final ProjectElement projectElement;

    public PackagesModelBuilder(ProjectElement projectElement) {
        this.projectElement = projectElement;
    }

    public void addJavaFileToprojectElement(@NotNull PsiJavaFile psiJavaFile) {
        findOrCreateJavaPackage(psiJavaFile).addFile(createJavaFile(psiJavaFile));
    }

    public PackageElement findOrCreateJavaPackage(@NotNull PsiJavaFile psiJavaFile) {
        List<PsiPackage> packageList = ClassUtils.getPackagesRecursive(psiJavaFile);
        if (projectElement.allPackagesIsEmpty()) {
            return makeNewRootJavaPackage(packageList);
        } else {
            Collections.reverse(packageList);
            PsiPackage[] psiPackages = packageList.toArray(new PsiPackage[0]);
            int j = 0;
            PackageElement aPackage = null;
            for (int i = 0; i < psiPackages.length; i++) {
                PackageElement javaPackage = projectElement.getFromAllPackages(psiPackages[i].getQualifiedName());
                if (javaPackage != null) {
                    aPackage = projectElement.getFromAllPackages(psiPackages[i].getQualifiedName());
                    j = i;
                    break;
                }
            }
            if (aPackage == null) {
                Collections.reverse(packageList);
                return makeNewRootJavaPackage(packageList);
            }
            for (int i = j - 1; i >= 0; i--) {
                PackageElement newPackage = new PackageElement(psiPackages[i].getName(), psiPackages[i]);
                projectElement.putToAllPackages(newPackage.getPsiPackage().getQualifiedName(), newPackage);
                aPackage.addPackage(newPackage);
                aPackage = newPackage;
            }
            return aPackage;
        }
    }

    @NotNull
    private PackageElement makeNewRootJavaPackage(@NotNull List<PsiPackage> packageList) {
        Iterator<PsiPackage> psiPackageIterator = packageList.iterator();
        PackageElement firstJavaPackage;
        if (!psiPackageIterator.hasNext()) {
            firstJavaPackage = new PackageElement("", new PsiPackageImpl(null, ""));
        } else {
            PsiPackage firstPsiPackage = psiPackageIterator.next();
            firstJavaPackage = new PackageElement(firstPsiPackage.getName(), firstPsiPackage);
            if (firstJavaPackage.getPsiPackage().getClasses().length > 0) {
                projectElement.putToAllPackages(firstJavaPackage.getPsiPackage().getQualifiedName(), firstJavaPackage);
            }
        }
        projectElement.addPackage(firstJavaPackage);
        PackageElement currentJavaPackage = firstJavaPackage;
        while (psiPackageIterator.hasNext()) {
            PsiPackage aPsiPackage = psiPackageIterator.next();
            PackageElement aJavaPackage = new PackageElement(aPsiPackage.getName(), aPsiPackage);
            if (aJavaPackage.getPsiPackage() != null && aJavaPackage.getPsiPackage().getClasses().length > 0) {
                projectElement.putToAllPackages(aJavaPackage.getPsiPackage().getQualifiedName(), aJavaPackage);
            }
            currentJavaPackage.addPackage(aJavaPackage);
            currentJavaPackage = aJavaPackage;
        }
        return currentJavaPackage;
    }

    @Override
    protected FileElement createJavaFile(@NotNull PsiJavaFile psiJavaFile) {
        FileElement javaFile = psiJavaFile.getProject().getService(CacheService.class).getJavaFile(psiJavaFile.getVirtualFile());
        if (javaFile != null) {
            return javaFile;
        }
        javaFile = new FileElement(psiJavaFile.getName());
        for (PsiClass psiClass : psiJavaFile.getClasses()) {
            ClassElement javaClass = new ClassElement(psiClass);
            javaFile.addClass(javaClass);
        }
        return javaFile;
    }

    @Override
    protected void addToAllClasses(ClassElement javaClass) {
    }

    @Override
    protected Stream<JavaRecursiveElementVisitor> classVisitors() {
        return null;
    }

    @Override
    protected Stream<JavaRecursiveElementVisitor> methodVisitors() {
        return null;
    }
}