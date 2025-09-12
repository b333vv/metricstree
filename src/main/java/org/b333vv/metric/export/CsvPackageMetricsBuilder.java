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
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.model.code.PackageElement;
import org.b333vv.metric.model.code.ProjectElement;
import org.b333vv.metric.model.metric.Metric;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class CsvPackageMetricsBuilder {

    private final Project project;

    public CsvPackageMetricsBuilder(Project project) {
        this.project = project;
    }

    public void buildAndExport(String fileName, ProjectElement javaProject) {
        File csvOutputFile = new File(fileName);
        try (PrintWriter printWriter = new PrintWriter(csvOutputFile)) {
            Optional<PackageElement> headerSupplierOpt = javaProject.allPackages().findAny();
            if (headerSupplierOpt.isEmpty()) {
                return;
            }
            PackageElement headerSupplier = headerSupplierOpt.get();
            String header = "Package Name;" + headerSupplier.metrics()
                    .map(m -> m.getType().name())
                    .collect(Collectors.joining(";"));
            printWriter.println(header);
            javaProject.allPackages()
                    .sorted((c1, c2) -> Objects.requireNonNull(c1.getPsiPackage().getQualifiedName())
                            .compareTo(Objects.requireNonNull(c2.getPsiPackage().getQualifiedName())))
                    .map(this::convertToCsv)
                    .forEach(printWriter::println);
        } catch (FileNotFoundException e) {
            this.project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(e.getMessage());
        }
        if (csvOutputFile.exists()) {
            this.project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC)
                    .printInfo("Package metrics have been exported in " + csvOutputFile.getAbsolutePath());
        }
    }

    private String convertToCsv(PackageElement javaPackage) {
        String packageName = Objects.requireNonNull(javaPackage.getPsiPackage().getQualifiedName()) + ";";
        String metrics = javaPackage.metrics()
                .map(Metric::getFormattedValue)
                .collect(Collectors.joining(";"));
        return packageName + metrics;
    }
}
