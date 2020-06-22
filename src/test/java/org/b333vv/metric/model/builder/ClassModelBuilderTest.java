/*
 * Copyright 2020 b333vv
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.b333vv.metric.model.builder;

import com.intellij.psi.PsiJavaFile;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.apache.commons.io.FilenameUtils;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.code.JavaFile;
import org.b333vv.metric.model.code.JavaMethod;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.util.MetricsService;
import org.b333vv.metric.util.MetricsUtils;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.b333vv.metric.model.metric.MetricType.*;

public class ClassModelBuilderTest extends LightJavaCodeInsightFixtureTestCase {
    private JavaFile javaFile;
    private String projectName;
    private JavaClass javaClass;
    private Map<MetricType, Metric> classMetrics, methodMetrics;
    private Map<String, JavaMethod> methods;
    private JavaMethod removeNode;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MetricsUtils.setProject(getProject());
        MetricsService.init(getProject());
        myFixture.configureByFiles("Object.java", "HashMap.java", "AbstractMap.java");
        PsiJavaFile psiJavaFile = (PsiJavaFile) myFixture.findClass("java.util.HashMap").getContainingFile();
        projectName = FilenameUtils.getBaseName(psiJavaFile.getName());
        ClassModelBuilder classModelBuilder = new ClassModelBuilder();
        javaFile = classModelBuilder.buildJavaFile(psiJavaFile);
        javaClass = javaFile.classes().findFirst().get();
        classMetrics = javaClass.metrics().collect(Collectors.toMap(Metric::getType, Function.identity()));
        methods = javaClass.methods().collect(Collectors.toMap(JavaMethod::getName, Function.identity()));
        removeNode = methods.get("removeNode(int, Object, Object, boolean, boolean)");
        methodMetrics = removeNode.metrics().collect(Collectors.toMap(Metric::getType, Function.identity()));
    }

    @Override
    protected String getTestDataPath() {
        return "testData";
    }

    public void testClassMetricsCount() {
        assertEquals(13, classMetrics.size());
    }

    public void testDepthOfInheritanceTreeMetricValue() {
        assertEquals(Value.of(1), classMetrics.get(DIT).getValue());
    }

    public void testNumberOfChildrenMetricValue() {
        assertEquals(Value.of(0), classMetrics.get(NOC).getValue());
    }

    public void testResponseForClassMetricValue() {
        assertEquals(Value.of(59), classMetrics.get(RFC).getValue());
    }

    public void testLackOfCohesionOfMethodsMetricValue() {
        assertEquals(Value.of(5), classMetrics.get(LCOM).getValue());
    }

    public void testWeightedMethodCountMetricValue() {
        assertEquals(Value.of(263), classMetrics.get(WMC).getValue());
    }

    public void testNumberOfAddedMethodsMetricValue() {
        assertEquals(Value.of(47), classMetrics.get(NOAM).getValue());
    }

    public void testNumberOfAttributesMetricValue() {
        assertEquals(Value.of(13), classMetrics.get(NOA).getValue());
    }

    public void testNumberOfOperationsMetricValue() {
        assertEquals(Value.of(51), classMetrics.get(NOO).getValue());
    }

    public void testNumberOfOverriddenMethodsMetricValue() {
        assertEquals(Value.of(0), classMetrics.get(NOOM).getValue());
    }

    public void testNumberOfAttributesAndMethodsMetricValue() {
        assertEquals(Value.of(64), classMetrics.get(SIZE2).getValue());
    }

    public void testNumberOfMethodsMetricValue() {
        assertEquals(Value.of(51), classMetrics.get(NOM).getValue());
    }

    public void testDataAbstractionCouplingMetricValue() {
        assertEquals(Value.of(1), classMetrics.get(DAC).getValue());
    }

    public void testMessagePassingCouplingMetricValue() {
        assertEquals(Value.of(51), classMetrics.get(MPC).getValue());
    }

    public void testMethodsCount() {
        assertEquals(51, methods.size());
    }

    public void testMethodMetricsCount() {
        assertEquals(6, methodMetrics.size());
    }

    public void testLinesOfCodeMetricValue() {
        assertEquals(Value.of(50), methodMetrics.get(LOC).getValue());
    }

    public void testConditionNestingDepthMetricValue() {
        assertEquals(Value.of(4), methodMetrics.get(CND).getValue());
    }

    public void testLoopNestingDepthMetricValue() {
        assertEquals(Value.of(1), methodMetrics.get(LND).getValue());
    }

    public void testMcCabeCyclomaticComplexityMetricValue() {
        assertEquals(Value.of(22), methodMetrics.get(CC).getValue());
    }

    public void testNumberOfLoopsMetricValue() {
        assertEquals(Value.of(1), methodMetrics.get(NOL).getValue());
    }
}