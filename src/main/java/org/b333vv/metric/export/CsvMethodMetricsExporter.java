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

import com.intellij.psi.PsiSubstitutor;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.MethodSignature;
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.code.JavaMethod;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.util.MetricsUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class CsvMethodMetricsExporter implements Exporter {

    public void export(String fileName, JavaProject javaProject) {
        File csvOutputFile = new File(fileName);
        try (PrintWriter printWriter = new PrintWriter(csvOutputFile)) {
            Optional<JavaMethod> headerSupplierOpt = javaProject.allClasses().flatMap(JavaClass::methods).findAny();
            if (headerSupplierOpt.isEmpty()) {
                return;
            }
            JavaMethod headerSupplier = headerSupplierOpt.get();
            String header = "Method Name;" + headerSupplier.metrics()
                    .map(m -> m.getType().name())
                    .collect(Collectors.joining(";"));
            printWriter.println(header);
            javaProject.allClasses()
                    .flatMap(JavaClass::methods)
                    .sorted((c1, c2) -> Objects.requireNonNull(c1.getJavaClass().getPsiClass().getQualifiedName())
                            .compareTo(Objects.requireNonNull(c2.getJavaClass().getPsiClass().getQualifiedName())))
                    .map(this::convertToCsv)
                    .forEach(printWriter::println);
        } catch (FileNotFoundException e) {
//            MetricsUtils.getConsole().error(e.getMessage());
            MetricsUtils.getCurrentProject().getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(e.getMessage());
        }
        if (csvOutputFile.exists()) {
            MetricsUtils.getCurrentProject().getMessageBus().syncPublisher(MetricsEventListener.TOPIC)
                    .printInfo("Method metrics have been exported in " + csvOutputFile.getAbsolutePath());
//            MetricsUtils.getConsole().info("Method metrics have been exported in " + csvOutputFile.getAbsolutePath());
        }
    }

    private String convertToCsv(JavaMethod javaMethod) {
        StringBuilder signature = new StringBuilder();
        MethodSignature methodSignature = javaMethod.getPsiMethod().getSignature(PsiSubstitutor.EMPTY);
        signature.append(javaMethod.getPsiMethod().getName());
        signature.append("(");
        signature.append(Arrays.stream(
                        methodSignature.getParameterTypes())
                .map(PsiType::getPresentableText)
                .collect(Collectors.joining(", ")));
        signature.append(")");
        String methodName = javaMethod
                .getJavaClass()
                .getPsiClass()
                .getQualifiedName()
                + "." +
                signature + ";";
        String metrics = javaMethod.metrics()
                .map(Metric::getFormattedValue)
                .collect(Collectors.joining(";"));
        return methodName + metrics;
    }
}
