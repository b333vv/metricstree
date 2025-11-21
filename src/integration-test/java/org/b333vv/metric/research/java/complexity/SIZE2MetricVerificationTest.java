package org.b333vv.metric.research.java.complexity;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.psi.PsiJavaFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import org.b333vv.metric.builder.DependenciesBuilder;
import org.b333vv.metric.builder.DependenciesCalculator;
import org.b333vv.metric.builder.JavaParserCalculationStrategy;
import org.b333vv.metric.builder.PsiCalculationStrategy;
import org.b333vv.metric.model.code.ProjectElement;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.service.CacheService;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class SIZE2MetricVerificationTest extends BasePlatformTestCase {
    private static final String TEST_DATA_PATH = "com/verification/complexity";
    private static final String BASE_CLASS_NAME = "SIZE2_BaseClass";
    private static final String CHILD_CLASS_NAME = "SIZE2_ChildClass";

    private static final long EXPECTED_BASE_CLASS_SIZE2 = 3; // 1 field + 2 methods (baseField, constructor, baseMethod)
    private static final long EXPECTED_CHILD_CLASS_SIZE2 = 8; // 3 fields + 5 methods (including inherited)

    protected ProjectElement javaProject;

    @Override
    protected String getTestDataPath() {
        return "metric-verification-data/src/main/java/";
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setupTest(TEST_DATA_PATH + "/SIZE2TestCases.java");
    }

    protected PsiJavaFile setupTest(String sourcePath) {
        try {
            // The test data path is configured via the getTestDataPath() override
            PsiJavaFile psiJavaFile = (PsiJavaFile) myFixture.configureByFile(sourcePath);

            EmptyProgressIndicator indicator = new EmptyProgressIndicator();

            // Build dependencies first (required by some metrics)
            AnalysisScope scope = new AnalysisScope(getProject());
            scope.setIncludeTestSource(false);
            DependenciesBuilder dependenciesBuilder = new DependenciesBuilder();
            DependenciesCalculator dependenciesCalculator = new DependenciesCalculator(scope, dependenciesBuilder);
            DependenciesBuilder builtDependencies = dependenciesCalculator.calculateDependencies(indicator);

            // Cache the dependencies for the visitors to use
            CacheService cacheService = getProject().getService(CacheService.class);
            cacheService.putUserData(CacheService.DEPENDENCIES, builtDependencies);

            // Calculate PSI-based metrics
            PsiCalculationStrategy psiCalculationStrategy = new PsiCalculationStrategy();
            javaProject = psiCalculationStrategy.calculate(getProject(), indicator, null);

            // Augment with JavaParser-based metrics
            JavaParserCalculationStrategy javaParserCalculationStrategy = new JavaParserCalculationStrategy();

            // Parse all compilation units from the test project for enhanced type
            // resolution
            List<CompilationUnit> testUnits = parseTestCompilationUnits(sourcePath);

            javaParserCalculationStrategy.augment(javaProject, getProject(), testUnits, indicator);

            return psiJavaFile;
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Parse all compilation units from the test project to enable enhanced type
     * resolution.
     */
    private List<CompilationUnit> parseTestCompilationUnits(String sourcePath) {
        List<CompilationUnit> units = new ArrayList<>();
        JavaParser javaParser = new JavaParser();

        try {
            com.intellij.openapi.vfs.VirtualFile virtualFile = myFixture.findFileInTempDir(sourcePath);
            if (virtualFile != null) {
                com.intellij.psi.PsiFile psiFile = myFixture.getPsiManager().findFile(virtualFile);
                if (psiFile != null) {
                    ParseResult<CompilationUnit> result = javaParser.parse(psiFile.getText());
                    if (result.isSuccessful()) {
                        units.add(result.getResult().get());
                    }
                }
            }
        } catch (Exception e) {
            // Ignore parsing errors
        }

        return units;
    }

    private Optional<Metric> getMetric(String className, MetricType metricType) {
        if (javaProject == null) {
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

    public void testSIZE2CalculationForBaseClass() {
        Value psiResult = getPsiValue(BASE_CLASS_NAME, MetricType.SIZE2);
        Value javaParserResult = getJavaParserValue(BASE_CLASS_NAME, MetricType.SIZE2);

        // Both implementations should count: 1 instance field + 2 instance methods
        // (constructor + baseMethod)
        // Static members should be excluded
        assertThat(psiResult.longValue()).isEqualTo(EXPECTED_BASE_CLASS_SIZE2);
        assertThat(javaParserResult.longValue()).isEqualTo(EXPECTED_BASE_CLASS_SIZE2);
    }

    public void testSIZE2CalculationForChildClass() {
        Value psiResult = getPsiValue(CHILD_CLASS_NAME, MetricType.SIZE2);
        Value javaParserResult = getJavaParserValue(CHILD_CLASS_NAME, MetricType.SIZE2);

        // Both implementations should count:
        // 3 fields (1 inherited + 2 declared) + 5 methods (2 inherited + 3 declared) =
        // 8
        assertThat(psiResult.longValue()).isEqualTo(EXPECTED_CHILD_CLASS_SIZE2);
        assertThat(javaParserResult.longValue()).isEqualTo(EXPECTED_CHILD_CLASS_SIZE2);
    }

    public void testPSICalculatorHandlesInheritedMembers() {
        Value psiResult = getPsiValue(CHILD_CLASS_NAME, MetricType.SIZE2);

        // Verify that the visitor correctly includes inherited members
        assertThat(psiResult.longValue()).isEqualTo(EXPECTED_CHILD_CLASS_SIZE2);
    }

    public void testJavaParserCalculatorHandlesInheritedMembers() {
        Value javaParserResult = getJavaParserValue(CHILD_CLASS_NAME, MetricType.SIZE2);

        // Verify that the visitor correctly includes inherited members
        assertThat(javaParserResult.longValue()).isEqualTo(EXPECTED_CHILD_CLASS_SIZE2);
    }
}