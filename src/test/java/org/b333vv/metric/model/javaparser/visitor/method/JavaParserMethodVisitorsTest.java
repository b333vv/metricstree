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

package org.b333vv.metric.model.javaparser.visitor.method;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.code.JavaMethod;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JavaParserMethodVisitorsTest {
    private static final String TEST_CODE = """
            class TestSubject {
                private int field;

                public int getField() {
                    return field;
                }

                public void setField(int field) {
                    this.field = field;
                }

                public void aComplexMethod(int p1, String p2, boolean p3) {
                    int x = 0;
                    if (p3) {
                        for (int i = 0; i < p1; i++) {
                            if (i % 2 == 0) {
                                x++;
                            }
                        }
                    } else {
                        while(x < 10) {
                            x++;
                        }
                        do {
                            x--;
                        } while (x > 0);
                    }
                }

                public void anotherMethod() {
                    // This method has no loops
                }
            }
            """;

    private CompilationUnit cu;
    private com.intellij.psi.PsiClass psiClass;

    @BeforeEach
    void setup() {
        JavaParser parser = new JavaParser();
        ParseResult<CompilationUnit> result = parser.parse(TEST_CODE);
        cu = result.getResult().get();
        psiClass = mock(com.intellij.psi.PsiClass.class);
    }

    private JavaMethod getTestJavaMethod(String methodName) {
        com.intellij.psi.PsiMethod psiMethod = mock(com.intellij.psi.PsiMethod.class);
        when(psiMethod.getName()).thenReturn(methodName);
        return new JavaMethod(psiMethod, new JavaClass(psiClass));
    }

    @Test
    void testNumberOfLoops() {
        MethodDeclaration methodDeclaration = cu.getClassByName("TestSubject").get()
                .getMethodsByName("aComplexMethod").get(0);
        JavaMethod javaMethod = getTestJavaMethod("aComplexMethod");

        Metric metric = Metric.of(MetricType.NOL, 0);
        javaMethod.addMetric(metric);

        JavaParserNumberOfLoopsVisitor visitor = new JavaParserNumberOfLoopsVisitor();
        visitor.visit(methodDeclaration, m -> metric.setJavaParserValue(m.getValue()));

        assertEquals(3.0, metric.getJavaParserValue().doubleValue());
    }

    @Test
    void testNumberOfLoopsOnMethodWithNoLoops() {
        MethodDeclaration methodDeclaration = cu.getClassByName("TestSubject").get()
                .getMethodsByName("anotherMethod").get(0);
        JavaMethod javaMethod = getTestJavaMethod("anotherMethod");

        Metric metric = Metric.of(MetricType.NOL, 0);
        javaMethod.addMetric(metric);

        JavaParserNumberOfLoopsVisitor visitor = new JavaParserNumberOfLoopsVisitor();
        visitor.visit(methodDeclaration, m -> metric.setJavaParserValue(m.getValue()));

        assertEquals(0.0, metric.getJavaParserValue().doubleValue());
    }

    @Test
    void testLinesOfCode() {
        MethodDeclaration methodDeclaration = cu.getClassByName("TestSubject").get()
                .getMethodsByName("aComplexMethod").get(0);
        JavaMethod javaMethod = getTestJavaMethod("aComplexMethod");

        Metric metric = Metric.of(MetricType.LOC, 0);
        javaMethod.addMetric(metric);

        JavaParserLinesOfCodeVisitor visitor = new JavaParserLinesOfCodeVisitor();
        visitor.visit(methodDeclaration, m -> metric.setJavaParserValue(m.getValue()));

        long expectedLines = methodDeclaration.toString().lines().filter(line -> !line.isBlank()).count();
        assertEquals(expectedLines, metric.getJavaParserValue().longValue());
    }

    @Test
    void testNumberOfParameters() {
        MethodDeclaration methodDeclaration = cu.getClassByName("TestSubject").get()
                .getMethodsByName("aComplexMethod").get(0);
        JavaMethod javaMethod = getTestJavaMethod("aComplexMethod");

        Metric metric = Metric.of(MetricType.NOPM, 0);
        javaMethod.addMetric(metric);

        JavaParserNumberOfParametersVisitor visitor = new JavaParserNumberOfParametersVisitor();
        visitor.visit(methodDeclaration, m -> metric.setJavaParserValue(m.getValue()));

        assertEquals(3.0, metric.getJavaParserValue().doubleValue());
    }
}
