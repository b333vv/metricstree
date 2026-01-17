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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.model.code.ClassElement;
import org.b333vv.metric.model.code.ProjectElement;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricLevel;
import org.b333vv.metric.model.metric.MetricType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CsvClassMetricsBuilder {

    private final Project project;
    private final List<MetricType> classMetricTypes;

    public CsvClassMetricsBuilder(Project project) {
        this.project = project;
        this.classMetricTypes = Arrays.stream(MetricType.values())
                .filter(m -> m.level() == MetricLevel.CLASS)
                .sorted(Comparator.comparing(MetricType::description))
                .collect(Collectors.toList());
    }

    public void buildAndExport(String fileName, ProjectElement projectElement) {
        File csvOutputFile = new File(fileName);
        try (PrintWriter printWriter = new PrintWriter(csvOutputFile)) {
            String header = "Class Name;" + classMetricTypes.stream()
                    .map(Enum::name)
                    .collect(Collectors.joining(";"));
            printWriter.println(header);
            projectElement.allClasses()
                    .sorted((c1, c2) -> com.intellij.openapi.application.ApplicationManager.getApplication()
                            .<Integer>runReadAction(
                                    (Computable<Integer>) () -> {
                                        String name1 = getClassQualifiedName(c1);
                                        String name2 = getClassQualifiedName(c2);
                                        return Objects.requireNonNull(name1).compareTo(Objects.requireNonNull(name2));
                                    }))
                    .map(this::convertToCsv)
                    .forEach(printWriter::println);
        } catch (FileNotFoundException e) {
            this.project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC)
                    .printInfo(e.getMessage());
        }
        if (csvOutputFile.exists()) {
            this.project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC)
                    .printInfo("Classes metrics have been exported in " + csvOutputFile.getAbsolutePath());
        }
    }

    private String getClassQualifiedName(ClassElement classElement) {
        return com.intellij.openapi.application.ApplicationManager.getApplication()
                .runReadAction((com.intellij.openapi.util.Computable<String>) () -> {
                    if (classElement.getPsiClass() != null) {
                        return classElement.getPsiClass().getQualifiedName();
                    } else if (classElement.getKtClassOrObject() != null) {
                        // For Kotlin classes, try to get a qualified name
                        String ktName = classElement.getKtClassOrObject().getName();
                        String containingFile = classElement.getKtClassOrObject().getContainingFile().getName();
                        // Create a qualified name using file name + class name
                        String baseName = containingFile.replace(".kt", "");
                        return baseName + "." + ktName;
                    } else {
                        // For synthetic class elements, use the name directly
                        return classElement.getName();
                    }
                });
    }

    private String convertToCsv(ClassElement javaClass) {
        return com.intellij.openapi.application.ApplicationManager.getApplication()
                .runReadAction((com.intellij.openapi.util.Computable<String>) () -> {
                    String className = Objects.requireNonNull(getClassQualifiedName(javaClass)) + ";";
                    String metrics = classMetricTypes.stream()
                            .map(m -> {
                                Metric metric = javaClass.metric(m);
                                return metric != null ? metric.getFormattedValue() : "";
                            })
                            .collect(Collectors.joining(";"));
                    return className + metrics;
                });
    }
}
