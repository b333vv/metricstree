package org.b333vv.metric.model.builder;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiPackage;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.code.JavaPackage;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.model.metric.util.ClassUtils;
import org.b333vv.metric.util.MetricsService;

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
        assert packageList != null;
        if (packageList.isEmpty()) {
            return null;
        }
        if (javaProject.getPackagesMap().isEmpty()) {
            Iterator<PsiPackage> psiPackageIterator = packageList.iterator();
            PsiPackage firstPsiPackage = psiPackageIterator.next();
            JavaPackage firstJavaPackage = new JavaPackage(firstPsiPackage.getName(), firstPsiPackage);
            javaProject.getPackagesMap().put(firstJavaPackage.getPsiPackage().getQualifiedName(), firstJavaPackage);
            javaProject.addPackage(firstJavaPackage);
            JavaPackage currentJavaPackage = firstJavaPackage;
            while (psiPackageIterator.hasNext()) {
                PsiPackage aPsiPackage = psiPackageIterator.next();
                JavaPackage aJavaPackage = new JavaPackage(aPsiPackage.getName(), aPsiPackage);
                javaProject.getPackagesMap().put(aJavaPackage.getPsiPackage().getQualifiedName(), aJavaPackage);
                currentJavaPackage.addPackage(aJavaPackage);
                currentJavaPackage = aJavaPackage;
            }
            return currentJavaPackage;
        } else {
            Collections.reverse(packageList);
            PsiPackage[] psiPackages = packageList.toArray(new PsiPackage[0]);
            int j = 0;
            JavaPackage aPackage = null;
            for (int i = 0; i < psiPackages.length; i++) {
                JavaPackage javaPackage = javaProject.getPackagesMap().get(psiPackages[i].getQualifiedName());
                if (javaPackage != null) {
                    aPackage = javaProject.getPackagesMap().get(psiPackages[i].getQualifiedName());
                    j = i;
                    break;
                }
            }
            for (int i = j - 1; i >= 0; i--) {
                JavaPackage newPackage = new JavaPackage(psiPackages[i].getName(), psiPackages[i]);
                javaProject.getPackagesMap().put(newPackage.getPsiPackage().getQualifiedName(), newPackage);
                aPackage.addPackage(newPackage);
                aPackage = newPackage;
            }
            return aPackage;
        }
    }

    @Override
    protected void addClassToClassesSet(JavaClass javaClass) {
        javaProject.addClassToClassesSet(javaClass);
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