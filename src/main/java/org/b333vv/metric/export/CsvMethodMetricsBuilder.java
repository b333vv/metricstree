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

package org.b333vv.metric.export;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiSubstitutor;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.MethodSignature;
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.model.code.ClassElement;
import org.b333vv.metric.model.code.MethodElement;
import org.b333vv.metric.model.code.ProjectElement;
import org.b333vv.metric.model.metric.Metric;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class CsvMethodMetricsBuilder {

    private final Project project;

    public CsvMethodMetricsBuilder(Project project) {
        this.project = project;
    }

    public void buildAndExport(String fileName, ProjectElement javaProject) {
        File csvOutputFile = new File(fileName);
        try (PrintWriter printWriter = new PrintWriter(csvOutputFile)) {
            Optional<MethodElement> headerSupplierOpt = javaProject.allClasses().flatMap(ClassElement::methods).findAny();
            if (headerSupplierOpt.isEmpty()) {
                return;
            }
            MethodElement headerSupplier = headerSupplierOpt.get();
            String header = "Method Name;" + headerSupplier.metrics()
                    .map(m -> m.getType().name())
                    .collect(Collectors.joining(";"));
            printWriter.println(header);
            
            // Wrap PSI access in read action
            ApplicationManager.getApplication().runReadAction(() -> {
                javaProject.allClasses()
                        .flatMap(ClassElement::methods)
                        .sorted((c1, c2) -> {
                            // Safely get qualified names with null checks
                            String name1 = getQualifiedNameSafely(c1);
                            String name2 = getQualifiedNameSafely(c2);
                            return name1.compareTo(name2);
                        })
                        .map(this::convertToCsv)
                        .forEach(printWriter::println);
            });
        } catch (FileNotFoundException e) {
            this.project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(e.getMessage());
        }
        if (csvOutputFile.exists()) {
            this.project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC)
                    .printInfo("Method metrics have been exported in " + csvOutputFile.getAbsolutePath());
        }
    }
    
    private String getQualifiedNameSafely(MethodElement javaMethod) {
        try {
            String qualifiedName = javaMethod.getJavaClass().getPsiClass().getQualifiedName();
            return qualifiedName != null ? qualifiedName : "Unknown";
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private String convertToCsv(MethodElement javaMethod) {
        try {
            StringBuilder signature = new StringBuilder();
            MethodSignature methodSignature = javaMethod.getPsiMethod().getSignature(PsiSubstitutor.EMPTY);
            signature.append(javaMethod.getPsiMethod().getName());
            signature.append("(");
            signature.append(Arrays.stream(
                            methodSignature.getParameterTypes())
                    .map(PsiType::getPresentableText)
                    .collect(Collectors.joining(", ")));
            signature.append(")");
            
            String qualifiedName = javaMethod.getJavaClass().getPsiClass().getQualifiedName();
            String methodName = (qualifiedName != null ? qualifiedName : "Unknown") + "." + signature + ";";
            
            String metrics = javaMethod.metrics()
                    .map(Metric::getFormattedValue)
                    .collect(Collectors.joining(";"));
            return methodName + metrics;
        } catch (Exception e) {
            // Fallback in case of PSI access issues
            String metrics = javaMethod.metrics()
                    .map(Metric::getFormattedValue)
                    .collect(Collectors.joining(";"));
            return "Unknown.unknownMethod();" + metrics;
        }
    }
}
