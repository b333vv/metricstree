package org.b333vv.metric.research;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.psi.PsiJavaFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
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
            javaParserCalculationStrategy.augment(javaProject, getProject(), indicator);
            
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
}
