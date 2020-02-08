package org.jacoquev.model.builder;

import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiPackage;
import org.jacoquev.model.code.JavaPackage;
import org.jacoquev.model.code.JavaProject;
import org.jacoquev.model.metric.util.ClassUtils;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ProjectModelBuilder extends ModelBuilder {

    public void addJavaFileToJavaProject(JavaProject javaProject, PsiJavaFile psiJavaFile) {
        JavaPackage javaPackage = findOrCreateJavaPackage(javaProject, psiJavaFile);
        createJavaClass(javaPackage, psiJavaFile);
    }

    private JavaPackage findOrCreateJavaPackage(JavaProject javaProject, PsiJavaFile psiJavaFile) {
        List<PsiPackage> packageList = ClassUtils.getPackagesRecursive(psiJavaFile);
        if (packageList.isEmpty()) {
            return null;
        }
        if (javaProject.getPackageMap().isEmpty()) {
            Iterator<PsiPackage> psiPackageIterator = packageList.iterator();
            PsiPackage firstPsiPackage = psiPackageIterator.next();
            JavaPackage firstJavaPackage = new JavaPackage(firstPsiPackage.getName(), firstPsiPackage);
            javaProject.getPackageMap().put(firstJavaPackage.getPsiPackage().getQualifiedName(), firstJavaPackage);
            javaProject.addPackage(firstJavaPackage);
            JavaPackage currentJavaPackage = firstJavaPackage;
            while (psiPackageIterator.hasNext()) {
                PsiPackage aPsiPackage = psiPackageIterator.next();
                JavaPackage aJavaPackage = new JavaPackage(aPsiPackage.getName(), aPsiPackage);
                javaProject.getPackageMap().put(aJavaPackage.getPsiPackage().getQualifiedName(), aJavaPackage);
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
                JavaPackage javaPackage = javaProject.getPackageMap().get(psiPackages[i].getQualifiedName());
                if (javaPackage != null) {
                    aPackage = javaProject.getPackageMap().get(psiPackages[i].getQualifiedName());
                    j = i;
                    break;
                }
            }
            for (int i = j - 1; i >= 0; i--) {
                JavaPackage newPackage = new JavaPackage(psiPackages[i].getName(), psiPackages[i]);
                javaProject.getPackageMap().put(newPackage.getPsiPackage().getQualifiedName(), newPackage);
                aPackage.addPackage(newPackage);
                aPackage = newPackage;
            }
            return aPackage;
        }
    }
}