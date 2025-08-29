package org.b333vv.metric.research;

import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.psi.PsiJavaFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.b333vv.metric.builder.JavaParserCalculationStrategy;
import org.b333vv.metric.builder.PsiCalculationStrategy;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;

import java.util.Optional;

public abstract class MetricVerificationTest extends BasePlatformTestCase {

    protected JavaProject javaProject;

    protected PsiJavaFile setupTest(String sourcePath) {
        // The test data path is configured via the getTestDataPath() override
        PsiJavaFile psiJavaFile = (PsiJavaFile) myFixture.configureByFile(sourcePath);

        // This strategy calculates metrics for the entire project within the fixture's scope.
        // Since we only configured one file, it will effectively process just that file.
        PsiCalculationStrategy psiCalculationStrategy = new PsiCalculationStrategy();
        javaProject = psiCalculationStrategy.calculate(getProject(), new EmptyProgressIndicator());

        // The JavaParser strategy augments the existing project model with its values.
        JavaParserCalculationStrategy javaParserCalculationStrategy = new JavaParserCalculationStrategy();
        javaParserCalculationStrategy.augment(javaProject, getProject(), new EmptyProgressIndicator());

        return psiJavaFile;
    }

    private Optional<Metric> getMetric(String className, MetricType metricType) {
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
