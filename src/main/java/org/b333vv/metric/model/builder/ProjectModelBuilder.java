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

package org.b333vv.metric.model.builder;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiPackage;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.code.JavaPackage;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.model.metric.util.ClassUtils;
import org.b333vv.metric.util.MetricsService;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class ProjectModelBuilder extends ModelBuilder {

    private final JavaProject javaProject;

    public ProjectModelBuilder(JavaProject javaProject) {
        super();
        this.javaProject = javaProject;
    }

    public void addJavaFileToJavaProject(JavaProject javaProject, PsiJavaFile psiJavaFile) {
        JavaPackage javaPackage = findOrCreateJavaPackage(javaProject, psiJavaFile);
        createJavaClass(javaPackage, psiJavaFile);
    }

    private JavaPackage findOrCreateJavaPackage(JavaProject javaProject, PsiJavaFile psiJavaFile) {
        List<PsiPackage> packageList = ClassUtils.getPackagesRecursive(psiJavaFile);
        if (javaProject.allPackagesIsEmpty()) {
            return makeNewRootJavaPackage(javaProject, packageList);
        } else {
            Collections.reverse(packageList);
            PsiPackage[] psiPackages = packageList.toArray(new PsiPackage[0]);
            int j = 0;
            JavaPackage aPackage = null;
            for (int i = 0; i < psiPackages.length; i++) {
                JavaPackage javaPackage = javaProject.getFromAllPackages(psiPackages[i].getQualifiedName());
                if (javaPackage != null) {
                    aPackage = javaProject.getFromAllPackages(psiPackages[i].getQualifiedName());
                    j = i;
                    break;
                }
            }
            if (aPackage == null) {
                Collections.reverse(packageList);
                return makeNewRootJavaPackage(javaProject, packageList);
            }
            for (int i = j - 1; i >= 0; i--) {
                JavaPackage newPackage = new JavaPackage(psiPackages[i].getName(), psiPackages[i]);
                javaProject.putToAllPackages(newPackage.getPsiPackage().getQualifiedName(), newPackage);
                aPackage.addPackage(newPackage);
                aPackage = newPackage;
            }
            return aPackage;
        }
    }

    @NotNull
    private JavaPackage makeNewRootJavaPackage(JavaProject javaProject, List<PsiPackage> packageList) {
        Iterator<PsiPackage> psiPackageIterator = packageList.iterator();
        PsiPackage firstPsiPackage = psiPackageIterator.next();
        JavaPackage firstJavaPackage = new JavaPackage(firstPsiPackage.getName(), firstPsiPackage);
        javaProject.putToAllPackages(firstJavaPackage.getPsiPackage().getQualifiedName(), firstJavaPackage);
        javaProject.addPackage(firstJavaPackage);
        JavaPackage currentJavaPackage = firstJavaPackage;
        while (psiPackageIterator.hasNext()) {
            PsiPackage aPsiPackage = psiPackageIterator.next();
            JavaPackage aJavaPackage = new JavaPackage(aPsiPackage.getName(), aPsiPackage);
            javaProject.putToAllPackages(aJavaPackage.getPsiPackage().getQualifiedName(), aJavaPackage);
            currentJavaPackage.addPackage(aJavaPackage);
            currentJavaPackage = aJavaPackage;
        }
        return currentJavaPackage;
    }

    @Override
    protected void addToAllClasses(JavaClass javaClass) {
        javaProject.addToAllClasses(javaClass);
    }

    @Override
    protected Stream<JavaRecursiveElementVisitor> getJavaClassVisitors() {
        return MetricsService.getJavaClassVisitorsForProjectMetricsTree();
    }

    @Override
    protected Stream<JavaRecursiveElementVisitor> getJavaMethodVisitors() {
        return MetricsService.getJavaMethodVisitorsForProjectMetricsTree();
    }

    public void calculateMetrics() {
        javaProject.getAllClasses().forEach(c ->
            MetricsService.getDeferredJavaClassVisitorsForProjectMetricsTree()
                    .forEach(c::accept));

    }
}