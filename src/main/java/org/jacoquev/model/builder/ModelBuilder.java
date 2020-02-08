package org.jacoquev.model.builder;

import com.intellij.psi.*;
import org.jacoquev.model.code.JavaClass;
import org.jacoquev.model.code.JavaMethod;
import org.jacoquev.model.code.JavaPackage;
import org.jacoquev.model.visitor.method.*;
import org.jacoquev.model.visitor.type.*;

import java.util.Set;

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
            new WeightedMethodCountVisitor()
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
        for (PsiClass aClass : psiJavaFile.getClasses()) {
            JavaClass javaClass = new JavaClass(aClass);
            javaPackage.addClass(javaClass);
            classVisitors.stream().forEach(v -> javaClass.accept(v));
            buildConstructors(javaClass);
            buildMethods(javaClass);
            buildInnerClasses(aClass, javaClass);
        }
    }

    protected void buildConstructors(JavaClass javaClass) {
        for (PsiMethod aConstructor : javaClass.getPsiClass().getConstructors()) {
            JavaMethod javaMethod = new JavaMethod(aConstructor);
            javaClass.addMethod(javaMethod);
            methodVisitors.stream().forEach(v -> javaMethod.accept(v));
        }
    }

    protected void buildMethods(JavaClass javaClass) {
        for (PsiMethod aMethod : javaClass.getPsiClass().getMethods()) {
            JavaMethod javaMethod = new JavaMethod(aMethod);
            javaClass.addMethod(javaMethod);
            methodVisitors.stream().forEach(v -> javaMethod.accept(v));
        }
    }

    protected void buildInnerClasses(PsiClass aClass, JavaClass parentClass) {
        for (PsiClass psiClass : aClass.getInnerClasses()) {
            JavaClass javaClass = new JavaClass(psiClass);
            parentClass.addClass(javaClass);
            classVisitors.stream().forEach(v -> javaClass.accept(v));
            buildConstructors(javaClass);
            buildMethods(javaClass);
            buildInnerClasses(psiClass, javaClass);
        }
    }
}