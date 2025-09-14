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
import com.intellij.psi.JavaPsiFacade;
import org.b333vv.metric.model.code.*;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.visitor.method.JavaMethodVisitor;
import org.b333vv.metric.model.visitor.type.HalsteadClassVisitor;
import org.b333vv.metric.model.visitor.type.JavaClassVisitor;
import org.b333vv.metric.model.code.ClassElement;
import org.b333vv.metric.model.code.ProjectElement;
import org.b333vv.metric.model.util.ClassUtils;
import org.b333vv.metric.service.CacheService;
import org.b333vv.metric.ui.settings.composition.MetricsTreeSettingsStub;
import org.jetbrains.annotations.NotNull;
import org.b333vv.metric.util.SettingsService;
// Avoid direct Kotlin imports to keep startup safe

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class ProjectModelBuilder extends ModelBuilder {

    private final ProjectElement javaProject;

    public ProjectModelBuilder(ProjectElement javaProject) {
        super();
        this.javaProject = javaProject;
    }

    private PackageElement findOrCreatePackageByFqn(@NotNull Project project, @NotNull String fqn) {
        PackageElement existing = javaProject.getFromAllPackages(fqn);
        if (existing != null) return existing;

        // Try to resolve via JavaPsiFacade
        PsiPackage psiPackage = JavaPsiFacade.getInstance(project).findPackage(fqn);
        if (psiPackage == null) {
            psiPackage = new PsiPackageImpl(null, fqn);
        }
        // Ensure parent hierarchy exists
        String[] parts = fqn.isEmpty() ? new String[0] : fqn.split("\\.");
        PackageElement current;
        if (parts.length == 0) {
            current = javaProject.getFromAllPackages("");
            if (current == null) {
                current = new PackageElement("", new PsiPackageImpl(null, ""));
                javaProject.putToAllPackages("", current);
                javaProject.addPackage(current);
            }
            return current;
        }
        // build from root
        StringBuilder path = new StringBuilder();
        current = javaProject.getFromAllPackages("");
        if (current == null) {
            current = new PackageElement("", new PsiPackageImpl(null, ""));
            javaProject.putToAllPackages("", current);
            javaProject.addPackage(current);
        }
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) path.append('.');
            path.append(parts[i]);
            String qn = path.toString();
            PackageElement next = javaProject.getFromAllPackages(qn);
            if (next == null) {
                PsiPackage p = JavaPsiFacade.getInstance(project).findPackage(qn);
                if (p == null) p = new PsiPackageImpl(null, qn);
                next = new PackageElement(parts[i], p);
                javaProject.putToAllPackages(qn, next);
                current.addPackage(next);
            }
            current = next;
        }
        return current;
    }

    public void addJavaFileToJavaProject(@NotNull PsiJavaFile psiJavaFile) {
        findOrCreateJavaPackage(psiJavaFile).addFile(createJavaFile(psiJavaFile));
    }

    public void addKotlinFileToProjectReflective(@NotNull com.intellij.psi.PsiFile psiFile) {
        try {
            if ("Kotlin".equals(psiFile.getFileType().getName()) || "KOTLIN".equals(psiFile.getFileType().getName())) {
                // Read package FQN via reflection: ktFile.getPackageFqName().toString()
                java.lang.reflect.Method getPkg = psiFile.getClass().getMethod("getPackageFqName");
                Object fqNameObj = getPkg.invoke(psiFile);
                String fqn = String.valueOf(fqNameObj); // FqName#toString
                if (fqn == null || "<root>".equals(fqn) || "ROOT".equals(fqn)) {
                    fqn = ""; // normalize to default package
                }

                // Build FileElement via KotlinModelBuilder
                Class<?> kmbClass = Class.forName("org.b333vv.metric.builder.KotlinModelBuilder");
                java.lang.reflect.Constructor<?> ctor = kmbClass.getConstructor(Project.class);
                Object kmb = ctor.newInstance(((com.intellij.psi.PsiFile) psiFile).getProject());

                java.lang.reflect.Method bridge = kmbClass.getMethod("createKotlinFileBridge", com.intellij.psi.PsiFile.class);
                if (bridge != null) {
                    Object fileEl = bridge.invoke(kmb, psiFile);
                    if (fileEl instanceof org.b333vv.metric.model.code.FileElement) {
                        org.b333vv.metric.model.code.FileElement fe = (org.b333vv.metric.model.code.FileElement) fileEl;
                        PackageElement pkg = findOrCreatePackageByFqn(psiFile.getProject(), fqn);
                        pkg.addFile(fe);
                        // Ensure ProjectElement.allClasses is populated for Kotlin classes
                        long count = fe.classes().peek(javaProject::addToAllClasses).count();
                        psiFile.getProject().getMessageBus().syncPublisher(org.b333vv.metric.event.MetricsEventListener.TOPIC)
                                .printInfo("[ProjectModelBuilder] Kotlin file '" + psiFile.getName() + "' -> added " + count + " classes into package '" + fqn + "'");
                    }
                }
            }
        } catch (ClassNotFoundException | LinkageError e) {
            // Kotlin not available; skip safely
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to add Kotlin file reflectively", e);
        }
    }

    @Override
    protected FileElement createJavaFile(@NotNull PsiJavaFile psiJavaFile) {
        Project project = psiJavaFile.getProject();
        FileElement javaFile = project.getService(CacheService.class).getJavaFile(psiJavaFile.getVirtualFile());
        if (javaFile != null) {
            javaFile.classes().forEach(c -> {
                addToAllClasses(c);
                addInnerClassesToAllClasses(c);
            });
            return javaFile;
        }
        javaFile = new FileElement(psiJavaFile.getName());
        for (PsiClass psiClass : psiJavaFile.getClasses()) {
            ClassElement javaClass = new ClassElement(psiClass);

//            classVisitors().forEach(javaClass::accept);

            project.getService(SettingsService.class).getClassMetricsTreeSettings().getMetricsList().stream()
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

    private void addInnerClassesToAllClasses(ClassElement javaClass) {
        javaClass.innerClasses().forEach(c -> {
            addToAllClasses(c);
            addInnerClassesToAllClasses(c);
        });
    }

    public PackageElement findOrCreateJavaPackage(@NotNull PsiJavaFile psiJavaFile) {
        List<PsiPackage> packageList = ClassUtils.getPackagesRecursive(psiJavaFile);
        if (javaProject.allPackagesIsEmpty()) {
            return makeNewRootJavaPackage(packageList);
        } else {
            Collections.reverse(packageList);
            PsiPackage[] psiPackages = packageList.toArray(new PsiPackage[0]);
            int j = 0;
            PackageElement aPackage = null;
            for (int i = 0; i < psiPackages.length; i++) {
                PackageElement javaPackage = javaProject.getFromAllPackages(psiPackages[i].getQualifiedName());
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
                PackageElement newPackage = new PackageElement(psiPackages[i].getName(), psiPackages[i]);
                javaProject.putToAllPackages(newPackage.getPsiPackage().getQualifiedName(), newPackage);
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
            javaProject.putToAllPackages("", firstJavaPackage);
        } else {
            PsiPackage firstPsiPackage = psiPackageIterator.next();
            firstJavaPackage = new PackageElement(firstPsiPackage.getName(), firstPsiPackage);
            javaProject.putToAllPackages(firstJavaPackage.getPsiPackage().getQualifiedName(), firstJavaPackage);
        }
        javaProject.addPackage(firstJavaPackage);
        PackageElement currentJavaPackage = firstJavaPackage;
        while (psiPackageIterator.hasNext()) {
            PsiPackage aPsiPackage = psiPackageIterator.next();
            PackageElement aJavaPackage = new PackageElement(aPsiPackage.getName(), aPsiPackage);
            javaProject.putToAllPackages(aJavaPackage.getPsiPackage().getQualifiedName(), aJavaPackage);
            currentJavaPackage.addPackage(aJavaPackage);
            currentJavaPackage = aJavaPackage;
        }
        return currentJavaPackage;
    }

    @Override
    protected void addToAllClasses(@NotNull ClassElement javaClass) {
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