package org.jacoquev.model.builder;

import com.intellij.psi.*;
import org.jacoquev.model.code.JavaClass;
import org.jacoquev.model.code.JavaMethod;
import org.jacoquev.model.code.JavaPackage;
import org.jacoquev.model.visitor.method.*;
import org.jacoquev.model.visitor.type.*;
import org.jacoquev.util.MetricsUtils;

import java.util.Set;
import java.util.stream.Collectors;

public abstract class ModelBuilder {
    private Set<JavaClassVisitor> classVisitors = Set.of(
            new NumberOfAddedMethodsVisitor(),
            new LackOfCohesionOfMethodsVisitor(),
            new DepthOfInheritanceTreeVisitor(),
            new NumberOfAttributesVisitor(),
            new NumberOfChildrenVisitor(),
            new NumberOfOperationsVisitor(),
            new NumberOfOverriddenMethodsVisitor(),
            new ResponseForClassVisitor(),
            new WeightedMethodCountVisitor(),
            new NumberOfAttributesAndMethods()
    );

    private Set<JavaMethodVisitor> methodVisitors = Set.of(
            new LinesOfCodeVisitor(),
            new ConditionNestingDepthVisitor(),
            new LoopNestingDepthVisitor(),
            new McCabeCyclomaticComplexityVisitor(),
            new NumberOfConditionsVisitor(),
            new NumberOfLoopsVisitor()
    );

    protected void createJavaClass(JavaPackage javaPackage, PsiJavaFile psiJavaFile) {
        for (PsiClass psiClass : psiJavaFile.getClasses()) {
            JavaClass javaClass = new JavaClass(psiClass);
            classVisitors.stream().forEach(v -> javaClass.accept(v));
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
            methodVisitors.stream().forEach(v -> javaMethod.accept(v));
            addMethodToMethodsSet(javaMethod);
        }
    }

    protected void buildMethods(JavaClass javaClass) {
        for (PsiMethod aMethod : javaClass.getPsiClass().getMethods()) {
            JavaMethod javaMethod = new JavaMethod(aMethod);
            javaClass.addMethod(javaMethod);
            methodVisitors.stream().forEach(v -> javaMethod.accept(v));
            addMethodToMethodsSet(javaMethod);
        }
    }

    protected void buildInnerClasses(PsiClass aClass, JavaClass parentClass) {
        for (PsiClass psiClass : aClass.getInnerClasses()) {
            JavaClass javaClass = new JavaClass(psiClass);
            parentClass.addClass(javaClass);
            classVisitors.stream().forEach(v -> javaClass.accept(v));
            buildConstructors(javaClass);
            buildMethods(javaClass);
            addClassToClassesSet(javaClass);
            buildInnerClasses(psiClass, javaClass);
        }
    }

    protected void addClassToClassesSet(JavaClass javaClass) {}
    protected void addMethodToMethodsSet(JavaMethod javaMethod) {}
}