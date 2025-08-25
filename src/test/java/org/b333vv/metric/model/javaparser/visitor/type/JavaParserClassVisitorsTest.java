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

package org.b333vv.metric.model.javaparser.visitor.type;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class JavaParserClassVisitorsTest {
    private static final String TEST_CODE = """
            public class TestSubject {
                public int publicField;
                private String privateField;
                protected static final double aConstant = 3.14;
                private boolean ready;

                public TestSubject() {}

                public void method1() {}

                private static void utilityMethod() {}

                public int getPublicField() {
                    return publicField;
                }

                public void setPrivateField(String value) {
                    this.privateField = value;
                }

                public boolean isReady() {
                    return ready;
                }
            }

            interface AnInterface {
                void doSomething();
            }
            """;

    private JavaClass javaClass;
    private ClassOrInterfaceDeclaration classDeclaration;

    @BeforeEach
    void setup() {
        JavaParser parser = new JavaParser();
        ParseResult<CompilationUnit> result = parser.parse(TEST_CODE);
        CompilationUnit cu = result.getResult().get();
        classDeclaration = cu.getClassByName("TestSubject").get();

        com.intellij.psi.PsiClass psiClass = mock(com.intellij.psi.PsiClass.class);
        javaClass = new JavaClass(psiClass);
    }

    @Test
    void testNumberOfMethods() {
        Metric metric = Metric.of(MetricType.NOM, 0);
        javaClass.addMetric(metric);

        JavaParserNumberOfMethodsVisitor visitor = new JavaParserNumberOfMethodsVisitor();
        visitor.visit(classDeclaration, javaClass);

        assertEquals(5.0, metric.getJavaParserValue().doubleValue());
    }

    @Test
    void testNumberOfAttributes() {
        Metric metric = Metric.of(MetricType.NOA, 0);
        javaClass.addMetric(metric);

        JavaParserNumberOfAttributesVisitor visitor = new JavaParserNumberOfAttributesVisitor();
        visitor.visit(classDeclaration, javaClass);

        assertEquals(4.0, metric.getJavaParserValue().doubleValue());
    }

    @Test
    void testNumberOfPublicAttributes() {
        Metric metric = Metric.of(MetricType.NOPA, 0);
        javaClass.addMetric(metric);

        JavaParserNumberOfPublicAttributesVisitor visitor = new JavaParserNumberOfPublicAttributesVisitor();
        visitor.visit(classDeclaration, javaClass);

        assertEquals(1.0, metric.getJavaParserValue().doubleValue());
    }

    @Test
    void testNumberOfAccessorMethods() {
        Metric metric = Metric.of(MetricType.NOAC, 0);
        javaClass.addMetric(metric);

        JavaParserNumberOfAccessorMethodsVisitor visitor = new JavaParserNumberOfAccessorMethodsVisitor();
        visitor.visit(classDeclaration, javaClass);

        assertEquals(3.0, metric.getJavaParserValue().doubleValue());
    }
}
