package org.b333vv.metric.research;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import org.b333vv.metric.builder.DependenciesBuilder;
import org.b333vv.metric.builder.DependenciesCalculator;
import org.b333vv.metric.builder.JavaParserCalculationStrategy;
import org.b333vv.metric.builder.PsiCalculationStrategy;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.service.CacheService;

import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

public abstract class MetricVerificationTest extends BasePlatformTestCase {

    protected JavaProject javaProject;

    protected PsiJavaFile setupTest(String sourcePath) {
        System.out.println("=== SETUP TEST STARTED ===");
        try {
            // The test data path is configured via the getTestDataPath() override
            PsiJavaFile psiJavaFile = (PsiJavaFile) myFixture.configureByFile(sourcePath);
            System.out.println("Successfully configured file: " + sourcePath);

            EmptyProgressIndicator indicator = new EmptyProgressIndicator();
            
            // Debug: Log the configured file
            System.out.println("Configured test file: " + psiJavaFile.getName());
            System.out.println("File classes: ");
            for (var psiClass : psiJavaFile.getClasses()) {
                System.out.println("  - " + psiClass.getName());
            }
            
            // Step 1: Build dependencies first (required by CBO and other coupling metrics)
            System.out.println("Building dependencies...");
            AnalysisScope scope = new AnalysisScope(getProject());
            scope.setIncludeTestSource(false);
            DependenciesBuilder dependenciesBuilder = new DependenciesBuilder();
            DependenciesCalculator dependenciesCalculator = new DependenciesCalculator(scope, dependenciesBuilder);
            DependenciesBuilder builtDependencies = dependenciesCalculator.calculateDependencies(indicator);
            
            // Cache the dependencies for the visitors to use
            CacheService cacheService = getProject().getService(CacheService.class);
            cacheService.putUserData(CacheService.DEPENDENCIES, builtDependencies);
            
            System.out.println("Dependencies built and cached");

            // Step 2: Calculate PSI-based metrics
            System.out.println("Starting PSI calculation...");
            PsiCalculationStrategy psiCalculationStrategy = new PsiCalculationStrategy();
            javaProject = psiCalculationStrategy.calculate(getProject(), indicator);
            
            if (javaProject == null) {
                System.out.println("ERROR: PSI calculation returned null javaProject!");
                throw new RuntimeException("PSI calculation failed - javaProject is null");
            }
            
            System.out.println("PSI calculation completed. Classes found: " + javaProject.allClasses().count());
            javaProject.allClasses().forEach(javaClass -> {
                System.out.println("  - Class: " + javaClass.getName() + ", Metrics: " + javaClass.metrics().count());
            });

            // Step 3: Augment with JavaParser-based metrics
            System.out.println("Starting JavaParser augmentation...");
            JavaParserCalculationStrategy javaParserCalculationStrategy = new JavaParserCalculationStrategy();
            
            // Parse all compilation units from the test project for enhanced type resolution
            List<CompilationUnit> testUnits = parseTestCompilationUnits();
            System.out.println("Parsed " + testUnits.size() + " compilation units for test.");
            
            javaParserCalculationStrategy.augment(javaProject, getProject(), testUnits, indicator);
            
            System.out.println("JavaParser augmentation completed");
            System.out.println("=== SETUP TEST COMPLETED SUCCESSFULLY ===");

            return psiJavaFile;
        } catch (Exception e) {
            System.out.println("ERROR in setupTest: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private Optional<Metric> getMetric(String className, MetricType metricType) {
        if (javaProject == null) {
            System.out.println("ERROR: javaProject is null in getMetric. Setup was not called or failed.");
            throw new IllegalStateException("JavaProject is null - test setup failed");
        }
        return javaProject.allClasses()
                .filter(javaClass -> javaClass.getName().equals(className))
                .findFirst()
                .flatMap(javaClass -> javaClass.metrics()
                        .filter(metric -> metric.getType().equals(metricType))
                        .findFirst());
    }

    protected Value getPsiValue(String className, MetricType metricType) {
        return getMetric(className, metricType)
                .map(Metric::getPsiValue)
                .orElse(Value.UNDEFINED);
    }

    protected Value getJavaParserValue(String className, MetricType metricType) {
        return getMetric(className, metricType)
                .map(Metric::getJavaParserValue)
                .orElse(Value.UNDEFINED);
    }

    @Override
    protected String getTestDataPath() {
        return "metric-verification-data/src/main/java/";
    }

    /**
     * Parse all compilation units from the test project to enable enhanced type resolution.
     */
    private List<CompilationUnit> parseTestCompilationUnits() {
        List<CompilationUnit> units = new ArrayList<>();
        JavaParser javaParser = new JavaParser();
        
        try {
            // Parse the files that were added to the test project directly
            // For CBOAlignmentVerificationTest, this includes ClassA and ClassB
            for (String fileName : List.of("ClassA.java", "ClassB.java")) {
                try {
                    com.intellij.openapi.vfs.VirtualFile virtualFile = myFixture.findFileInTempDir("com/test/" + fileName);
                    if (virtualFile != null) {
                        PsiFile psiFile = myFixture.getPsiManager().findFile(virtualFile);
                        if (psiFile != null) {
                            ParseResult<CompilationUnit> result = javaParser.parse(psiFile.getText());
                            if (result.isSuccessful()) {
                                units.add(result.getResult().get());
                                System.out.println("Parsed temp file: " + fileName);
                            } else {
                                System.err.println("Failed to parse " + fileName + ": " + result.getProblems());
                            }
                        } else {
                            System.err.println("Could not get PsiFile for: com/test/" + fileName);
                        }
                    } else {
                        System.err.println("Could not find temp file: com/test/" + fileName);
                    }
                } catch (Exception e) {
                    System.err.println("Exception parsing " + fileName + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error in parseTestCompilationUnits: " + e.getMessage());
        }
        
        return units;
    }
}
