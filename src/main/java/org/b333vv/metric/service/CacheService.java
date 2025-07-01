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

package org.b333vv.metric.service;

import com.intellij.openapi.Disposable;
 import com.intellij.openapi.components.Service;
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.util.Key;
 import com.intellij.openapi.util.UserDataHolder;
 import com.intellij.openapi.util.UserDataHolderBase;
 import com.intellij.openapi.vfs.*;
 import com.intellij.openapi.vfs.newvfs.events.*;
 import org.b333vv.metric.builder.DependenciesBuilder;
import org.b333vv.metric.model.code.*;
import org.b333vv.metric.model.metric.MetricLevel;
 import org.b333vv.metric.model.metric.MetricType;
 import org.b333vv.metric.model.metric.value.RangeType;
 import org.b333vv.metric.task.InvalidateCachesTask;
 import org.b333vv.metric.ui.fitnessfunction.FitnessFunction;
 import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.ui.chart.builder.MetricPieChartBuilder;
 import org.b333vv.metric.ui.chart.builder.ProfileBoxChartBuilder;
 import org.b333vv.metric.ui.chart.builder.ProfileRadarChartBuilder;
 import org.b333vv.metric.ui.tree.builder.ProjectMetricTreeBuilder;
 import org.b333vv.metric.ui.treemap.presentation.MetricTreeMap;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 import org.knowm.xchart.CategoryChart;
 import org.knowm.xchart.HeatMapChart;
 import org.knowm.xchart.XYChart;

 import javax.swing.tree.DefaultTreeModel;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.stream.Stream;

/**
 * Service for managing in-memory caches and VFS-based cache invalidation.
 * This service replaces cache management previously handled by MetricTaskCache.
 */
@Service(Service.Level.PROJECT)
public final class CacheService implements UserDataHolder, Disposable {
    // Cache keys
    public static final Key<DependenciesBuilder> DEPENDENCIES = Key.create("DEPENDENCIES");
    public static final Key<JavaProject> CLASS_AND_METHODS_METRICS = Key.create("CLASS_AND_METHODS_METRICS");
    public static final Key<JavaProject> PACKAGE_METRICS = Key.create("PACKAGE_METRICS");
    public static final Key<JavaProject> PACKAGE_ONLY_METRICS = Key.create("PACKAGE_ONLY_METRICS");
    public static final Key<JavaProject> PROJECT_METRICS = Key.create("PROJECT_METRICS");
    public static final Key<DefaultTreeModel> PROJECT_TREE = Key.create("PROJECT_TREE");
    public static final Key<ProjectMetricTreeBuilder> TREE_BUILDER = Key.create("TREE_BUILDER");
    public static final Key<DefaultTreeModel> CLASSES_BY_METRIC_TREE = Key.create("CLASSES_BY_METRIC_TREE");
    public static final Key<Map<MetricType, Map<JavaClass, Metric>>> CLASSES_BY_METRIC_TYPES = Key.create("CLASSES_BY_METRIC_TYPES");
    public static final Key<List<MetricPieChartBuilder.PieChartStructure>> PIE_CHART_LIST = Key.create("PIE_CHART_LIST");
    public static final Key<Map<MetricType, Map<RangeType, Double>>> CLASSES_BY_METRIC_TYPES_FOR_CATEGORY_CHART = Key.create("CLASSES_BY_METRIC_TYPES_FOR_CATEGORY_CHART");
    public static final Key<CategoryChart> CATEGORY_CHART = Key.create("CATEGORY_CHART");
    public static final Key<Map<String, Double>> INSTABILITY = Key.create("INSTABILITY");
    public static final Key<Map<String, Double>> ABSTRACTNESS = Key.create("ABSTRACTNESS");
    public static final Key<XYChart> XY_CHART = Key.create("XY_CHART");
    public static final Key<Map<FitnessFunction, Set<JavaPackage>>> PACKAGE_LEVEL_FITNESS_FUNCTION = Key.create("PACKAGE_LEVEL_FITNESS_FUNCTION");
    public static final Key<Map<FitnessFunction, Set<JavaClass>>> CLASS_LEVEL_FITNESS_FUNCTION =
            Key.create("CLASS_LEVEL_FITNESS_FUNCTION");
    public static final Key<Map<FitnessFunction, Set<JavaClass>>> CLASSES_BY_PROFILE = Key.create("CLASSES_BY_PROFILE");
    public static final Key<List<ProfileBoxChartBuilder.BoxChartStructure>> BOX_CHARTS = Key.create("BOX_CHARTS");
    public static final Key<CategoryChart> PROFILE_CATEGORY_CHART = Key.create("PROFILE_CATEGORY_CHART");
    public static final Key<HeatMapChart> HEAT_MAP_CHART = Key.create("HEAT_MAP_CHART");
    public static final Key<List<ProfileRadarChartBuilder.RadarChartStructure>> RADAR_CHART = Key.create("RADAR_CHART");
    public static final Key<MetricTreeMap<JavaCode>> METRIC_TREE_MAP = Key.create("METRIC_TREE_MAP");
    public static final Key<MetricTreeMap<JavaCode>> PROFILE_TREE_MAP = Key.create("PROFILE_TREE_MAP");
    public static final Key<XYChart> PROJECT_METRICS_HISTORY_XY_CHART = Key.create("PROJECT_METRICS_HISTORY_XY_CHART");

    private UserDataHolderBase userData = new UserDataHolderBase();
    private final ConcurrentHashMap<String, JavaFile> javaFiles = new ConcurrentHashMap<>();
    private final Project project;
    private final VirtualFileListener vfsListener;

    public CacheService(Project project) {
        this.project = project;
        this.vfsListener = new MyVfsListener();
        VirtualFileManager.getInstance().addVirtualFileListener(vfsListener, this);
    }

    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {
        userData.putUserData(key, value);
    }

    @Override
    public <T> T getUserData(@NotNull Key<T> key) {
        return userData.getUserData(key);
    }

    public JavaProject getProject() {
        return getUserData(CLASS_AND_METHODS_METRICS);
    }

    public Map<FitnessFunction, Set<JavaClass>> getClassesByProfile() {
        return getUserData(CLASSES_BY_PROFILE);
    }

    /**
     * Invalidates all cached user data.
     */
    public void invalidateUserData() {
        // Clear all user data by replacing the userData holder entirely
        // This ensures all keys are cleared, including any custom ones used in tests
        userData = new UserDataHolderBase();
    }

    /**
     * Adds a JavaFile to the cache.
     *
     * @param javaFile the JavaFile to add
     */
    public void addJavaFile(VirtualFile virtualFile, JavaFile javaFile) {
        javaFiles.put(virtualFile.getPath(), javaFile);
    }

    /**
     * Removes a JavaFile from the cache based on its VirtualFile.
     *
     * @param virtualFile the VirtualFile of the JavaFile to remove
     */
    public void removeJavaFile(VirtualFile virtualFile) {
        javaFiles.remove(virtualFile.getPath());
    }

    /**
     * Gets a JavaFile from the cache based on its VirtualFile.
     *
     * @param virtualFile the VirtualFile of the JavaFile to get
     */
    public JavaFile getJavaFile(VirtualFile virtualFile) {
        return javaFiles.get(virtualFile.getPath());
    }

    /**
     * Returns a stream of all JavaFiles in the cache.
     *
     * @return a stream of JavaFiles
     */
    public Stream<JavaFile> getJavaFiles() {
        return javaFiles.values().stream();
    }

    @Override
    public void dispose() {
        invalidateUserData();
        javaFiles.clear();
    }

    /**
     * VirtualFileListener implementation that invalidates caches when Java files are modified.
     */
    private class MyVfsListener implements VirtualFileListener {
        @Override
        public void contentsChanged(@NotNull VirtualFileEvent event) {
            VirtualFile file = event.getFile();
            if (file != null && "java".equals(file.getExtension())) {
                // Invalidate caches immediately for unit-test consistency
                invalidateUserData();
                removeJavaFile(file);
                // Still enqueue background task for any listeners/UI updates
                project.getService(TaskQueueService.class)
                        .queue(new InvalidateCachesTask(project, file));
            }
        }

        @Override
        public void fileDeleted(@NotNull VirtualFileEvent event) {
            VirtualFile file = event.getFile();
            if (file != null && "java".equals(file.getExtension())) {
                invalidateUserData();
                removeJavaFile(file);
                project.getService(TaskQueueService.class)
                        .queue(new InvalidateCachesTask(project, file));
            }
        }

        @Override
        public void fileMoved(@NotNull VirtualFileMoveEvent event) {
            VirtualFile file = event.getFile();
            if (file != null && "java".equals(file.getExtension())) {
                invalidateUserData();
                removeJavaFile(file);
                project.getService(TaskQueueService.class)
                        .queue(new InvalidateCachesTask(project, file));
            }
        }

        @Override
        public void propertyChanged(@NotNull VirtualFilePropertyEvent event) {
            VirtualFile file = event.getFile();
            if (file != null && "java".equals(file.getExtension())) {
                invalidateUserData();
                removeJavaFile(file);
                project.getService(TaskQueueService.class)
                        .queue(new InvalidateCachesTask(project, file));
            }
        }
    }
}