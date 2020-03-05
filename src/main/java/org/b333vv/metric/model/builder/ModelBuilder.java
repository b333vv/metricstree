package org.b333vv.metric.model.builder;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.code.JavaMethod;
import org.b333vv.metric.model.code.JavaPackage;
import org.b333vv.metric.util.MetricsService;

import java.util.stream.Stream;

public abstract class ModelBuilder {

    protected void createJavaClass(JavaPackage javaPackage, PsiJavaFile psiJavaFile) {
        for (PsiClass psiClass : psiJavaFile.getClasses()) {
            JavaClass javaClass = new JavaClass(psiClass);
            getJavaClassVisitors().forEach(v -> javaClass.accept(v));
            javaPackage.addClass(javaClass);
            buildConstructors(javaClass);
            buildMethods(javaClass);
            buildInnerClasses(psiClass, javaClass);
            addClassToClassesSet(javaClass);
        }
    }

    protected void buildConstructors(JavaClass javaClass) {
        for (PsiMethod aConstructor : javaClass.getPsiClass().getConstructors()) {
            JavaMethod javaMethod = new JavaMethod(aConstructor);
            javaClass.addMethod(javaMethod);
            getJavaMethodVisitors().forEach(v -> javaMethod.accept(v));
        }
    }

    protected void buildMethods(JavaClass javaClass) {
        for (PsiMethod aMethod : javaClass.getPsiClass().getMethods()) {
            JavaMethod javaMethod = new JavaMethod(aMethod);
            javaClass.addMethod(javaMethod);
            getJavaMethodVisitors().forEach(v -> javaMethod.accept(v));
        }
    }

    protected void buildInnerClasses(PsiClass aClass, JavaClass parentClass) {
        for (PsiClass psiClass : aClass.getInnerClasses()) {
            JavaClass javaClass = new JavaClass(psiClass);
            parentClass.addClass(javaClass);
            getJavaClassVisitors().forEach(v -> javaClass.accept(v));
            buildConstructors(javaClass);
            buildMethods(javaClass);
            addClassToClassesSet(javaClass);
            buildInnerClasses(psiClass, javaClass);
        }
    }

    protected void addClassToClassesSet(JavaClass javaClass) {}

    protected Stream<JavaRecursiveElementVisitor> getJavaClassVisitors() {
        return MetricsService.getJavaClassVisitorsForClassMetricsTree();
    }
    protected Stream<JavaRecursiveElementVisitor> getJavaMethodVisitors() {
        return MetricsService.getJavaMethodVisitorsForClassMetricsTree();
    }
}