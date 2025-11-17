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

    public void buildAndExport(String fileName, ProjectElement projectElement) {
        File csvOutputFile = new File(fileName);
        try (PrintWriter printWriter = new PrintWriter(csvOutputFile)) {
            Optional<PackageElement> headerSupplierOpt = projectElement.allPackages().findAny();
            if (headerSupplierOpt.isEmpty()) {
                return;
            }
            PackageElement headerSupplier = headerSupplierOpt.get();
            String header = "Package Name;" + headerSupplier.metrics()
                    .map(m -> m.getType().name())
                    .collect(Collectors.joining(";"));
            printWriter.println(header);
            projectElement.allPackages()
                    .sorted((c1, c2) -> {
                        String name1 = getPackageNameSafely(c1);
                        String name2 = getPackageNameSafely(c2);
                        return Objects.requireNonNull(name1).compareTo(Objects.requireNonNull(name2));
                    })
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

    private String getPackageNameSafely(PackageElement packageElement) {
        if (packageElement.getPsiPackage() != null) {
            return packageElement.getPsiPackage().getQualifiedName();
        } else {
            // For packages without PSI package, use the name directly
            return packageElement.getName();
        }
    }

    private String convertToCsv(PackageElement javaPackage) {
        String packageName = Objects.requireNonNull(getPackageNameSafely(javaPackage)) + ";";
        String metrics = javaPackage.metrics()
                .map(Metric::getFormattedValue)
                .collect(Collectors.joining(";"));
        return packageName + metrics;
    }
}
