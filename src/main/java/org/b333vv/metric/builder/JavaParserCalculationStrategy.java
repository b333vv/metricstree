package org.b333vv.metric.builder;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
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
import org.b333vv.metric.model.javaparser.visitor.type.JavaParserForeignDataProvidersVisitor;
import org.b333vv.metric.model.javaparser.visitor.type.JavaParserHalsteadClassVisitor;
import org.b333vv.metric.model.javaparser.visitor.type.JavaParserNumberOfAddedMethodsVisitor;
import org.b333vv.metric.model.javaparser.visitor.type.JavaParserNumberOfChildrenVisitor;
import org.b333vv.metric.model.javaparser.visitor.type.JavaParserNumberOfOverriddenMethodsVisitor;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import static org.b333vv.metric.model.metric.MetricType.*;
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
                new JavaParserWeightOfAClassVisitor(),
                new JavaParserHalsteadClassVisitor(),
                new JavaParserNumberOfOverriddenMethodsVisitor(),
                new JavaParserNumberOfAddedMethodsVisitor()
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
    public void augment(JavaProject javaProject, Project project, List<CompilationUnit> allUnits, ProgressIndicator indicator) {
        indicator.setText("Calculating metrics with JavaParser");

        TypeSolverProvider typeSolverProvider = new TypeSolverProvider();
        ParserConfiguration parserConfiguration = new ParserConfiguration()
                .setSymbolResolver(new JavaSymbolSolver(typeSolverProvider.getTypeSolver(project, allUnits))); 
        
        // Re-parse ALL units together with the enhanced TypeSolver to ensure shared context
        List<CompilationUnit> enhancedUnits = new ArrayList<>();
        for (CompilationUnit originalUnit : allUnits) {
            try {
                JavaParser enhancedParser = new JavaParser(parserConfiguration);
                ParseResult<CompilationUnit> result = enhancedParser.parse(originalUnit.toString());
                if (result.isSuccessful()) {
                    enhancedUnits.add(result.getResult().get());
                } else {
                    System.err.println("Failed to re-parse unit with enhanced context: " + result.getProblems());
                }
            } catch (Exception e) {
                System.err.println("Exception during enhanced parsing: " + e.getMessage());
            }
        }
        
        System.out.println("Enhanced parsing complete. " + enhancedUnits.size() + " units processed.");

        // Build class declarations from enhanced units
        List<ClassOrInterfaceDeclaration> allClassDeclarations = enhancedUnits.stream()
                .flatMap(cu -> cu.findAll(ClassOrInterfaceDeclaration.class).stream())
                .collect(Collectors.toList());
        
        // Create mapping from class names to their enhanced compilation units
        Map<String, CompilationUnit> enhancedUnitsByClass = new HashMap<>();
        for (CompilationUnit unit : enhancedUnits) {
            String packageName = unit.getPackageDeclaration()
                    .map(pd -> pd.getNameAsString())
                    .orElse("");
            
            unit.findAll(ClassOrInterfaceDeclaration.class).forEach(classDecl -> {
                String key = packageName.isEmpty() ? classDecl.getNameAsString() 
                           : packageName + "." + classDecl.getNameAsString();
                enhancedUnitsByClass.put(key, unit);
                enhancedUnitsByClass.put(classDecl.getNameAsString(), unit); // fallback key
                System.out.println("Mapped enhanced class: " + key);
            });
        }

        javaProject.allClasses().forEach(javaClass -> {
            indicator.setText2("Processing class: " + javaClass.getName());
            if (indicator.isCanceled()) {
                return;
            }
            try {
                // Find the enhanced compilation unit for this class
                CompilationUnit cu = null;
                
                // Try with package qualification first  
                String className = javaClass.getName();
                String packageName = ApplicationManager.getApplication().runReadAction((Computable<String>) () -> {
                    PsiClass psiClass = javaClass.getPsiClass();
                    if (psiClass.getContainingFile() instanceof PsiJavaFile) {
                        return ((PsiJavaFile) psiClass.getContainingFile()).getPackageName();
                    }
                    return "";
                });
                
                String qualifiedKey = packageName.isEmpty() ? className : packageName + "." + className;
                cu = enhancedUnitsByClass.get(qualifiedKey);
                
                // Fallback to simple class name
                if (cu == null) {
                    cu = enhancedUnitsByClass.get(className);
                }
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
                                    visitor.visit(classDeclaration, classMetricConsumer);
                                }
                                // Handle context-dependent visitors separately
                                new JavaParserNumberOfChildrenVisitor(allClassDeclarations).visit(classDeclaration, classMetricConsumer);
                                new JavaParserForeignDataProvidersVisitor(allClassDeclarations).visit(classDeclaration, classMetricConsumer);

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
                                                calculateDerivativeMethodMetrics(javaMethod);
                                            });
                                });
                                calculateDerivativeClassMetrics(javaClass);
                            });
                }
            } catch (Exception e) {
                System.err.println("Failed to process class " + javaClass.getName() + ": " + e.getMessage());
            }
        });
    }

    private void calculateDerivativeClassMetrics(JavaClass javaClass) {
        // CLOC Calculation
        Value totalLOCValue = javaClass.methods()
                .map(m -> {
                    Metric metric = m.metric(LOC);
                    return (metric != null && metric.getJavaParserValue() != null) ? metric.getJavaParserValue() : Value.UNDEFINED;
                })
                .reduce(Value.ZERO, (acc, next) -> {
                    if (acc == Value.UNDEFINED || next == Value.UNDEFINED) {
                        return Value.UNDEFINED;
                    }
                    return acc.plus(next);
                });
        Metric clocMetric = javaClass.metric(CLOC);
        if (clocMetric != null) {
            clocMetric.setJavaParserValue(totalLOCValue);
        }

        // CCC Calculation
        long cognitiveComplexity = javaClass.methods()
                .mapToLong(javaMethod -> {
                    Metric ccmMetric = javaMethod.metric(CCM);
                    if (ccmMetric != null && ccmMetric.getJavaParserValue() != null && ccmMetric.getJavaParserValue() != Value.UNDEFINED) {
                        return ccmMetric.getJavaParserValue().longValue();
                    }
                    return 0L;
                })
                .sum();

        Metric cccMetric = javaClass.metric(CCC);
        if (cccMetric != null) {
            cccMetric.setJavaParserValue(Value.of(cognitiveComplexity));
        }

        // CMI Calculation
        Metric chvlMetric = javaClass.metric(CHVL);
        Value halsteadVolumeValue = (chvlMetric != null && chvlMetric.getJavaParserValue() != null)
                ? chvlMetric.getJavaParserValue() : Value.UNDEFINED;

        Value totalCCValue = javaClass.methods()
                .map(m -> {
                    Metric metric = m.metric(CC);
                    return (metric != null && metric.getJavaParserValue() != null) ? metric.getJavaParserValue() : Value.UNDEFINED;
                })
                .reduce(Value.ZERO, (acc, next) -> {
                    if (acc == Value.UNDEFINED || next == Value.UNDEFINED) {
                        return Value.UNDEFINED;
                    }
                    return acc.plus(next);
                });

        // We can reuse totalLOCValue from the CLOC calculation above.

        Metric cmiMetric = javaClass.metric(CMI);
        if (cmiMetric != null) {
            if (halsteadVolumeValue == Value.UNDEFINED || totalCCValue == Value.UNDEFINED || totalLOCValue == Value.UNDEFINED
                || halsteadVolumeValue.doubleValue() <= 0.0 || totalCCValue.longValue() <= 0L || totalLOCValue.longValue() <= 0L) {
                cmiMetric.setJavaParserValue(Value.UNDEFINED);
            } else {
                double halsteadVolume = halsteadVolumeValue.doubleValue();
                long cyclomaticComplexity = totalCCValue.longValue();
                long linesOfCode = totalLOCValue.longValue();

                double maintainabilityIndex = Math.max(0.0, (171.0 - 5.2 * Math.log(halsteadVolume)
                            - 0.23 * cyclomaticComplexity
                            - 16.2 * Math.log((double) linesOfCode)) * 100.0 / 171.0);
                cmiMetric.setJavaParserValue(Value.of(maintainabilityIndex));
            }
        }
    }

    private void calculateDerivativeMethodMetrics(JavaMethod javaMethod) {
        Metric hvlMetric = javaMethod.metric(HVL);
        Metric ccMetric = javaMethod.metric(CC);
        Metric locMetric = javaMethod.metric(LOC);

        Value halsteadVolume = (hvlMetric != null && hvlMetric.getJavaParserValue() != null) ? hvlMetric.getJavaParserValue() : Value.UNDEFINED;
        Value cyclomaticComplexity = (ccMetric != null && ccMetric.getJavaParserValue() != null) ? ccMetric.getJavaParserValue() : Value.UNDEFINED;
        Value linesOfCode = (locMetric != null && locMetric.getJavaParserValue() != null) ? locMetric.getJavaParserValue() : Value.UNDEFINED;

        Metric mmiMetric = javaMethod.metric(MMI);
        if (mmiMetric != null) {
            if (halsteadVolume == Value.UNDEFINED || cyclomaticComplexity == Value.UNDEFINED || linesOfCode == Value.UNDEFINED) {
                mmiMetric.setJavaParserValue(Value.UNDEFINED);
            } else {
                double hvl = halsteadVolume.doubleValue();
                long cc = cyclomaticComplexity.longValue();
                long loc = linesOfCode.longValue();

                double maintainabilityIndex = 0.0;
                if (hvl > 0.0 && cc > 0L && loc > 0L) {
                    maintainabilityIndex = Math.max(0.0, (171.0 - 5.2 * Math.log(hvl)
                            - 0.23 * Math.log((double) cc)
                            - 16.2 * Math.log((double) loc)) * 100.0 / 171.0);
                }
                mmiMetric.setJavaParserValue(Value.of(maintainabilityIndex));
            }
        }
    }

    /**
     * Parse a compilation unit for a given JavaClass, handling both file-based and string-based parsing.
     * In test environments with temp filesystem, falls back to string-based parsing.
     */
    private CompilationUnit parseClassCompilationUnit(JavaClass javaClass, JavaParser javaParser) {
        return ApplicationManager.getApplication().runReadAction((Computable<CompilationUnit>) () -> {
            try {
                PsiClass psiClass = javaClass.getPsiClass();
                VirtualFile virtualFile = psiClass.getContainingFile().getVirtualFile();
                String filePath = virtualFile.getPath();
                
                // Try file-based parsing first (for production environment)
                try {
                    return javaParser.parse(Paths.get(filePath)).getResult().orElse(null);
                } catch (Exception e) {
                    // Fall back to string-based parsing (for test environment)
                    System.out.println("File-based parsing failed, trying string-based parsing: " + e.getMessage());
                    
                    try {
                        String sourceCode = new String(virtualFile.contentsToByteArray(), virtualFile.getCharset());
                        System.out.println("Source code length: " + sourceCode.length() + " characters");
                        ParseResult<CompilationUnit> parseResult = javaParser.parse(sourceCode);
                        if (parseResult.isSuccessful()) {
                            System.out.println("String-based parsing successful for: " + javaClass.getName());
                            return parseResult.getResult().orElse(null);
                        } else {
                            System.err.println("String-based parsing failed for " + javaClass.getName() + ": " + parseResult.getProblems());
                            return null;
                        }
                    } catch (Exception stringParseException) {
                        System.err.println("Both file and string parsing failed for " + javaClass.getName() + ": " + stringParseException.getMessage());
                        return null;
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to parse class " + javaClass.getName() + ": " + e.getMessage());
                return null;
            }
        });
    }

    /**
     * Find the compilation unit that contains the given JavaClass from the pre-indexed map.
     */
    private CompilationUnit findCompilationUnitForClass(JavaClass javaClass, Map<String, CompilationUnit> unitsByClass) {
        return ApplicationManager.getApplication().runReadAction((Computable<CompilationUnit>) () -> {
            try {
                PsiClass psiClass = javaClass.getPsiClass();
                String className = psiClass.getName();
                String packageName = "";
                
                // Get package name if available
                if (psiClass.getContainingFile() instanceof PsiJavaFile) {
                    PsiJavaFile javaFile = (PsiJavaFile) psiClass.getContainingFile();
                    packageName = javaFile.getPackageName();
                }
                
                // Create the key to look up the compilation unit
                String key = packageName.isEmpty() ? className : packageName + "." + className;
                CompilationUnit unit = unitsByClass.get(key);
                
                if (unit != null) {
                    return unit;
                }
                
                // Fallback: try just the class name
                unit = unitsByClass.get(className);
                if (unit != null) {
                    return unit;
                }
                
                // Last resort: use the original parsing method
                return parseClassCompilationUnit(javaClass, new JavaParser());
            } catch (Exception e) {
                System.err.println("Failed to find compilation unit for class " + javaClass.getName() + ": " + e.getMessage());
                return null;
            }
        });
    }
}