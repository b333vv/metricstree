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

package org.b333vv.metric.builder;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase; // Changed base class
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.util.MetricsUtils;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ClassModelBuilderTest extends BasePlatformTestCase { // Changed base class

    private static final String TEST_CLASS_STRING =
            "package com.example;\n" +
            "\n" +
            "public class TestClass {\n" +
            "    private int field1;\n" +
            "    private String field2;\n" +
            "\n" +
            "    public TestClass(int field1) {\n" + // Constructor (Complexity 1)
            "        this.field1 = field1;\n" +
            "    }\n" +
            "\n" +
            "    public void method1() {\n" + // Method 1 (Complexity 1)
            "        System.out.println(\"Hello\");\n" +
            "    }\n" +
            "\n" +
            "    public int method2(int val) {\n" + // Method 2 (Complexity 1 + 1 for if)
            "        if (val > 0) {\n" +
            "            return val * 2;\n" +
            "        }\n" +
            "        return val;\n" +
            "    }\n" +
            "\n" +
            "    private String helperMethod(String input) {\n" + // Method 3 (Complexity 1)
            "        return \"Helper: \" + input;\n" +
            "    }\n" +
            "}\n";

    private JavaClass javaClassModel;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MetricsUtils.setCurrentProject(getProject()); // Still need this for builder context

        myFixture.configureByText("TestClass.java", TEST_CLASS_STRING);
        PsiJavaFile psiJavaFile = (PsiJavaFile) myFixture.getFile();
        PsiClass psiClass = psiJavaFile.getClasses()[0];

        ClassModelBuilder classModelBuilder = new ClassModelBuilder(getProject());
        // Build model for the specific PsiClass, not the whole file
        javaClassModel = classModelBuilder.buildJavaClass(psiClass, null); // Assuming parent JavaFile model not strictly needed for these assertions
    }

    public void testClassName() {
        assertNotNull(javaClassModel);
        assertEquals("TestClass", javaClassModel.getName());
    }

    public void testNumberOfMethods() {
        assertNotNull(javaClassModel);
        // Expecting: constructor, method1, method2, helperMethod
        assertEquals(4, javaClassModel.methods().count());
    }

    public void testWeightedMethodsPerClass() {
        assertNotNull(javaClassModel);
        Metric wmcMetric = javaClassModel.metric(MetricType.WMC);
        assertNotNull("WMC Metric should be present", wmcMetric);
        // Manual WMC calculation for TEST_CLASS_STRING:
        // Constructor: 1
        // method1: 1
        // method2: 1 (for method itself) + 1 (for if statement) = 2
        // helperMethod: 1
        // Total WMC = 1 + 1 + 2 + 1 = 5
        assertEquals(Value.of(5), wmcMetric.getValue(), "WMC value mismatch");
    }

    public void testNumberOfMethodsMetric() {
        assertNotNull(javaClassModel);
        Metric nomMetric = javaClassModel.metric(MetricType.NOM);
        assertNotNull("NOM Metric should be present", nomMetric);
        // NOM counts user-defined methods.
        // In some interpretations, constructors are not counted in NOM by default, or are counted separately.
        // Assuming ClassModelBuilder's NOM includes constructors if they are PsiMethods:
        // TestClass (constructor), method1, method2, helperMethod => 4 methods
        // If constructors are excluded by default by the underlying tools, it would be 3.
        // Let's assume it counts all PsiMethod entries in the class.
        assertEquals(Value.of(4), nomMetric.getValue(), "NOM value mismatch");
    }

    // getTestDataPath is not needed if we are not loading from external files.
    // @Override
    // protected String getTestDataPath() {
    // return "testData"; // Or relevant path if still needed, e.g., "../../testData"
    // }
}
