/*
 * Copyright 2020 b33vv
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

import com.intellij.openapi.project.Project;
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.model.code.ProjectElement;
import org.b333vv.metric.model.metric.MetricLevel;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class ProjectMetricsSet2Json {
    private static class ProjectMetricsStampComparator implements Comparator<JSONObject> {
        @Override
        public int compare(JSONObject o1, JSONObject o2) {
            return (int) (Long.parseLong(o1.getString("time"))
                    - (Long.parseLong(o2.getString("time"))));
        }
    }

    public static void takeProjectMetricsSnapshot(Project project, ProjectElement javaProject) {
        long epoch = Instant.now().toEpochMilli();
        String snapshotTime = "" + epoch;
        String directoryPath = project.getBasePath() +
                java.io.File.separator +
                ".idea" +
                java.io.File.separator +
                "metrics";
        long epoch1 = Long.parseLong(snapshotTime);
        LocalDateTime dateTime = Instant.ofEpochMilli(epoch1).atZone(ZoneId.systemDefault()).toLocalDateTime();
        Map<String, String> projectMetrics = javaProject.metrics()
                .filter(m -> m.getType().level() == MetricLevel.PROJECT)
                .collect(Collectors.toMap(m -> m.getType().name(), m -> m.getPsiValue().toString()));

        projectMetrics.put("time", snapshotTime);

        JSONObject json = new JSONObject(projectMetrics);

        File directory = new File(directoryPath);
        if (!directory.exists()){
            directory.mkdirs();
        }

        String filePath = directoryPath +
                File.separator +
                snapshotTime +
                ".json";

        File outputFile = new File(filePath);

        try (PrintWriter printWriter = new PrintWriter(outputFile)) {
            printWriter.println(json);
        } catch (FileNotFoundException e) {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC)
                    .printInfo(e.getMessage());
//            MetricsUtils.getConsole().error(e.getMessage());
        }
    }

    public static TreeSet<JSONObject> parseStoredMetricsSnapshots(Project project) {
        String directoryPath = project.getBasePath() +
                java.io.File.separator +
                ".idea" +
                java.io.File.separator +
                "metrics";
        File directory = new File(directoryPath);
        if (!directory.exists() || Objects.requireNonNull(directory.listFiles()).length == 0){
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC)
                    .printInfo("There are no saved project metrics.");
//            MetricsUtils.getConsole().error("There are no saved project metrics.");
            return new TreeSet<>(new ProjectMetricsStampComparator()); // Return empty TreeSet instead of null
        }

        File[] fileList = Objects.requireNonNull(directory.listFiles());
        TreeSet<JSONObject> metricsStampSet = new TreeSet<>(new ProjectMetricsStampComparator());
        for (File file: fileList) {
            if (file.isFile() && !file.isHidden()) {
                JSONObject metricsStamp = parseFile(project, file);
                if (metricsStamp != null) {
                    metricsStampSet.add(metricsStamp);
                }
            }
        }
        return metricsStampSet;
    }

    private static JSONObject parseFile(Project project, File file) {
        try {
            JSONTokener jsonTokener = new JSONTokener(new String(Files.readAllBytes(file.toPath())));
            return new JSONObject(jsonTokener);
        } catch (Exception e) {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC)
                    .printInfo(e.getMessage());
//            MetricsUtils.getConsole().error(e.getMessage());
        }
        return null;
    }
}
