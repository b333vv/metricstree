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
import com.github.javaparser.ast.CompilationUnit;
import org.b333vv.metric.builder.DependenciesBuilder;
import org.b333vv.metric.model.code.*;
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
    public static final Key<ProjectElement> CLASS_AND_METHODS_METRICS = Key.create("CLASS_AND_METHODS_METRICS");
    public static final Key<ProjectElement> PACKAGE_METRICS = Key.create("PACKAGE_METRICS");
    public static final Key<ProjectElement> PACKAGE_ONLY_METRICS = Key.create("PACKAGE_ONLY_METRICS");
    public static final Key<ProjectElement> PROJECT_METRICS = Key.create("PROJECT_METRICS");
    public static final Key<DefaultTreeModel> PROJECT_TREE = Key.create("PROJECT_TREE");
    public static final Key<ProjectMetricTreeBuilder> TREE_BUILDER = Key.create("TREE_BUILDER");
    public static final Key<DefaultTreeModel> CLASSES_BY_METRIC_TREE = Key.create("CLASSES_BY_METRIC_TREE");
    public static final Key<Map<MetricType, Map<ClassElement, Metric>>> CLASSES_BY_METRIC_TYPES = Key
            .create("CLASSES_BY_METRIC_TYPES");
    public static final Key<List<MetricPieChartBuilder.PieChartStructure>> PIE_CHART_LIST = Key
            .create("PIE_CHART_LIST");
    public static final Key<Map<MetricType, Map<RangeType, Double>>> CLASSES_BY_METRIC_TYPES_FOR_CATEGORY_CHART = Key
            .create("CLASSES_BY_METRIC_TYPES_FOR_CATEGORY_CHART");
    public static final Key<CategoryChart> CATEGORY_CHART = Key.create("CATEGORY_CHART");
    public static final Key<Map<String, Double>> INSTABILITY = Key.create("INSTABILITY");
    public static final Key<Map<String, Double>> ABSTRACTNESS = Key.create("ABSTRACTNESS");
    public static final Key<XYChart> XY_CHART = Key.create("XY_CHART");
    public static final Key<Map<FitnessFunction, Set<PackageElement>>> PACKAGE_LEVEL_FITNESS_FUNCTION = Key
            .create("PACKAGE_LEVEL_FITNESS_FUNCTION");
    public static final Key<Map<FitnessFunction, Set<ClassElement>>> CLASS_LEVEL_FITNESS_FUNCTION = Key
            .create("CLASS_LEVEL_FITNESS_FUNCTION");
    public static final Key<Map<FitnessFunction, Set<ClassElement>>> CLASSES_BY_PROFILE = Key
            .create("CLASSES_BY_PROFILE");
    public static final Key<List<ProfileBoxChartBuilder.BoxChartStructure>> BOX_CHARTS = Key.create("BOX_CHARTS");
    public static final Key<CategoryChart> PROFILE_CATEGORY_CHART = Key.create("PROFILE_CATEGORY_CHART");
    public static final Key<HeatMapChart> HEAT_MAP_CHART = Key.create("HEAT_MAP_CHART");
    public static final Key<List<ProfileRadarChartBuilder.RadarChartStructure>> RADAR_CHART = Key.create("RADAR_CHART");
    public static final Key<MetricTreeMap<CodeElement>> METRIC_TREE_MAP = Key.create("METRIC_TREE_MAP");
    public static final Key<MetricTreeMap<CodeElement>> PROFILE_TREE_MAP = Key.create("PROFILE_TREE_MAP");
    public static final Key<XYChart> PROJECT_METRICS_HISTORY_XY_CHART = Key.create("PROJECT_METRICS_HISTORY_XY_CHART");
    public static final Key<List<CompilationUnit>> ALL_COMPILATION_UNITS = Key.create("ALL_COMPILATION_UNITS");

    private UserDataHolderBase userData = new UserDataHolderBase();
    private final ConcurrentHashMap<String, FileElement> javaFiles = new ConcurrentHashMap<>();
    private final Map<String, ProjectElement> projectMetricsCache = new ConcurrentHashMap<>();
    private final Map<String, ProjectElement> packageMetricsCache = new ConcurrentHashMap<>();
    private final Map<String, ProjectElement> classAndMethodMetricsCache = new ConcurrentHashMap<>();
    private final Map<String, DependenciesBuilder> dependenciesCache = new ConcurrentHashMap<>();
    private final Map<String, DefaultTreeModel> projectTreeCache = new ConcurrentHashMap<>();
    private final Map<String, List<MetricPieChartBuilder.PieChartStructure>> pieChartCache = new ConcurrentHashMap<>();
    private final Map<String, CategoryChart> categoryChartCache = new ConcurrentHashMap<>();
    private final Map<String, Map<MetricType, Map<RangeType, Double>>> classesByMetricTypesForCategoryChartCache = new ConcurrentHashMap<>();
    private final Map<String, Map<MetricType, Map<ClassElement, Metric>>> classesByMetricTypesCache = new ConcurrentHashMap<>();
    private final Map<String, XYChart> xyChartCache = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Double>> instabilityCache = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Double>> abstractnessCache = new ConcurrentHashMap<>();
    private final Map<String, MetricTreeMap<CodeElement>> metricTreeMapCache = new ConcurrentHashMap<>();
    private final Map<String, DefaultTreeModel> classesByMetricTreeCache = new ConcurrentHashMap<>();
    private final Map<String, Map<FitnessFunction, Set<ClassElement>>> classLevelFitnessFunctionCache = new ConcurrentHashMap<>();
    private final Map<String, Map<FitnessFunction, Set<PackageElement>>> packageLevelFitnessFunctionCache = new ConcurrentHashMap<>();
    private final Map<String, MetricTreeMap<CodeElement>> profileTreeMapCache = new ConcurrentHashMap<>();
    private final Map<String, List<ProfileBoxChartBuilder.BoxChartStructure>> boxChartsCache = new ConcurrentHashMap<>();
    private final Map<String, HeatMapChart> heatMapChartCache = new ConcurrentHashMap<>();
    private final Map<String, List<ProfileRadarChartBuilder.RadarChartStructure>> radarChartCache = new ConcurrentHashMap<>();
    private final Map<String, CategoryChart> profileCategoryChartCache = new ConcurrentHashMap<>();

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

    public ProjectElement getProject() {
        return getUserData(CLASS_AND_METHODS_METRICS);
    }

    public Map<FitnessFunction, Set<ClassElement>> getClassesByProfile() {
        return getUserData(CLASSES_BY_PROFILE);
    }

    public ProjectElement getProjectMetrics(@Nullable com.intellij.openapi.module.Module module) {
        return projectMetricsCache.get(getKey(module));
    }

    public void putProjectMetrics(@Nullable com.intellij.openapi.module.Module module, ProjectElement projectElement) {
        projectMetricsCache.put(getKey(module), projectElement);
    }

    public ProjectElement getPackageMetrics(@Nullable com.intellij.openapi.module.Module module) {
        return packageMetricsCache.get(getKey(module));
    }

    public void putPackageMetrics(@Nullable com.intellij.openapi.module.Module module, ProjectElement projectElement) {
        packageMetricsCache.put(getKey(module), projectElement);
    }

    public ProjectElement getClassAndMethodMetrics(@Nullable com.intellij.openapi.module.Module module) {
        return classAndMethodMetricsCache.get(getKey(module));
    }

    public void putClassAndMethodMetrics(@Nullable com.intellij.openapi.module.Module module,
            ProjectElement projectElement) {
        classAndMethodMetricsCache.put(getKey(module), projectElement);
    }

    public DependenciesBuilder getDependencies(@Nullable com.intellij.openapi.module.Module module) {
        return dependenciesCache.get(getKey(module));
    }

    public void putDependencies(@Nullable com.intellij.openapi.module.Module module, DependenciesBuilder dependencies) {
        dependenciesCache.put(getKey(module), dependencies);
    }

    public DefaultTreeModel getProjectTree(@Nullable com.intellij.openapi.module.Module module) {
        return projectTreeCache.get(getKey(module));
    }

    public void putProjectTree(@Nullable com.intellij.openapi.module.Module module, DefaultTreeModel treeModel) {
        projectTreeCache.put(getKey(module), treeModel);
    }

    public List<MetricPieChartBuilder.PieChartStructure> getPieChartList(
            @Nullable com.intellij.openapi.module.Module module) {
        return pieChartCache.get(getKey(module));
    }

    public void putPieChartList(@Nullable com.intellij.openapi.module.Module module,
            List<MetricPieChartBuilder.PieChartStructure> pieChartList) {
        pieChartCache.put(getKey(module), pieChartList);
    }

    public CategoryChart getCategoryChart(@Nullable com.intellij.openapi.module.Module module) {
        return categoryChartCache.get(getKey(module));
    }

    public void putCategoryChart(@Nullable com.intellij.openapi.module.Module module, CategoryChart categoryChart) {
        categoryChartCache.put(getKey(module), categoryChart);
    }

    public Map<MetricType, Map<RangeType, Double>> getClassesByMetricTypesForCategoryChart(
            @Nullable com.intellij.openapi.module.Module module) {
        return classesByMetricTypesForCategoryChartCache.get(getKey(module));
    }

    public void putClassesByMetricTypesForCategoryChart(@Nullable com.intellij.openapi.module.Module module,
            Map<MetricType, Map<RangeType, Double>> classesByMetricTypes) {
        classesByMetricTypesForCategoryChartCache.put(getKey(module), classesByMetricTypes);
    }

    public Map<MetricType, Map<ClassElement, Metric>> getClassesByMetricTypes(
            @Nullable com.intellij.openapi.module.Module module) {
        return classesByMetricTypesCache.get(getKey(module));
    }

    public void putClassesByMetricTypes(@Nullable com.intellij.openapi.module.Module module,
            Map<MetricType, Map<ClassElement, Metric>> classesByMetricTypes) {
        classesByMetricTypesCache.put(getKey(module), classesByMetricTypes);
    }

    public XYChart getXyChart(@Nullable com.intellij.openapi.module.Module module) {
        return xyChartCache.get(getKey(module));
    }

    public void putXyChart(@Nullable com.intellij.openapi.module.Module module, XYChart xyChart) {
        xyChartCache.put(getKey(module), xyChart);
    }

    public Map<String, Double> getInstability(@Nullable com.intellij.openapi.module.Module module) {
        return instabilityCache.get(getKey(module));
    }

    public void putInstability(@Nullable com.intellij.openapi.module.Module module, Map<String, Double> instability) {
        instabilityCache.put(getKey(module), instability);
    }

    public Map<String, Double> getAbstractness(@Nullable com.intellij.openapi.module.Module module) {
        return abstractnessCache.get(getKey(module));
    }

    public void putAbstractness(@Nullable com.intellij.openapi.module.Module module, Map<String, Double> abstractness) {
        abstractnessCache.put(getKey(module), abstractness);
    }

    public MetricTreeMap<CodeElement> getMetricTreeMap(@Nullable com.intellij.openapi.module.Module module) {
        return metricTreeMapCache.get(getKey(module));
    }

    public void putMetricTreeMap(@Nullable com.intellij.openapi.module.Module module,
            MetricTreeMap<CodeElement> metricTreeMap) {
        metricTreeMapCache.put(getKey(module), metricTreeMap);
    }

    public DefaultTreeModel getClassesByMetricTree(@Nullable com.intellij.openapi.module.Module module) {
        return classesByMetricTreeCache.get(getKey(module));
    }

    public void putClassesByMetricTree(@Nullable com.intellij.openapi.module.Module module,
            DefaultTreeModel treeModel) {
        classesByMetricTreeCache.put(getKey(module), treeModel);
    }

    public Map<FitnessFunction, Set<ClassElement>> getClassLevelFitnessFunctions(
            @Nullable com.intellij.openapi.module.Module module) {
        return classLevelFitnessFunctionCache.get(getKey(module));
    }

    public void putClassLevelFitnessFunctions(@Nullable com.intellij.openapi.module.Module module,
            Map<FitnessFunction, Set<ClassElement>> classLevelFitnessFunctions) {
        classLevelFitnessFunctionCache.put(getKey(module), classLevelFitnessFunctions);
    }

    public Map<FitnessFunction, Set<PackageElement>> getPackageLevelFitnessFunctions(
            @Nullable com.intellij.openapi.module.Module module) {
        return packageLevelFitnessFunctionCache.get(getKey(module));
    }

    public void putPackageLevelFitnessFunctions(@Nullable com.intellij.openapi.module.Module module,
            Map<FitnessFunction, Set<PackageElement>> packageLevelFitnessFunctions) {
        packageLevelFitnessFunctionCache.put(getKey(module), packageLevelFitnessFunctions);
    }

    public MetricTreeMap<CodeElement> getProfileTreeMap(@Nullable com.intellij.openapi.module.Module module) {
        return profileTreeMapCache.get(getKey(module));
    }

    public void putProfileTreeMap(@Nullable com.intellij.openapi.module.Module module,
            MetricTreeMap<CodeElement> profileTreeMap) {
        profileTreeMapCache.put(getKey(module), profileTreeMap);
    }

    public List<ProfileBoxChartBuilder.BoxChartStructure> getBoxCharts(
            @Nullable com.intellij.openapi.module.Module module) {
        return boxChartsCache.get(getKey(module));
    }

    public void putBoxCharts(@Nullable com.intellij.openapi.module.Module module,
            List<ProfileBoxChartBuilder.BoxChartStructure> boxCharts) {
        boxChartsCache.put(getKey(module), boxCharts);
    }

    public HeatMapChart getHeatMapChart(@Nullable com.intellij.openapi.module.Module module) {
        return heatMapChartCache.get(getKey(module));
    }

    public void putHeatMapChart(@Nullable com.intellij.openapi.module.Module module, HeatMapChart heatMapChart) {
        heatMapChartCache.put(getKey(module), heatMapChart);
    }

    public List<ProfileRadarChartBuilder.RadarChartStructure> getRadarCharts(
            @Nullable com.intellij.openapi.module.Module module) {
        return radarChartCache.get(getKey(module));
    }

    public void putRadarCharts(@Nullable com.intellij.openapi.module.Module module,
            List<ProfileRadarChartBuilder.RadarChartStructure> radarCharts) {
        radarChartCache.put(getKey(module), radarCharts);
    }

    public CategoryChart getProfileCategoryChart(@Nullable com.intellij.openapi.module.Module module) {
        return profileCategoryChartCache.get(getKey(module));
    }

    public void putProfileCategoryChart(@Nullable com.intellij.openapi.module.Module module,
            CategoryChart profileCategoryChart) {
        profileCategoryChartCache.put(getKey(module), profileCategoryChart);
    }

    private String getKey(@Nullable com.intellij.openapi.module.Module module) {
        return module == null ? "PROJECT_ROOT" : module.getName();
    }

    /**
     * Invalidates all cached user data.
     */
    public void invalidateUserData() {
        // Clear all user data by replacing the userData holder entirely
        // This ensures all keys are cleared, including any custom ones used in tests
        userData = new UserDataHolderBase();
        projectMetricsCache.clear();
        packageMetricsCache.clear();
        classAndMethodMetricsCache.clear();
        dependenciesCache.clear();
        projectTreeCache.clear();
        pieChartCache.clear();
        categoryChartCache.clear();
        classesByMetricTypesForCategoryChartCache.clear();
        classesByMetricTypesCache.clear();
        xyChartCache.clear();
        instabilityCache.clear();
        abstractnessCache.clear();
        metricTreeMapCache.clear();
        classesByMetricTreeCache.clear();
        classLevelFitnessFunctionCache.clear();
        packageLevelFitnessFunctionCache.clear();
        profileTreeMapCache.clear();
        boxChartsCache.clear();
        heatMapChartCache.clear();
        radarChartCache.clear();
        profileCategoryChartCache.clear();
    }

    /**
     * Adds a JavaFile to the cache.
     *
     * @param javaFile the JavaFile to add
     */
    public void addJavaFile(VirtualFile virtualFile, FileElement javaFile) {
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
    public FileElement getJavaFile(VirtualFile virtualFile) {
        return javaFiles.get(virtualFile.getPath());
    }

    /**
     * Returns a stream of all JavaFiles in the cache.
     *
     * @return a stream of JavaFiles
     */
    public Stream<FileElement> getJavaFiles() {
        return javaFiles.values().stream();
    }

    @Override
    public void dispose() {
        invalidateUserData();
        javaFiles.clear();
    }

    /**
     * VirtualFileListener implementation that invalidates caches when Java files
     * are modified.
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