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

import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.util.MetricsUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class CsvExporter implements Exporter {
    private final String CSV_DELIMITER = ",";

    public void export(String fileName, JavaProject javaProject) {
        File csvOutputFile = new File(fileName);
        try (PrintWriter printWriter = new PrintWriter(csvOutputFile)) {
            Optional<JavaClass> headerSupplierOpt = javaProject.allClasses().findAny();
            if (headerSupplierOpt.isEmpty()) {
                return;
            }
            JavaClass headerSupplier = headerSupplierOpt.get();
            String header = "Class Name" + CSV_DELIMITER + headerSupplier.metrics()
                    .map(m -> m.getType().name())
                    .collect(Collectors.joining(CSV_DELIMITER));
            printWriter.println(header);
            javaProject.allClasses()
                    .sorted((c1, c2) -> Objects.requireNonNull(c1.getPsiClass().getQualifiedName())
                            .compareTo(Objects.requireNonNull(c2.getPsiClass().getQualifiedName())))
                    .map(this::convertToCsv)
                    .forEach(printWriter::println);
        } catch (FileNotFoundException e) {
            MetricsUtils.getConsole().error(e.getMessage());
        }
        if (csvOutputFile.exists()) {
            MetricsUtils.getConsole().info("Classes metrics have been exported in " + csvOutputFile.getAbsolutePath());
        }
    }

    private String convertToCsv(JavaClass javaClass) {
        String className = Objects.requireNonNull(javaClass.getPsiClass().getQualifiedName()) + CSV_DELIMITER;
        String metrics = javaClass.metrics()
                .map(Metric::getFormattedValue)
                .collect(Collectors.joining(CSV_DELIMITER));
        return className + metrics;
    }
}
