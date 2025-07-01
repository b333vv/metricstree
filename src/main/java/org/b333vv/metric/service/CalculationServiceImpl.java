package org.b333vv.metric.service;

import com.intellij.openapi.project.Project;

import org.b333vv.metric.builder.MetricsBackgroundableTask;
import org.b333vv.metric.builder.ProjectTreeModelCalculator;
import org.b333vv.metric.event.MetricsEventListener;

import org.b333vv.metric.service.TaskQueueService;
import org.b333vv.metric.util.SettingsService;

import javax.swing.tree.DefaultTreeModel;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.b333vv.metric.builder.PieChartDataCalculator;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.task.MetricTaskManager;

import org.b333vv.metric.ui.chart.builder.MetricPieChartBuilder;
import org.b333vv.metric.builder.CategoryChartDataCalculator;
import org.knowm.xchart.CategoryChart;

import java.util.List;
import org.b333vv.metric.model.code.JavaCode;
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
import org.b333vv.metric.ui.fitnessfunction.FitnessFunction;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.code.JavaPackage;
import org.b333vv.metric.builder.ClassFitnessFunctionCalculator;
import org.b333vv.metric.builder.PackageFitnessFunctionCalculator;

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

    @Override
    public void calculateProjectTree() {
        project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).clearProjectMetricsTree();

        DefaultTreeModel treeModel = cacheService.getUserData(CacheService.PROJECT_TREE);

        if (treeModel != null) {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).projectMetricsTreeIsReady(treeModel);
        } else {
            Supplier<DefaultTreeModel> taskLogic = new ProjectTreeModelCalculator(project)::calculate;
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
        if (pieChartList != null) {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).pieChartIsReady();
        } else {
            Supplier<List<MetricPieChartBuilder.PieChartStructure>> taskLogic = () -> {
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building classes distribution by metric values pie chart started");
                JavaProject javaProject = project.getService(CacheService.class).getProject();
                PieChartDataCalculator calculator = new PieChartDataCalculator();
                List<MetricPieChartBuilder.PieChartStructure> newPieChartList = calculator.calculate(javaProject, project);
                project.getService(CacheService.class).putUserData(CacheService.PIE_CHART_LIST, newPieChartList);
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

    @Override
    public void calculateCategoryChart() {
        CategoryChart categoryChart = cacheService.getUserData(CacheService.CATEGORY_CHART);
        if (categoryChart != null) {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).categoryChartIsReady();
        } else {
            Supplier<CategoryChart> taskLogic = () -> {
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building classes distribution by metric values category chart started");
                JavaProject javaProject = project.getService(CacheService.class).getProject();
                CategoryChartDataCalculator calculator = new CategoryChartDataCalculator();
                CategoryChart newCategoryChart = calculator.calculate(javaProject, project);
                project.getService(CacheService.class).putUserData(CacheService.CATEGORY_CHART, newCategoryChart);
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
        MetricTreeMap<JavaCode> metricTreeMap = cacheService.getUserData(CacheService.METRIC_TREE_MAP);
        if (metricTreeMap != null) {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).metricTreeMapIsReady();
        } else {
            Supplier<MetricTreeMap<JavaCode>> taskLogic = () -> {
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building treemap with metric types distribution started");
                JavaProject javaProject = project.getService(CacheService.class).getProject();
                MetricTreeMapModelCalculator calculator = new MetricTreeMapModelCalculator();
                MetricTreeMap<JavaCode> newMetricTreeMap = calculator.calculate(javaProject);
                project.getService(CacheService.class).putUserData(CacheService.METRIC_TREE_MAP, newMetricTreeMap);
                return newMetricTreeMap;
            };
            Consumer<MetricTreeMap<JavaCode>> onSuccessCallback = (newMetricTreeMap) -> {
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building treemap with metric types distribution finished");
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).metricTreeMapIsReady();
            };
            Runnable onCancelCallback = () -> project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building treemap with metric types distribution canceled");

            MetricsBackgroundableTask<MetricTreeMap<JavaCode>> genericTask = new MetricsBackgroundableTask<>(
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
        if (xyChart != null) {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).xyChartIsReady();
        } else {
            Supplier<XYChart> taskLogic = () -> {
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building XY chart started");
                JavaProject javaProject = project.getService(CacheService.class).getProject();
                XyChartDataCalculator calculator = new XyChartDataCalculator();
                XYChart newXyChart = calculator.calculate(javaProject, project);
                project.getService(CacheService.class).putUserData(CacheService.XY_CHART, newXyChart);
                return newXyChart;
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
            Supplier<List<ProfileBoxChartBuilder.BoxChartStructure>> taskLogic = () -> {
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building profile box charts started");
                ProfileBoxChartDataCalculator calculator = new ProfileBoxChartDataCalculator();
                List<ProfileBoxChartBuilder.BoxChartStructure> newBoxCharts = calculator.calculate(project.getService(CacheService.class).getClassesByProfile());
                project.getService(CacheService.class).putUserData(CacheService.BOX_CHARTS, newBoxCharts);
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
            Supplier<CategoryChart> taskLogic = () -> {
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building profile category chart started");
                ProfileCategoryChartDataCalculator calculator = new ProfileCategoryChartDataCalculator();
                CategoryChart newCategoryChart = calculator.calculate(project.getService(CacheService.class).getClassesByProfile());
                project.getService(CacheService.class).putUserData(CacheService.PROFILE_CATEGORY_CHART, newCategoryChart);
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
            Supplier<HeatMapChart> taskLogic = () -> {
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building profile heat map chart started");
                ProfileHeatMapDataCalculator calculator = new ProfileHeatMapDataCalculator();
                HeatMapChart newHeatMapChart = calculator.calculate(project.getService(CacheService.class).getClassesByProfile());
                project.getService(CacheService.class).putUserData(CacheService.HEAT_MAP_CHART, newHeatMapChart);
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
            Supplier<List<ProfileRadarChartBuilder.RadarChartStructure>> taskLogic = () -> {
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building profile radar charts started");
                ProfileRadarDataCalculator calculator = new ProfileRadarDataCalculator();
                List<ProfileRadarChartBuilder.RadarChartStructure> newRadarCharts = calculator.calculate(project.getService(CacheService.class).getClassesByProfile(), project);
                project.getService(CacheService.class).putUserData(CacheService.RADAR_CHART, newRadarCharts);
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
        MetricTreeMap<JavaCode> profileTreeMap = cacheService.getUserData(CacheService.PROFILE_TREE_MAP);
        if (profileTreeMap != null) {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).profileTreeMapIsReady();
        } else {
            Supplier<MetricTreeMap<JavaCode>> taskLogic = () -> {
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building profile tree map started");
                ProfileTreeMapModelCalculator calculator = new ProfileTreeMapModelCalculator();
                MetricTreeMap<JavaCode> newProfileTreeMap = calculator.calculate(project.getService(CacheService.class).getProject());
                project.getService(CacheService.class).putUserData(CacheService.PROFILE_TREE_MAP, newProfileTreeMap);
                return newProfileTreeMap;
            };
            Consumer<MetricTreeMap<JavaCode>> onSuccessCallback = (newProfileTreeMap) -> {
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building profile tree map finished");
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).profileTreeMapIsReady();
            };
            Runnable onCancelCallback = () -> project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building profile tree map canceled");

            MetricsBackgroundableTask<MetricTreeMap<JavaCode>> genericTask = new MetricsBackgroundableTask<>(
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
            Supplier<XYChart> taskLogic = () -> {
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building project metrics history chart started");
                ProjectHistoryChartDataCalculator calculator = new ProjectHistoryChartDataCalculator();
                XYChart newProjectMetricsHistoryChart = calculator.calculate(project);
                project.getService(CacheService.class).putUserData(CacheService.PROJECT_METRICS_HISTORY_XY_CHART, newProjectMetricsHistoryChart);
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
        Supplier<Void> taskLogic = () -> {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Export project, package, class and method levels metrics to .xml started");
            JavaProject javaProject = project.getService(MetricTaskManager.class).getProjectModel(null); // indicator is not available here, this logic must be inside the supplier
            if (fileName != null) {
                XmlExporter exporter = new XmlExporter(project);
                exporter.export(fileName, javaProject);
            }
            return null; // Supplier<Void> must return null
        };
        Consumer<Void> onSuccessCallback = (v) -> {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Export project, package, class and method levels metrics to .xml finished");
        };
        Runnable onCancelCallback = () -> project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Export project, package, class and method levels metrics to .xml canceled");

        MetricsBackgroundableTask<Void> genericTask = new MetricsBackgroundableTask<>(
                project,
                "Export Project, Package, Class And Method Levels Metrics To XML",
                true, // canBeCancelled
                taskLogic,
                onSuccessCallback,
                onCancelCallback,
                null // onFinished
        );
        taskQueueService.queue(genericTask);
    }

    @Override
    public void exportClassMetricsToCsv(String fileName) {
        Supplier<Void> taskLogic = () -> {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Export class level metrics to .csv started");
            JavaProject javaProject = project.getService(MetricTaskManager.class).getProjectModel(null);
            if (fileName != null) {
                CsvClassMetricsExporter exporter = new CsvClassMetricsExporter(project);
                exporter.export(fileName, javaProject);
            }
            return null;
        };
        Consumer<Void> onSuccessCallback = (v) -> {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Export class level metrics to .csv finished");
        };
        Runnable onCancelCallback = () -> project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Export class level metrics to .csv canceled");

        MetricsBackgroundableTask<Void> genericTask = new MetricsBackgroundableTask<>(
                project,
                "Export Class Level Metrics To CSV",
                true, // canBeCancelled
                taskLogic,
                onSuccessCallback,
                onCancelCallback,
                null // onFinished
        );
        taskQueueService.queue(genericTask);
    }

    @Override
    public void exportMethodMetricsToCsv(String fileName) {
        Supplier<Void> taskLogic = () -> {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Export method level metrics to .csv started");
            JavaProject javaProject = project.getService(MetricTaskManager.class).getProjectModel(null);
            if (fileName != null) {
                CsvMethodMetricsExporter exporter = new CsvMethodMetricsExporter(project);
                exporter.export(fileName, javaProject);
            }
            return null;
        };
        Consumer<Void> onSuccessCallback = (v) -> {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Export method level metrics to .csv finished");
        };
        Runnable onCancelCallback = () -> project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Export method level metrics to .csv canceled");

        MetricsBackgroundableTask<Void> genericTask = new MetricsBackgroundableTask<>(
                project,
                "Export Method Level Metrics To CSV",
                true, // canBeCancelled
                taskLogic,
                onSuccessCallback,
                onCancelCallback,
                null // onFinished
        );
        taskQueueService.queue(genericTask);
    }

    @Override
    public void exportPackageMetricsToCsv(String fileName) {
        Supplier<Void> taskLogic = () -> {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Export package level metrics to .csv started");
            JavaProject javaProject = project.getService(MetricTaskManager.class).getProjectModel(null);
            if (fileName != null) {
                CsvPackageMetricsExporter exporter = new CsvPackageMetricsExporter(project);
                exporter.export(fileName, javaProject);
            }
            return null;
        };
        Consumer<Void> onSuccessCallback = (v) -> {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Export package level metrics to .csv finished");
        };
        Runnable onCancelCallback = () -> project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Export package level metrics to .csv canceled");

        MetricsBackgroundableTask<Void> genericTask = new MetricsBackgroundableTask<>(
                project,
                "Export Package Level Metrics To CSV",
                true, // canBeCancelled
                taskLogic,
                onSuccessCallback,
                onCancelCallback,
                null // onFinished
        );
        taskQueueService.queue(genericTask);
    }

    @Override
    public void calculateClassFitnessFunctions() {
        Map<FitnessFunction, Set<JavaClass>> classFitnessFunctions = cacheService.getUserData(CacheService.CLASS_LEVEL_FITNESS_FUNCTION);
        if (classFitnessFunctions != null) {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).classLevelFitnessFunctionIsReady();
        } else {
            Supplier<Map<FitnessFunction, Set<JavaClass>>> taskLogic = () -> {
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building class level fitness functions started");
                Map<FitnessFunction, Set<JavaClass>> newClassFitnessFunctions = new ClassFitnessFunctionCalculator().calculate(project, null);
                project.getService(CacheService.class).putUserData(CacheService.CLASS_LEVEL_FITNESS_FUNCTION, newClassFitnessFunctions);
                return newClassFitnessFunctions;
            };
            Consumer<Map<FitnessFunction, Set<JavaClass>>> onSuccessCallback = (newClassFitnessFunctions) -> {
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building class level fitness functions finished");
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).classLevelFitnessFunctionIsReady();
            };
            Runnable onCancelCallback = () -> project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building class level fitness functions canceled");

            MetricsBackgroundableTask<Map<FitnessFunction, Set<JavaClass>>> genericTask = new MetricsBackgroundableTask<>(
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
        Map<FitnessFunction, Set<JavaPackage>> packageFitnessFunctions = cacheService.getUserData(CacheService.PACKAGE_LEVEL_FITNESS_FUNCTION);
        if (packageFitnessFunctions != null) {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).packageLevelFitnessFunctionIsReady();
        } else {
            Supplier<Map<FitnessFunction, Set<JavaPackage>>> taskLogic = () -> {
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building package level fitness functions started");
                Map<FitnessFunction, Set<JavaPackage>> newPackageFitnessFunctions = new PackageFitnessFunctionCalculator().calculate(project, null);
                project.getService(CacheService.class).putUserData(CacheService.PACKAGE_LEVEL_FITNESS_FUNCTION, newPackageFitnessFunctions);
                return newPackageFitnessFunctions;
            };
            Consumer<Map<FitnessFunction, Set<JavaPackage>>> onSuccessCallback = (newPackageFitnessFunctions) -> {
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building package level fitness functions finished");
                project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).packageLevelFitnessFunctionIsReady();
            };
            Runnable onCancelCallback = () -> project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo("Building package level fitness functions canceled");

            MetricsBackgroundableTask<Map<FitnessFunction, Set<JavaPackage>>> genericTask = new MetricsBackgroundableTask<>(
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
}
