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
import com.intellij.openapi.progress.BackgroundTaskQueue;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.AsyncFileListener;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.events.*;
import org.b333vv.metric.builder.DependenciesBuilder;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.code.JavaFile;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.RangeType;
import org.b333vv.metric.ui.chart.builder.MetricPieChartBuilder;
import org.b333vv.metric.ui.profile.MetricProfile;
import org.b333vv.metric.ui.tree.builder.ProjectMetricTreeBuilder;
import org.b333vv.metric.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.XYChart;

import javax.swing.tree.DefaultTreeModel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
    public static final Key<JavaProject> PACKAGE_ONLY_METRICS = Key.create("PACKAGE_WITHOUT_CLASSES_METRICS");
    public static final Key<DefaultTreeModel> CLASSES_BY_METRIC_TREE = Key.create("CLASSES_BY_METRIC_TREE");
    public static final Key<Map<MetricProfile, Set<JavaClass>>> METRIC_PROFILES = Key.create("METRIC_PROFILES");

    private final UserDataHolder myUserDataHolder = new UserDataHolderBase();
    private final BackgroundTaskQueue queue = new BackgroundTaskQueue(MetricsUtils.getCurrentProject(), "MetricsTree Queue");
    private final Map<VirtualFile, JavaFile> javaFiles = new ConcurrentHashMap<>();

    private MetricTaskCache () {
        VirtualFileManager.getInstance().addAsyncFileListener(new MyAsyncVfsListener(), this);
    }
    public static MetricTaskCache instance() {
        return ServiceManager.getService(MetricsUtils.getCurrentProject(), MetricTaskCache.class);
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

    public static BackgroundTaskQueue getQueue() {
        return instance().queue;
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
        putUserData(MetricTaskCache.PACKAGE_ONLY_METRICS, null);
        putUserData(MetricTaskCache.CLASSES_BY_METRIC_TREE, null);
        putUserData(MetricTaskCache.METRIC_PROFILES, null);
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
        queue.run(invalidateCachesTask);
    }
}
