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

package org.b333vv.metric.task;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.impl.CoreProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.AsyncFileListener;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.events.*;
import org.b333vv.metric.builder.DependenciesBuilder;
import org.b333vv.metric.model.code.*;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.RangeType;
import org.b333vv.metric.ui.chart.builder.MetricPieChartBuilder;
import org.b333vv.metric.ui.chart.builder.ProfileBoxChartBuilder;
import org.b333vv.metric.ui.chart.builder.ProfileRadarChartBuilder;
import org.b333vv.metric.ui.fitnessfunction.FitnessFunction;
import org.b333vv.metric.ui.tree.builder.ProjectMetricTreeBuilder;
import org.b333vv.metric.ui.treemap.presentation.MetricTreeMap;
import org.b333vv.metric.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.HeatMapChart;
import org.knowm.xchart.XYChart;

import javax.swing.tree.DefaultTreeModel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

public final class MetricTaskCache implements UserDataHolder, Disposable {
    public static final Key<DependenciesBuilder> DEPENDENCIES = Key.create("DEPENDENCIES");
    public static final Key<JavaProject> CLASS_AND_METHODS_METRICS = Key.create("CLASS_AND_METHODS_METRICS");
    public static final Key<JavaProject> PACKAGE_METRICS = Key.create("PACKAGE_METRICS");
    public static final Key<JavaProject> PROJECT_METRICS = Key.create("PROJECT_METRICS");
    public static final Key<DefaultTreeModel> PROJECT_TREE = Key.create("PROJECT_TREE");
    public static final Key<ProjectMetricTreeBuilder> TREE_BUILDER = Key.create("TREE_BUILDER");
    public static final Key<Map<MetricType, Map<JavaClass, Metric>>> CLASSES_BY_METRIC_TYPES = Key.create("CLASSES_BY_METRIC_TYPES");
    public static final Key<List<MetricPieChartBuilder.PieChartStructure>> PIE_CHART_LIST = Key.create("PIE_CHART_LIST");
    public static final Key<CategoryChart> CATEGORY_CHART = Key.create("CATEGORY_CHART");
    public static final Key<Map<MetricType, Map<RangeType, Double>>> CLASSES_BY_METRIC_TYPES_FOR_CATEGORY_CHART = Key.create("CLASSES_BY_METRIC_TYPES_FOR_CATEGORY_CHART");
    public static final Key<Map<String, Double>> INSTABILITY = Key.create("INSTABILITY");
    public static final Key<Map<String, Double>> ABSTRACTNESS = Key.create("ABSTRACTNESS");
    public static final Key<XYChart> XY_CHART = Key.create("XY_CHART");
    public static final Key<XYChart> PROJECT_METRICS_HISTORY_XY_CHART = Key.create("PROJECT_METRICS_HISTORY_XY_CHART");
    public static final Key<JavaProject> PACKAGE_ONLY_METRICS = Key.create("PACKAGE_WITHOUT_CLASSES_METRICS");
    public static final Key<DefaultTreeModel> CLASSES_BY_METRIC_TREE = Key.create("CLASSES_BY_METRIC_TREE");
    public static final Key<Map<FitnessFunction, Set<JavaClass>>> CLASS_LEVEL_FITNESS_FUNCTION = Key.create("CLASS_LEVEL_FITNESS_FUNCTION");
    public static final Key<Map<FitnessFunction, Set<JavaPackage>>> PACKAGE_LEVEL_FITNESS_FUNCTION = Key.create("PACKAGE_LEVEL_FITNESS_FUNCTION");
    public static final Key<List<ProfileBoxChartBuilder.BoxChartStructure>> BOX_CHARTS = Key.create("BOX_CHARTS");
    public static final Key<HeatMapChart> HEAT_MAP_CHART = Key.create("HEAT_MAP_CHART");
    public static final Key<List<ProfileRadarChartBuilder.RadarChartStructure>> RADAR_CHART = Key.create("RADAR_CHART");
    public static final Key<CategoryChart> PROFILE_CATEGORY_CHART = Key.create("PROFILE_CATEGORY_CHART");
    public static final Key<MetricTreeMap<JavaCode>> METRIC_TREE_MAP = Key.create("METRIC_TREE_MAP");
    public static final Key<MetricTreeMap<JavaCode>> PROFILE_TREE_MAP = Key.create("PROFILE_TREE_MAP");

    private final UserDataHolder myUserDataHolder = new UserDataHolderBase();
    private final ConcurrentLinkedQueue<Task.Backgroundable> taskQueue = new ConcurrentLinkedQueue<>();
    private volatile boolean isProcessing = false;
    private final Map<VirtualFile, JavaFile> javaFiles = new ConcurrentHashMap<>();

    private MetricTaskCache () {
        VirtualFileManager.getInstance().addAsyncFileListener(new MyAsyncVfsListener(), this);
    }

    @Nullable
    @Override
    public <T> T getUserData(@NotNull Key<T> key) {
        return myUserDataHolder.getUserData(key);
    }

    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {
        myUserDataHolder.putUserData(key, value);
    }

    @Override
    public void dispose() {
    }

    private void processNextTask() {
        if (isProcessing) return;
        
        Task.Backgroundable nextTask = taskQueue.poll();
        if (nextTask != null) {
            isProcessing = true;
            ProgressManager.getInstance().run(nextTask);
            ApplicationManager.getApplication().invokeLater(() -> {
                isProcessing = false;
                processNextTask();
            }, ModalityState.NON_MODAL);
        }
    }

    public static void runTask(Project project, Task.Backgroundable task) {
        MetricTaskCache instance = project.getService(MetricTaskCache.class);
        instance.taskQueue.offer(task);
        instance.processNextTask();
    }

    public static boolean isQueueEmpty(Project project) {
        MetricTaskCache instance = project.getService(MetricTaskCache.class);
        return instance.taskQueue.isEmpty() && !instance.isProcessing;
    }

    @Nullable
    public JavaFile getJavaFile(@NotNull VirtualFile virtualFile) {
        return javaFiles.get(virtualFile);
    }

    public void putJavaFile(@NotNull VirtualFile virtualFile, @NotNull JavaFile javaFile) {
        javaFiles.put(virtualFile, javaFile);
    }

    public void removeJavaFile(@NotNull VirtualFile virtualFile) {
        javaFiles.remove(virtualFile);
    }

    public Stream<JavaFile> getJavaFiles() {
        return javaFiles.values().stream();
    }

    public void invalidateUserData() {
        putUserData(MetricTaskCache.DEPENDENCIES, null);
        putUserData(MetricTaskCache.CLASS_AND_METHODS_METRICS, null);
        putUserData(MetricTaskCache.PACKAGE_METRICS, null);
        putUserData(MetricTaskCache.PROJECT_METRICS, null);
        putUserData(MetricTaskCache.PROJECT_TREE, null);
        putUserData(MetricTaskCache.TREE_BUILDER, null);
        putUserData(MetricTaskCache.CLASSES_BY_METRIC_TYPES, null);
        putUserData(MetricTaskCache.PIE_CHART_LIST, null);
        putUserData(MetricTaskCache.CLASSES_BY_METRIC_TYPES_FOR_CATEGORY_CHART, null);
        putUserData(MetricTaskCache.CATEGORY_CHART, null);
        putUserData(MetricTaskCache.INSTABILITY, null);
        putUserData(MetricTaskCache.ABSTRACTNESS, null);
        putUserData(MetricTaskCache.XY_CHART, null);
        putUserData(MetricTaskCache.PROJECT_METRICS_HISTORY_XY_CHART, null);
        putUserData(MetricTaskCache.PACKAGE_ONLY_METRICS, null);
        putUserData(MetricTaskCache.CLASSES_BY_METRIC_TREE, null);
        putUserData(MetricTaskCache.CLASS_LEVEL_FITNESS_FUNCTION, null);
        putUserData(MetricTaskCache.BOX_CHARTS, null);
        putUserData(MetricTaskCache.HEAT_MAP_CHART, null);
        putUserData(MetricTaskCache.PROFILE_CATEGORY_CHART, null);
        putUserData(MetricTaskCache.METRIC_TREE_MAP, null);
        putUserData(MetricTaskCache.PROFILE_TREE_MAP, null);
    }

    private class MyAsyncVfsListener implements AsyncFileListener {

        private boolean isBeforeEvent(@NotNull VFileEvent event) {
            return event instanceof VFileContentChangeEvent
                    || event instanceof VFileDeleteEvent
                    || event instanceof VFileMoveEvent
                    || event instanceof VFilePropertyChangeEvent;
        }

        private boolean isAfterEvent(@NotNull VFileEvent event) {
            return event instanceof VFileCreateEvent
                    || event instanceof VFileCopyEvent
                    || event instanceof VFileMoveEvent;
        }


        @Nullable
        @Override
        public ChangeApplier prepareChange(@NotNull List<? extends VFileEvent> events) {
            List<VFileEvent> beforeEvents = new ArrayList<>();
            List<VFileEvent> afterEvents = new ArrayList<>();
            for (VFileEvent event : events) {
                ProgressManager.checkCanceled();
                if (event instanceof VFileContentChangeEvent) {
                    VirtualFile file = Objects.requireNonNull(event.getFile());
                    beforeEvents.add(event);
                }
                else {
                    if (isBeforeEvent(event)) {
                        beforeEvents.add(event);
                    }
                    if (isAfterEvent(event)) {
                        afterEvents.add(event);
                    }
                }
            }
            return beforeEvents.isEmpty() && afterEvents.isEmpty() ? null : new ChangeApplier() {
                @Override
                public void beforeVfsChange() {
                    for (VFileEvent event : beforeEvents) {
                        if (event.getFile() == null || !event.getFile().getFileType().getName().equals("JAVA")) {
                            return;
                        }
                        if (event instanceof VFileContentChangeEvent) {
                            invalidateCaches(event.getFile());
                        }
                        else if (event instanceof VFileDeleteEvent) {
                            invalidateCaches(event.getFile());
                        }
                        else if (event instanceof VFileMoveEvent) {
                            invalidateCaches(event.getFile());
                        }
                        else if (event instanceof VFilePropertyChangeEvent) {
                            invalidateCaches(event.getFile());
                        }
                    }
                }

                @Override
                public void afterVfsChange() {
                    for (VFileEvent event : afterEvents) {
                        if (event.getFile() == null || !event.getFile().getFileType().getName().equals("JAVA")) {
                            return;
                        }
                        if (event instanceof VFileCreateEvent) {
                            invalidateCaches(event.getFile());
                        }
                        else if (event instanceof VFileCopyEvent) {
                            invalidateCaches(event.getFile());
                        }
                        else if (event instanceof VFileMoveEvent) {
                            invalidateCaches(event.getFile());
                        }
                    }
                }
            };
        }
    }

    private void invalidateCaches(VirtualFile file) {
        InvalidateCachesTask invalidateCachesTask = new InvalidateCachesTask(file);
        taskQueue.add(invalidateCachesTask);
    }
}
