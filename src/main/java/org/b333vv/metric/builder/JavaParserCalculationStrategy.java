package org.b333vv.metric.builder;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.code.JavaMethod;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.model.javaparser.util.TypeSolverProvider;
import org.b333vv.metric.model.javaparser.visitor.JavaParserClassVisitor;
import org.b333vv.metric.model.javaparser.visitor.JavaParserMethodVisitor;
import org.b333vv.metric.model.javaparser.visitor.method.*;
import org.b333vv.metric.model.javaparser.visitor.type.*;
import org.b333vv.metric.model.metric.Metric;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class JavaParserCalculationStrategy implements MetricCalculationStrategy {

    private final List<JavaParserClassVisitor> classVisitors;
    private final List<JavaParserMethodVisitor> methodVisitors;

    public JavaParserCalculationStrategy() {
        classVisitors = List.of(
                new JavaParserCouplingBetweenObjectsVisitor(),
                new JavaParserDepthOfInheritanceTreeVisitor(),
                new JavaParserLackOfCohesionOfMethodsVisitor(),
                new JavaParserNumberOfMethodsVisitor(),
                new JavaParserNumberOfAttributesVisitor(),
                new JavaParserNumberOfPublicAttributesVisitor(),
                new JavaParserNumberOfAccessorMethodsVisitor(),
                new JavaParserResponseForClassVisitor(),
                new JavaParserTightClassCohesionVisitor(),
                new JavaParserAccessToForeignDataVisitor(),
                new JavaParserDataAbstractionCouplingVisitor(),
                new JavaParserMessagePassingCouplingVisitor(),
                new JavaParserLocalityOfAttributeAccessesVisitor(),
                new JavaParserNonCommentingSourceStatementsVisitor(),
                new JavaParserNumberOfAttributesAndMethodsVisitor(),
                new JavaParserNumberOfOperationsVisitor(),
                new JavaParserWeightedMethodCountVisitor(),
                new JavaParserWeightOfAClassVisitor()
        );
        methodVisitors = List.of(
                new JavaParserNumberOfLoopsVisitor(),
                new JavaParserLinesOfCodeVisitor(),
                new JavaParserNumberOfParametersVisitor(),
                new JavaParserMcCabeCyclomaticComplexityVisitor(),
                new JavaParserCognitiveComplexityVisitor(),
                new JavaParserConditionNestingDepthVisitor(),
                new JavaParserLoopNestingDepthVisitor(),
                new JavaParserMaximumNestingDepthVisitor(),
                new JavaParserCouplingDispersionVisitor(),
                new JavaParserCouplingIntensityVisitor(),
                new JavaParserMethodCognitiveComplexityVisitor(),
                new JavaParserMethodComplexityVisitor(),
                new JavaParserNumberOfAccessedVariablesVisitor(),
                new JavaParserHalsteadMethodVisitor()
        );
    }

    @Override
    public JavaProject calculate(Project project, ProgressIndicator indicator) {
        return new JavaProject(project.getName());
    }

    @Override
    public void augment(JavaProject javaProject, Project project, ProgressIndicator indicator) {
        indicator.setText("Calculating metrics with JavaParser");

        TypeSolverProvider typeSolverProvider = new TypeSolverProvider();
        ParserConfiguration parserConfiguration = new ParserConfiguration()
                .setSymbolResolver(new JavaSymbolSolver(typeSolverProvider.getTypeSolver(project)));
        JavaParser javaParser = new JavaParser(parserConfiguration);

        List<ClassOrInterfaceDeclaration> allClassDeclarations = javaProject.allClasses()
                .map(javaClass -> {
                    String filePath = ApplicationManager.getApplication().runReadAction((Computable<String>) () -> {
                        PsiClass psiClass = javaClass.getPsiClass();
                        return psiClass.getContainingFile().getVirtualFile().getPath();
                    });
                    try {
                        return javaParser.parse(Paths.get(filePath)).getResult().orElse(null);
                    } catch (IOException e) {
                        return null;
                    }
                })
                .filter(cu -> cu != null)
                .flatMap(cu -> cu.findAll(ClassOrInterfaceDeclaration.class).stream())
                .collect(Collectors.toList());

        javaProject.allClasses().forEach(javaClass -> {
            indicator.setText2("Processing class: " + javaClass.getName());
            if (indicator.isCanceled()) {
                return;
            }
            String filePath = ApplicationManager.getApplication().runReadAction((Computable<String>) () -> {
                PsiClass psiClass = javaClass.getPsiClass();
                return psiClass.getContainingFile().getVirtualFile().getPath();
            });
            try {
                CompilationUnit cu = javaParser.parse(Paths.get(filePath)).getResult().orElse(null);
                if (cu != null) {
                    cu.findFirst(ClassOrInterfaceDeclaration.class, c -> c.getNameAsString().equals(javaClass.getName()))
                            .ifPresent(classDeclaration -> {
                                Consumer<Metric> classMetricConsumer = (m) -> {
                                    Metric metric = javaClass.metric(m.getType());
                                    if (metric != null) {
                                        metric.setJavaParserValue(m.getValue());
                                    }
                                };

                                for (JavaParserClassVisitor visitor : classVisitors) {
                                    if (visitor instanceof JavaParserNumberOfChildrenVisitor) {
                                        new JavaParserNumberOfChildrenVisitor(allClassDeclarations).visit(classDeclaration, classMetricConsumer);
                                    } else if (visitor instanceof JavaParserForeignDataProvidersVisitor) {
                                        new JavaParserForeignDataProvidersVisitor(allClassDeclarations).visit(classDeclaration, classMetricConsumer);
                                    } else {
                                        visitor.visit(classDeclaration, classMetricConsumer);
                                    }
                                }

                                javaClass.methods().forEach(javaMethod -> {
                                    PsiMethod psiMethod = javaMethod.getPsiMethod();
                                    String methodName = ApplicationManager.getApplication().runReadAction((Computable<String>) psiMethod::getName);
                                    int paramCount = ApplicationManager.getApplication().runReadAction((Computable<Integer>) () -> psiMethod.getParameterList().getParametersCount());
                                    classDeclaration.findFirst(MethodDeclaration.class, m -> m.getNameAsString().equals(methodName) &&
                                            m.getParameters().size() == paramCount)
                                            .ifPresent(methodDeclaration -> {
                                                Consumer<Metric> methodMetricConsumer = (m) -> {
                                                    Metric metric = javaMethod.metric(m.getType());
                                                    if (metric != null) {
                                                        metric.setJavaParserValue(m.getValue());
                                                    }
                                                };

                                                for (JavaParserMethodVisitor visitor : methodVisitors) {
                                                    visitor.visit(methodDeclaration, methodMetricConsumer);
                                                }
                                            });
                                });
                            });
                }
            } catch (Exception e) {
                // Log error, e.g., using Logger
            }
        });
    }
}