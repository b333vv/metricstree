package org.b333vv.metric.service;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.analysis.AnalysisScope;

import org.b333vv.metric.builder.MetricsBackgroundableTask;
import org.b333vv.metric.builder.ProjectTreeModelCalculator;
import org.b333vv.metric.event.MetricsEventListener;

import org.b333vv.metric.model.code.*;
import org.b333vv.metric.util.SettingsService;

import javax.swing.tree.DefaultTreeModel;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.b333vv.metric.builder.PieChartDataCalculator;


import org.b333vv.metric.ui.chart.builder.MetricPieChartBuilder;
import org.b333vv.metric.builder.CategoryChartDataCalculator;
import org.knowm.xchart.CategoryChart;

import java.util.List;
import org.b333vv.metric.model.code.CodeElement;
import org.b333vv.metric.builder.MetricTreeMapModelCalculator;
import org.b333vv.metric.ui.treemap.presentation.MetricTreeMap;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.HeatMapChart;
import org.b333vv.metric.ui.chart.builder.ProfileBoxChartBuilder;
import org.b333vv.metric.ui.chart.builder.ProfileRadarChartBuilder;
import org.b333vv.metric.builder.XyChartDataCalculator;
import org.b333vv.metric.builder.ProfileBoxChartDataCalculator;
import org.b333vv.metric.builder.ProfileCategoryChartDataCalculator;
import org.b333vv.metric.builder.ProfileHeatMapDataCalculator;
import org.b333vv.metric.builder.ProfileRadarDataCalculator;
import org.b333vv.metric.builder.ProfileTreeMapModelCalculator;
import org.b333vv.metric.builder.ProjectHistoryChartDataCalculator;
import org.b333vv.metric.export.XmlExporter;
import org.b333vv.metric.export.CsvClassMetricsExporter;
import org.b333vv.metric.export.CsvMethodMetricsExporter;
import org.b333vv.metric.export.CsvPackageMetricsExporter;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import org.b333vv.metric.ui.fitnessfunction.FitnessFunction;
import org.b333vv.metric.model.code.ClassElement;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.value.RangeType;
import org.b333vv.metric.builder.ClassFitnessFunctionCalculator;
import org.b333vv.metric.builder.PackageFitnessFunctionCalculator;
import org.b333vv.metric.builder.ClassesByMetricsValuesCounter;

// New imports for model builders
import org.b333vv.metric.builder.DependenciesBuilder;
import org.b333vv.metric.builder.DependenciesCalculator;
import org.b333vv.metric.builder.PsiCalculationStrategy;
import org.b333vv.metric.builder.PackageMetricsSetCalculator;
import org.b333vv.metric.builder.ProjectMetricsSetCalculator;
import org.b333vv.metric.ui.settings.other.CalculationEngine;
import org.b333vv.metric.builder.JavaParserCalculationStrategy;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;

import java.nio.file.Paths;
import java.util.ArrayList;


public class CalculationServiceImpl implements CalculationService {
    private final Project project;
    private final TaskQueueService taskQueueService;
    private final CacheService cacheService;
    private final SettingsService settingsService;

    public CalculationServiceImpl(Project project) {
        this.project = project;
        this.taskQueueService = project.getService(TaskQueueService.class);
        this.cacheService = project.getService(CacheService.class);
        this.settingsService = project.getService(SettingsService.class);
    }

    // Helper method to run a task synchronously and get its result
    private <T> T runTaskSynchronously(String title, Function<ProgressIndicator, T> taskLogic, ProgressIndicator indicator) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<T> result = new AtomicReference<>();

        MetricsBackgroundableTask<T> genericTask = new MetricsBackgroundableTask<>(
                project,
                title,
                true, // canBeCancelled
                taskLogic,
                (res) -> {
                    result.set(res);
                    latch.countDown();
                },
                () -> latch.countDown(), // onCancel
                null // onFinished
        );

        // Queue the task and wait for it to complete
        taskQueueService.queue(genericTask);
        try {
            latch.await(); // Wait for the task to complete
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            // Handle interruption if necessary
        }
        return result.get();
    }

    private List<CompilationUnit> getOrBuildAllCompilationUnits(ProgressIndicator indicator) {
        List<CompilationUnit> allUnits = cacheService.getUserData(CacheService.ALL_COMPILATION_UNITS);
        if (allUnits == null) {
            allUnits = runTaskSynchronously(
                "Parsing All Project Sources",
                (progressIndicator) -> {
                    List<CompilationUnit> units = new ArrayList<>();
                    JavaParser javaParser = new JavaParser(); // Use a simple parser, no symbol solving needed here.
                    ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();

                    fileIndex.iterateContent(fileOrDir -> {
                        if (!fileOrDir.isDirectory() && "java".equals(fileOrDir.getExtension()) && !fileIndex.isInLibrarySource(fileOrDir) && !fileIndex.isInTestSourceContent(fileOrDir)) {
                            try {
                                ParseResult<CompilationUnit> result = javaParser.parse(Paths.get(fileOrDir.getPath()));
                                result.ifSuccessful(units::add);
                            } catch (Exception e) {
                                // Log or handle parsing errors if necessary
                            }
                        }
                        return true; // continue iteration
                    });
                    return units;
                },
                indicator
            );
            cacheService.putUserData(CacheService.ALL_COMPILATION_UNITS, allUnits);
        }
        return allUnits;
    }

    public DependenciesBuilder getOrBuildDependencies(ProgressIndicator indicator) {
        DependenciesBuilder dependencies = cacheService.getUserData(CacheService.DEPENDENCIES);
        if (dependencies == null) {
            dependencies = runTaskSynchronously(
                    "Building Dependencies Model",
                    (progressIndicator) -> {
                        // Create AnalysisScope within the task to ensure proper initialization
                        AnalysisScope analysisScope = new AnalysisScope(project);
                        analysisScope.setIncludeTestSource(false);
                        return new DependenciesCalculator(analysisScope, new DependenciesBuilder()).calculateDependencies();
                    },
                    indicator
            );
            cacheService.putUserData(CacheService.DEPENDENCIES, dependencies);
        }
        return dependencies;
    }

    public ProjectElement getOrBuildClassAndMethodModel(ProgressIndicator indicator) {
        ProjectElement javaProject = cacheService.getUserData(CacheService.CLASS_AND_METHODS_METRICS);
        if (javaProject == null) {
            // Ensure dependencies are built first
            DependenciesBuilder dependencies = getOrBuildDependencies(indicator);

            javaProject = runTaskSynchronously(
                    "Building Class and Method Metrics Model",
                    (progressIndicator) -> {
                        // Stage 1: Always run PSI
                        PsiCalculationStrategy psiStrategy = new PsiCalculationStrategy();
                        ProjectElement newJavaProject = psiStrategy.calculate(project, progressIndicator);

                        // Stage 2: Conditionally augment with JavaParser
                        if (settingsService.getCalculationEngine() == CalculationEngine.JAVAPARSER) {
                            List<CompilationUnit> allUnits = getOrBuildAllCompilationUnits(progressIndicator); // Call the new method
                            JavaParserCalculationStrategy javaParserStrategy = new JavaParserCalculationStrategy();
                            javaParserStrategy.augment(newJavaProject, project, allUnits, progressIndicator); // Pass the list
                            logMetricDifferences(newJavaProject);
                        }
                        return newJavaProject;
                    },
                    indicator
            );
            cacheService.putUserData(CacheService.CLASS_AND_METHODS_METRICS, javaProject);
        }
        return javaProject;
    }

    public ProjectElement getOrBuildPackageMetricsModel(ProgressIndicator indicator) {
        ProjectElement javaProject = cacheService.getUserData(CacheService.PACKAGE_METRICS);
        if (javaProject == null) {
            // Ensure class and method model is built first
            ProjectElement classAndMethodModel = getOrBuildClassAndMethodModel(indicator);

            javaProject = runTaskSynchronously(
                    "Building Package Metrics Model",
                    (progressIndicator) -> {
                        // Create AnalysisScope within the task to ensure proper initialization
                        AnalysisScope analysisScope = new AnalysisScope(project);
                        analysisScope.setIncludeTestSource(false);
                        DependenciesBuilder dependencies = getOrBuildDependencies(progressIndicator);
                        new PackageMetricsSetCalculator(analysisScope, dependencies, classAndMethodModel).calculate();
                        return classAndMethodModel;
                    },
                    indicator
            );
            cacheService.putUserData(CacheService.PACKAGE_METRICS, javaProject);
        }
        return javaProject;
    }

    public ProjectElement getOrBuildProjectMetricsModel(ProgressIndicator indicator) {
        ProjectElement javaProject = cacheService.getUserData(CacheService.PROJECT_METRICS);
        if (javaProject == null) {
            // Ensure package metrics model is built first
            ProjectElement packageMetricsModel = getOrBuildPackageMetricsModel(indicator);

            javaProject = runTaskSynchronously(
                    "Building Project Metrics Model",
                    (progressIndicator) -> {
                        // Create AnalysisScope within the task to ensure proper initialization
                        AnalysisScope analysisScope = new AnalysisScope(project);
                        analysisScope.setIncludeTestSource(false);
                        DependenciesBuilder dependencies = getOrBuildDependencies(progressIndicator);
                        new ProjectMetricsSetCalculator(analysisScope, dependencies, packageMetricsModel).calculate();
                        return packageMetricsModel;
                    },
                    indicator
            );
            cacheService.putUserData(CacheService.PROJECT_METRICS, javaProject);
        }
        return javaProject;
    }

    @Override
    public void calculateProjectTree() {
        project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).clearProjectMetricsTree();

        DefaultTreeModel treeModel = cacheService.getUserData(CacheService.PROJECT_TREE);

        if (treeModel != null) {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).projectMetricsTreeIsReady(treeModel);
        } else {
            Function<ProgressIndicator, DefaultTreeModel> taskLogic = (indicator) -> new ProjectTreeModelCalculator(project).calculate();
            Consumer<DefaultTreeModel> onSuccessCallback = (model) -> {
                cacheService.putUserData(CacheService.PROJECT_TREE, model);
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC)
                       .projectMetricsTreeIsReady(model);
            };
            Runnable onCancelCallback = () -> project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building tree model canceled");

            MetricsBackgroundableTask<DefaultTreeModel> genericTask = new MetricsBackgroundableTask<>(
                    project,
                    "Build Project Tree",
                    true, // canBeCancelled
                    taskLogic,
                    onSuccessCallback,
                    onCancelCallback,
                    null // onFinished
            );
            taskQueueService.queue(genericTask);
        }
    }

    @Override
    public void calculatePieChart() {
        List<MetricPieChartBuilder.PieChartStructure> pieChartList = cacheService.getUserData(CacheService.PIE_CHART_LIST);
        Map<MetricType, Map<ClassElement, Metric>> classesByMetricTypes = cacheService.getUserData(CacheService.CLASSES_BY_METRIC_TYPES);
        
        if (pieChartList != null && classesByMetricTypes != null) {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).pieChartIsReady();
        } else {
            Function<ProgressIndicator, List<MetricPieChartBuilder.PieChartStructure>> taskLogic = (indicator) -> {
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building classes distribution by metric values pie chart started");
                ProjectElement javaProject = getOrBuildProjectMetricsModel(indicator);
                
                // Generate classes by metric types data
                Map<MetricType, Map<ClassElement, Metric>> newClassesByMetricTypes = generateClassesByMetricTypes(javaProject);
                cacheService.putUserData(CacheService.CLASSES_BY_METRIC_TYPES, newClassesByMetricTypes);
                
                // Generate pie chart data
                PieChartDataCalculator calculator = new PieChartDataCalculator();
                List<MetricPieChartBuilder.PieChartStructure> newPieChartList = calculator.calculate(javaProject, project);
                cacheService.putUserData(CacheService.PIE_CHART_LIST, newPieChartList);
                
                return newPieChartList;
            };
            Consumer<List<MetricPieChartBuilder.PieChartStructure>> onSuccessCallback = (newPieChartList) -> {
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building classes distribution by metric values pie chart finished");
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).pieChartIsReady();
            };
            Runnable onCancelCallback = () -> project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building classes distribution by metric values pie chart canceled");

            MetricsBackgroundableTask<List<MetricPieChartBuilder.PieChartStructure>> genericTask = new MetricsBackgroundableTask<>(
                    project,
                    "Building Pie Chart",
                    true, // canBeCancelled
                    taskLogic,
                    onSuccessCallback,
                    onCancelCallback,
                    null // onFinished
            );
            taskQueueService.queue(genericTask);
        }
    }
    
    private Map<MetricType, Map<ClassElement, Metric>> generateClassesByMetricTypes(ProjectElement javaProject) {
        Map<MetricType, Map<ClassElement, Metric>> classesByMetricTypes = new HashMap<>();
        
        javaProject.allClasses().forEach(javaClass -> {
            javaClass.metrics().forEach(metric -> {
                classesByMetricTypes.computeIfAbsent(metric.getType(), k -> new HashMap<>())
                                   .put(javaClass, metric);
            });
        });
        
        return classesByMetricTypes;
    }

    @Override
    public void calculateCategoryChart() {
        CategoryChart categoryChart = cacheService.getUserData(CacheService.CATEGORY_CHART);
        Map<MetricType, Map<RangeType, Double>> classesByMetricTypes = cacheService.getUserData(CacheService.CLASSES_BY_METRIC_TYPES_FOR_CATEGORY_CHART);
        
        if (categoryChart != null && classesByMetricTypes != null) {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).categoryChartIsReady();
        } else {
            Function<ProgressIndicator, CategoryChart> taskLogic = (indicator) -> {
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building classes distribution by metric values category chart started");
                ProjectElement javaProject = getOrBuildProjectMetricsModel(indicator);
                
                // Generate the distribution data first
                ClassesByMetricsValuesCounter distributor = new ClassesByMetricsValuesCounter(project);
                Map<MetricType, Map<RangeType, Double>> newClassesByMetricTypes = distributor.classesByMetricsValuesDistribution(javaProject);
                cacheService.putUserData(CacheService.CLASSES_BY_METRIC_TYPES_FOR_CATEGORY_CHART, newClassesByMetricTypes);
                
                // Then generate the chart
                CategoryChartDataCalculator calculator = new CategoryChartDataCalculator();
                CategoryChart newCategoryChart = calculator.calculate(javaProject, project);
                cacheService.putUserData(CacheService.CATEGORY_CHART, newCategoryChart);
                return newCategoryChart;
            };
            Consumer<CategoryChart> onSuccessCallback = (calculatedCategoryChart) -> {
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building classes distribution by metric values category chart finished");
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).categoryChartIsReady();
            };
            Runnable onCancelCallback = () -> project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building classes distribution by metric values category chart canceled");

            MetricsBackgroundableTask<CategoryChart> genericTask = new MetricsBackgroundableTask<>(
                    project,
                    "Building Category Chart",
                    true, // canBeCancelled
                    taskLogic,
                    onSuccessCallback,
                    onCancelCallback,
                    null // onFinished
            );
            taskQueueService.queue(genericTask);
        }
    }

    @Override
    public void calculateMetricTreeMap() {
        MetricTreeMap<CodeElement> metricTreeMap = cacheService.getUserData(CacheService.METRIC_TREE_MAP);
        if (metricTreeMap != null) {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).metricTreeMapIsReady();
        } else {
            Function<ProgressIndicator, MetricTreeMap<CodeElement>> taskLogic = (indicator) -> {
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building treemap with metric types distribution started");
                ProjectElement javaProject = getOrBuildProjectMetricsModel(indicator);
                MetricTreeMapModelCalculator calculator = new MetricTreeMapModelCalculator();
                MetricTreeMap<CodeElement> newMetricTreeMap = calculator.calculate(javaProject);
                cacheService.putUserData(CacheService.METRIC_TREE_MAP, newMetricTreeMap);
                return newMetricTreeMap;
            };
            Consumer<MetricTreeMap<CodeElement>> onSuccessCallback = (newMetricTreeMap) -> {
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building treemap with metric types distribution finished");
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).metricTreeMapIsReady();
            };
            Runnable onCancelCallback = () -> project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building treemap with metric types distribution canceled");

            MetricsBackgroundableTask<MetricTreeMap<CodeElement>> genericTask = new MetricsBackgroundableTask<>(
                    project,
                    "Build Metric Treemap",
                    true, // canBeCancelled
                    taskLogic,
                    onSuccessCallback,
                    onCancelCallback,
                    null // onFinished
            );
            taskQueueService.queue(genericTask);
        }
    }

    @Override
    public void calculateXyChart() {
        XYChart xyChart = cacheService.getUserData(CacheService.XY_CHART);
        Map<String, Double> instability = cacheService.getUserData(CacheService.INSTABILITY);
        Map<String, Double> abstractness = cacheService.getUserData(CacheService.ABSTRACTNESS);
        
        if (xyChart != null && instability != null && abstractness != null) {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).xyChartIsReady();
        } else {
            Function<ProgressIndicator, XYChart> taskLogic = (indicator) -> {
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building XY chart started");
                ProjectElement javaProject = getOrBuildProjectMetricsModel(indicator);
                XyChartDataCalculator calculator = new XyChartDataCalculator();
                XyChartDataCalculator.XyChartResult result = calculator.calculate(javaProject, project);
                
                // Store all data in cache
                cacheService.putUserData(CacheService.XY_CHART, result.getChart());
                cacheService.putUserData(CacheService.INSTABILITY, result.getInstability());
                cacheService.putUserData(CacheService.ABSTRACTNESS, result.getAbstractness());
                
                return result.getChart();
            };
            Consumer<XYChart> onSuccessCallback = (newXyChart) -> {
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building XY chart finished");
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).xyChartIsReady();
            };
            Runnable onCancelCallback = () -> project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building XY chart canceled");

            MetricsBackgroundableTask<XYChart> genericTask = new MetricsBackgroundableTask<>(
                    project,
                    "Building XY Chart",
                    true, // canBeCancelled
                    taskLogic,
                    onSuccessCallback,
                    onCancelCallback,
                    null // onFinished
            );
            taskQueueService.queue(genericTask);
        }
    }

    @Override
    public void calculateProfileBoxCharts() {
        List<ProfileBoxChartBuilder.BoxChartStructure> boxCharts = cacheService.getUserData(CacheService.BOX_CHARTS);
        if (boxCharts != null) {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).profilesBoxChartIsReady();
        } else {
            Function<ProgressIndicator, List<ProfileBoxChartBuilder.BoxChartStructure>> taskLogic = (indicator) -> {
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building profile box charts started");
                ProjectElement javaProject = getOrBuildProjectMetricsModel(indicator);
                
                // Ensure class fitness functions are calculated first
                Map<FitnessFunction, Set<ClassElement>> classesByProfile = cacheService.getClassesByProfile();
                if (classesByProfile == null) {
                    // Calculate class fitness functions synchronously
                    classesByProfile = runTaskSynchronously(
                            "Building class level fitness functions for profile box charts",
                            (progressIndicator) -> new ClassFitnessFunctionCalculator().calculate(project, javaProject),
                            indicator
                    );
                    cacheService.putUserData(CacheService.CLASS_LEVEL_FITNESS_FUNCTION, classesByProfile);
                    cacheService.putUserData(CacheService.CLASSES_BY_PROFILE, classesByProfile);
                }
                
                ProfileBoxChartDataCalculator calculator = new ProfileBoxChartDataCalculator();
                List<ProfileBoxChartBuilder.BoxChartStructure> newBoxCharts = calculator.calculate(classesByProfile);
                cacheService.putUserData(CacheService.BOX_CHARTS, newBoxCharts);
                return newBoxCharts;
            };
            Consumer<List<ProfileBoxChartBuilder.BoxChartStructure>> onSuccessCallback = (newBoxCharts) -> {
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building profile box charts finished");
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).profilesBoxChartIsReady();
            };
            Runnable onCancelCallback = () -> project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building profile box charts canceled");

            MetricsBackgroundableTask<List<ProfileBoxChartBuilder.BoxChartStructure>> genericTask = new MetricsBackgroundableTask<>(
                    project,
                    "Building Profile Box Charts",
                    true, // canBeCancelled
                    taskLogic,
                    onSuccessCallback,
                    onCancelCallback,
                    null // onFinished
            );
            taskQueueService.queue(genericTask);
        }
    }

    @Override
    public void calculateProfileCategoryChart() {
        CategoryChart profileCategoryChart = cacheService.getUserData(CacheService.PROFILE_CATEGORY_CHART);
        if (profileCategoryChart != null) {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).profilesCategoryChartIsReady();
        } else {
            Function<ProgressIndicator, CategoryChart> taskLogic = (indicator) -> {
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building profile category chart started");
                ProjectElement javaProject = getOrBuildProjectMetricsModel(indicator);
                
                // Ensure class fitness functions are calculated first
                Map<FitnessFunction, Set<ClassElement>> classesByProfile = cacheService.getClassesByProfile();
                if (classesByProfile == null) {
                    // Calculate class fitness functions synchronously
                    classesByProfile = runTaskSynchronously(
                            "Building class level fitness functions for profile chart",
                            (progressIndicator) -> new ClassFitnessFunctionCalculator().calculate(project, javaProject),
                            indicator
                    );
                    cacheService.putUserData(CacheService.CLASS_LEVEL_FITNESS_FUNCTION, classesByProfile);
                    cacheService.putUserData(CacheService.CLASSES_BY_PROFILE, classesByProfile);
                }
                
                ProfileCategoryChartDataCalculator calculator = new ProfileCategoryChartDataCalculator();
                CategoryChart newCategoryChart = calculator.calculate(classesByProfile);
                cacheService.putUserData(CacheService.PROFILE_CATEGORY_CHART, newCategoryChart);
                return newCategoryChart;
            };
            Consumer<CategoryChart> onSuccessCallback = (newCategoryChart) -> {
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building profile category chart finished");
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).profilesCategoryChartIsReady();
            };
            Runnable onCancelCallback = () -> project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building profile category chart canceled");

            MetricsBackgroundableTask<CategoryChart> genericTask = new MetricsBackgroundableTask<>(
                    project,
                    "Building Profile Category Chart",
                    true, // canBeCancelled
                    taskLogic,
                    onSuccessCallback,
                    onCancelCallback,
                    null // onFinished
            );
            taskQueueService.queue(genericTask);
        }
    }

    @Override
    public void calculateProfileHeatMapChart() {
        HeatMapChart heatMapChart = cacheService.getUserData(CacheService.HEAT_MAP_CHART);
        if (heatMapChart != null) {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).profilesHeatMapChartIsReady();
        } else {
            Function<ProgressIndicator, HeatMapChart> taskLogic = (indicator) -> {
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building profile heat map chart started");
                ProjectElement javaProject = getOrBuildProjectMetricsModel(indicator);
                
                // Ensure class fitness functions are calculated first
                Map<FitnessFunction, Set<ClassElement>> classesByProfile = cacheService.getClassesByProfile();
                if (classesByProfile == null) {
                    // Calculate class fitness functions synchronously
                    classesByProfile = runTaskSynchronously(
                            "Building class level fitness functions for profile heat map chart",
                            (progressIndicator) -> new ClassFitnessFunctionCalculator().calculate(project, javaProject),
                            indicator
                    );
                    cacheService.putUserData(CacheService.CLASS_LEVEL_FITNESS_FUNCTION, classesByProfile);
                    cacheService.putUserData(CacheService.CLASSES_BY_PROFILE, classesByProfile);
                }
                
                ProfileHeatMapDataCalculator calculator = new ProfileHeatMapDataCalculator();
                HeatMapChart newHeatMapChart = calculator.calculate(classesByProfile);
                cacheService.putUserData(CacheService.HEAT_MAP_CHART, newHeatMapChart);
                return newHeatMapChart;
            };
            Consumer<HeatMapChart> onSuccessCallback = (newHeatMapChart) -> {
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building profile heat map chart finished");
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).profilesHeatMapChartIsReady();
            };
            Runnable onCancelCallback = () -> project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building profile heat map chart canceled");

            MetricsBackgroundableTask<HeatMapChart> genericTask = new MetricsBackgroundableTask<>(
                    project,
                    "Building Profile Heat Map Chart",
                    true, // canBeCancelled
                    taskLogic,
                    onSuccessCallback,
                    onCancelCallback,
                    null // onFinished
            );
            taskQueueService.queue(genericTask);
        }
    }

    @Override
    public void calculateProfileRadarCharts() {
        List<ProfileRadarChartBuilder.RadarChartStructure> radarChart = cacheService.getUserData(CacheService.RADAR_CHART);
        if (radarChart != null) {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).profilesRadarChartIsReady();
        } else {
            Function<ProgressIndicator, List<ProfileRadarChartBuilder.RadarChartStructure>> taskLogic = (indicator) -> {
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building profile radar charts started");
                ProjectElement javaProject = getOrBuildProjectMetricsModel(indicator);
                
                // Ensure class fitness functions are calculated first
                Map<FitnessFunction, Set<ClassElement>> classesByProfile = cacheService.getClassesByProfile();
                if (classesByProfile == null) {
                    // Calculate class fitness functions synchronously
                    classesByProfile = runTaskSynchronously(
                            "Building class level fitness functions for profile radar charts",
                            (progressIndicator) -> new ClassFitnessFunctionCalculator().calculate(project, javaProject),
                            indicator
                    );
                    cacheService.putUserData(CacheService.CLASS_LEVEL_FITNESS_FUNCTION, classesByProfile);
                    cacheService.putUserData(CacheService.CLASSES_BY_PROFILE, classesByProfile);
                }
                
                ProfileRadarDataCalculator calculator = new ProfileRadarDataCalculator();
                List<ProfileRadarChartBuilder.RadarChartStructure> newRadarCharts = calculator.calculate(classesByProfile, project);
                cacheService.putUserData(CacheService.RADAR_CHART, newRadarCharts);
                return newRadarCharts;
            };
            Consumer<List<ProfileRadarChartBuilder.RadarChartStructure>> onSuccessCallback = (newRadarCharts) -> {
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building profile radar charts finished");
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).profilesRadarChartIsReady();
            };
            Runnable onCancelCallback = () -> project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building profile radar charts canceled");

            MetricsBackgroundableTask<List<ProfileRadarChartBuilder.RadarChartStructure>> genericTask = new MetricsBackgroundableTask<>(
                    project,
                    "Building Profile Radar Charts",
                    true, // canBeCancelled
                    taskLogic,
                    onSuccessCallback,
                    onCancelCallback,
                    null // onFinished
            );
            taskQueueService.queue(genericTask);
        }
    }

    @Override
    public void calculateProfileTreeMap() {
        MetricTreeMap<CodeElement> profileTreeMap = cacheService.getUserData(CacheService.PROFILE_TREE_MAP);
        Map<FitnessFunction, Set<ClassElement>> classesByProfile = cacheService.getClassesByProfile();
        if (profileTreeMap != null && classesByProfile != null) {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).profileTreeMapIsReady();
        } else {
            Function<ProgressIndicator, MetricTreeMap<CodeElement>> taskLogic = (indicator) -> {
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building profile tree map started");
                ProjectElement javaProject = getOrBuildProjectMetricsModel(indicator);
                
                // Ensure class fitness functions are calculated first
                Map<FitnessFunction, Set<ClassElement>> newClassesByProfile = cacheService.getClassesByProfile();
                if (newClassesByProfile == null) {
                    // Calculate class fitness functions synchronously
                    newClassesByProfile = runTaskSynchronously(
                            "Building class level fitness functions for profile tree map",
                            (progressIndicator) -> new ClassFitnessFunctionCalculator().calculate(project, javaProject),
                            indicator
                    );
                    cacheService.putUserData(CacheService.CLASS_LEVEL_FITNESS_FUNCTION, newClassesByProfile);
                    cacheService.putUserData(CacheService.CLASSES_BY_PROFILE, newClassesByProfile);
                }
                
                ProfileTreeMapModelCalculator calculator = new ProfileTreeMapModelCalculator();
                MetricTreeMap<CodeElement> newProfileTreeMap = calculator.calculate(javaProject);
                cacheService.putUserData(CacheService.PROFILE_TREE_MAP, newProfileTreeMap);
                return newProfileTreeMap;
            };
            Consumer<MetricTreeMap<CodeElement>> onSuccessCallback = (newProfileTreeMap) -> {
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building profile tree map finished");
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).profileTreeMapIsReady();
            };
            Runnable onCancelCallback = () -> project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building profile tree map canceled");

            MetricsBackgroundableTask<MetricTreeMap<CodeElement>> genericTask = new MetricsBackgroundableTask<>(
                    project,
                    "Building Profile Tree Map",
                    true, // canBeCancelled
                    taskLogic,
                    onSuccessCallback,
                    onCancelCallback,
                    null // onFinished
            );
            taskQueueService.queue(genericTask);
        }
    }

    @Override
    public void calculateProjectMetricsHistoryChart() {
        XYChart projectMetricsHistoryChart = cacheService.getUserData(CacheService.PROJECT_METRICS_HISTORY_XY_CHART);
        if (projectMetricsHistoryChart != null) {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).projectMetricsHistoryXyChartIsReady();
        } else {
            Function<ProgressIndicator, XYChart> taskLogic = (indicator) -> {
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building project metrics history chart started");
                ProjectElement javaProject = getOrBuildProjectMetricsModel(indicator);
                ProjectHistoryChartDataCalculator calculator = new ProjectHistoryChartDataCalculator();
                XYChart newProjectMetricsHistoryChart = calculator.calculate(project);
                cacheService.putUserData(CacheService.PROJECT_METRICS_HISTORY_XY_CHART, newProjectMetricsHistoryChart);
                return newProjectMetricsHistoryChart;
            };
            Consumer<XYChart> onSuccessCallback = (newProjectMetricsHistoryChart) -> {
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building project metrics history chart finished");
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).projectMetricsHistoryXyChartIsReady();
            };
            Runnable onCancelCallback = () -> project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building project metrics history chart canceled");

            MetricsBackgroundableTask<XYChart> genericTask = new MetricsBackgroundableTask<>(
                    project,
                    "Building Project Metrics History Chart",
                    true, // canBeCancelled
                    taskLogic,
                    onSuccessCallback,
                    onCancelCallback,
                    null // onFinished
            );
            taskQueueService.queue(genericTask);
        }
    }

    @Override
    public void exportToXml(String fileName) {
        MetricsBackgroundableTask<Void> genericTask = new MetricsBackgroundableTask<>(
                project,
                "Export Project, Package, Class And Method Levels Metrics To XML",
                true, // canBeCancelled
                (indicator) -> {
                    project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Export project, package, class and method levels metrics to .xml started");
                    ProjectElement javaProject = getOrBuildProjectMetricsModel(indicator);
                    if (fileName != null) {
                        XmlExporter exporter = new XmlExporter(project);
                        exporter.export(fileName, javaProject);
                    }
                    return null;
                },
                (v) -> {
                    project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Export project, package, class and method levels metrics to .xml finished");
                },
                () -> project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Export project, package, class and method levels metrics to .xml canceled"),
                null
        );
        taskQueueService.queue(genericTask);
    }

    @Override
    public void exportClassMetricsToCsv(String fileName) {
        MetricsBackgroundableTask<Void> genericTask = new MetricsBackgroundableTask<>(
                project,
                "Export Class Level Metrics To CSV",
                true, // canBeCancelled
                (indicator) -> {
                    project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Export class level metrics to .csv started");
                    ProjectElement javaProject = getOrBuildProjectMetricsModel(indicator);
                    if (fileName != null) {
                        CsvClassMetricsExporter exporter = new CsvClassMetricsExporter(project);
                        exporter.export(fileName, javaProject);
                    }
                    return null;
                },
                (v) -> {
                    project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Export class level metrics to .csv finished");
                },
                () -> project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Export class level metrics to .csv canceled"),
                null
        );
        taskQueueService.queue(genericTask);
    }

    @Override
    public void exportMethodMetricsToCsv(String fileName) {
        MetricsBackgroundableTask<Void> genericTask = new MetricsBackgroundableTask<>(
                project,
                "Export Method Level Metrics To CSV",
                true, // canBeCancelled
                (indicator) -> {
                    project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Export method level metrics to .csv started");
                    ProjectElement javaProject = getOrBuildProjectMetricsModel(indicator);
                    if (fileName != null) {
                        CsvMethodMetricsExporter exporter = new CsvMethodMetricsExporter(project);
                        exporter.export(fileName, javaProject);
                    }
                    return null;
                },
                (v) -> {
                    project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Export method level metrics to .csv finished");
                },
                () -> project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Export method level metrics to .csv canceled"),
                null
        );
        taskQueueService.queue(genericTask);
    }

    @Override
    public void exportPackageMetricsToCsv(String fileName) {
        MetricsBackgroundableTask<Void> genericTask = new MetricsBackgroundableTask<>(
                project,
                "Export Package Level Metrics To CSV",
                true, // canBeCancelled
                (indicator) -> {
                    project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Export package level metrics to .csv started");
                    ProjectElement javaProject = getOrBuildProjectMetricsModel(indicator);
                    if (fileName != null) {
                        CsvPackageMetricsExporter exporter = new CsvPackageMetricsExporter(project);
                        exporter.export(fileName, javaProject);
                    }
                    return null;
                },
                (v) -> {
                    project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Export package level metrics to .csv finished");
                },
                () -> project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Export package level metrics to .csv canceled"),
                null
        );
        taskQueueService.queue(genericTask);
    }

    @Override
    public void calculateClassFitnessFunctions() {
        Map<FitnessFunction, Set<ClassElement>> classFitnessFunctions = cacheService.getUserData(CacheService.CLASS_LEVEL_FITNESS_FUNCTION);
        if (classFitnessFunctions != null) {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).classLevelFitnessFunctionIsReady();
        } else {
            Function<ProgressIndicator, Map<FitnessFunction, Set<ClassElement>>> taskLogic = (indicator) -> {
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building class level fitness functions started");
                ProjectElement javaProject = getOrBuildProjectMetricsModel(indicator);
                Map<FitnessFunction, Set<ClassElement>> newClassFitnessFunctions = new ClassFitnessFunctionCalculator().calculate(project, javaProject);
                cacheService.putUserData(CacheService.CLASS_LEVEL_FITNESS_FUNCTION, newClassFitnessFunctions);
                cacheService.putUserData(CacheService.CLASSES_BY_PROFILE, newClassFitnessFunctions);
                return newClassFitnessFunctions;
            };
            Consumer<Map<FitnessFunction, Set<ClassElement>>> onSuccessCallback = (newClassFitnessFunctions) -> {
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building class level fitness functions finished");
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).classLevelFitnessFunctionIsReady();
            };
            Runnable onCancelCallback = () -> project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building class level fitness functions canceled");

            MetricsBackgroundableTask<Map<FitnessFunction, Set<ClassElement>>> genericTask = new MetricsBackgroundableTask<>(
                    project,
                    "Building class level fitness functions",
                    true, // canBeCancelled
                    taskLogic,
                    onSuccessCallback,
                    onCancelCallback,
                    null // onFinished
            );
            taskQueueService.queue(genericTask);
        }
    }

    @Override
    public void calculatePackageFitnessFunctions() {
        Map<FitnessFunction, Set<PackageElement>> packageFitnessFunctions = cacheService.getUserData(CacheService.PACKAGE_LEVEL_FITNESS_FUNCTION);
        if (packageFitnessFunctions != null) {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).packageLevelFitnessFunctionIsReady();
        } else {
            Function<ProgressIndicator, Map<FitnessFunction, Set<PackageElement>>> taskLogic = (indicator) -> {
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building package level fitness functions started");
                ProjectElement javaProject = getOrBuildProjectMetricsModel(indicator);
                Map<FitnessFunction, Set<PackageElement>> newPackageFitnessFunctions = new PackageFitnessFunctionCalculator().calculate(project, javaProject);
                cacheService.putUserData(CacheService.PACKAGE_LEVEL_FITNESS_FUNCTION, newPackageFitnessFunctions);
                return newPackageFitnessFunctions;
            };
            Consumer<Map<FitnessFunction, Set<PackageElement>>> onSuccessCallback = (newPackageFitnessFunctions) -> {
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building package level fitness functions finished");
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).packageLevelFitnessFunctionIsReady();
            };
            Runnable onCancelCallback = () -> project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building package level fitness functions canceled");

            MetricsBackgroundableTask<Map<FitnessFunction, Set<PackageElement>>> genericTask = new MetricsBackgroundableTask<>(
                    project,
                    "Building package level fitness functions",
                    true, // canBeCancelled
                    taskLogic,
                    onSuccessCallback,
                    onCancelCallback,
                    null // onFinished
            );
            taskQueueService.queue(genericTask);
        }
    }

    private void logMetricDifferences(ProjectElement javaProject) {
        javaProject.allClasses().forEach(classElement -> {
            classElement.metrics().forEach(metric -> {
                if (metric.getJavaParserValue() != null && !metric.getJavaParserValue().equals(metric.getPsiValue())) {
                    String message = "Class:" + classElement.getName() + " " +
                            "Metric:" + metric.getType().name() + " " +
                            "PSI:" + metric.getPsiValue() + " " +
                            "JavaParser:" + metric.getJavaParserValue();
                    project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(message);
                }
            });
            classElement.methods().forEach(methodElement -> {
                methodElement.metrics().forEach(metric -> {
                    if (metric.getJavaParserValue() != null && !metric.getJavaParserValue().equals(metric.getPsiValue())) {
                        String message = "Class.Method name:" + classElement.getName() + "." + methodElement.getName() + " " +
                                "Metric:" + metric.getType().name() + " " +
                                "PSI:" + metric.getPsiValue() + " " +
                                "JavaParser:" + metric.getJavaParserValue();
                        project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(message);
                    }
                });
            });
        });
    }
}
