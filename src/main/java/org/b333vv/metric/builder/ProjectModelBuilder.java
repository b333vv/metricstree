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

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.impl.file.PsiPackageImpl;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.visitor.method.JavaMethodVisitor;
import org.b333vv.metric.model.visitor.type.HalsteadClassVisitor;
import org.b333vv.metric.model.visitor.type.JavaClassVisitor;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.code.JavaFile;
import org.b333vv.metric.model.code.JavaPackage;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.model.util.ClassUtils;
import org.b333vv.metric.service.CacheService;
import org.b333vv.metric.ui.settings.composition.ClassMetricsTreeSettings;
import org.b333vv.metric.ui.settings.composition.MetricsTreeSettingsStub;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
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

    public void addJavaFileToJavaProject(@NotNull PsiJavaFile psiJavaFile) {
        findOrCreateJavaPackage(psiJavaFile).addFile(createJavaFile(psiJavaFile));
    }

    @Override
    protected JavaFile createJavaFile(@NotNull PsiJavaFile psiJavaFile) {
        Project project = psiJavaFile.getProject();
        JavaFile javaFile = project.getService(CacheService.class).getJavaFile(psiJavaFile.getVirtualFile());
        if (javaFile != null) {
            javaFile.classes().forEach(c -> {
                addToAllClasses(c);
                addInnerClassesToAllClasses(c);
            });
            return javaFile;
        }
        javaFile = new JavaFile(psiJavaFile.getName());
        for (PsiClass psiClass : psiJavaFile.getClasses()) {
            JavaClass javaClass = new JavaClass(psiClass);

//            classVisitors().forEach(javaClass::accept);

            project.getService(ClassMetricsTreeSettings.class).getMetricsList().stream()
                    .filter(MetricsTreeSettingsStub::isNeedToConsider)
                    .map(m -> m.getType().visitor())
                    .filter(m -> m instanceof JavaClassVisitor)
                    .forEach(javaClass::accept);

            HalsteadClassVisitor halsteadClassVisitor = new HalsteadClassVisitor();
            javaClass.accept(halsteadClassVisitor);

            javaFile.addClass(javaClass);
            buildConstructors(javaClass);
            buildMethods(javaClass);
            buildInnerClasses(psiClass, javaClass);

            addMaintainabilityIndexForClass(javaClass);
            addLinesOfCodeIndexForClass(javaClass);

            addCognitiveComplexityForClass(javaClass);

            addToAllClasses(javaClass);
        }
        project.getService(CacheService.class).addJavaFile(psiJavaFile.getVirtualFile(), javaFile);
        return javaFile;
    }

    private void addInnerClassesToAllClasses(JavaClass javaClass) {
        javaClass.innerClasses().forEach(c -> {
            addToAllClasses(c);
            addInnerClassesToAllClasses(c);
        });
    }

    public JavaPackage findOrCreateJavaPackage(@NotNull PsiJavaFile psiJavaFile) {
        List<PsiPackage> packageList = ClassUtils.getPackagesRecursive(psiJavaFile);
        if (javaProject.allPackagesIsEmpty()) {
            return makeNewRootJavaPackage(packageList);
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
                return makeNewRootJavaPackage(packageList);
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
    private JavaPackage makeNewRootJavaPackage(@NotNull List<PsiPackage> packageList) {
        Iterator<PsiPackage> psiPackageIterator = packageList.iterator();
        JavaPackage firstJavaPackage;
        if (!psiPackageIterator.hasNext()) {
            firstJavaPackage = new JavaPackage("", new PsiPackageImpl(null, ""));
            javaProject.putToAllPackages("", firstJavaPackage);
        } else {
            PsiPackage firstPsiPackage = psiPackageIterator.next();
            firstJavaPackage = new JavaPackage(firstPsiPackage.getName(), firstPsiPackage);
            javaProject.putToAllPackages(firstJavaPackage.getPsiPackage().getQualifiedName(), firstJavaPackage);
        }
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
    protected void addToAllClasses(@NotNull JavaClass javaClass) {
        javaProject.addToAllClasses(javaClass);
    }

//    @Override
//    protected Stream<JavaRecursiveElementVisitor> classVisitors() {
//        return MetricsService.classVisitorsForProjectMetricsTree();
//    }

    @Override
    protected Stream<JavaRecursiveElementVisitor> classVisitors() {
        return Arrays.stream(MetricType.values())
                .map(MetricType::visitor)
                .filter(m -> m instanceof JavaClassVisitor);
    }

//

//    @Override
//    protected Stream<JavaRecursiveElementVisitor> methodVisitors() {
//        return MetricsService.methodsVisitorsForProjectMetricsTree();
//    }

    @Override
    protected Stream<JavaRecursiveElementVisitor> methodVisitors() {
        return Arrays.stream(MetricType.values())
                .map(MetricType::visitor)
                .filter(m -> m instanceof JavaMethodVisitor);
    }

//    public void calculateDeferredMetrics() {
//        javaProject.allClasses().forEach(c -> {
//            MetricsService.getDeferredMetricTypes().forEach(t -> c.accept(t.visitor()));
//        });
//    }
}