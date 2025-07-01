package org.b333vv.metric.service;

import com.intellij.openapi.project.Project;

import org.b333vv.metric.builder.ProjectTreeModelCalculator;
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.task.ProjectTreeTask;
import org.b333vv.metric.service.TaskQueueService;
import org.b333vv.metric.util.SettingsService;

import javax.swing.tree.DefaultTreeModel;

import org.b333vv.metric.builder.PieChartDataCalculator;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.task.MetricTaskManager;
import org.b333vv.metric.task.PieChartTask;
import org.b333vv.metric.ui.chart.builder.MetricPieChartBuilder;
import org.b333vv.metric.task.CategoryChartTask;
import org.knowm.xchart.CategoryChart;

import java.util.List;
import org.b333vv.metric.model.code.JavaCode;
import org.b333vv.metric.task.MetricTreeMapTask;
import org.b333vv.metric.ui.treemap.presentation.MetricTreeMap;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.HeatMapChart;
import org.b333vv.metric.ui.chart.builder.ProfileBoxChartBuilder;
import org.b333vv.metric.ui.chart.builder.ProfileRadarChartBuilder;
import org.b333vv.metric.task.XyChartTask;
import org.b333vv.metric.task.ProfileBoxChartsTask;
import org.b333vv.metric.task.ProfileCategoryChartTask;
import org.b333vv.metric.task.ProfileHeatMapChartTask;
import org.b333vv.metric.task.ProfileRadarChartsTask;
import org.b333vv.metric.task.ProfileTreeMapTask;
import org.b333vv.metric.task.ProjectMetricsHistoryChartTask;
import org.b333vv.metric.task.ExportToXmlTask;
import org.b333vv.metric.task.ExportClassMetricsToCsvTask;
import org.b333vv.metric.task.ExportMethodMetricsToCsvTask;
import org.b333vv.metric.task.ExportPackageMetricsToCsvTask;
import java.util.Map;
import java.util.Set;
import org.b333vv.metric.ui.fitnessfunction.FitnessFunction;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.code.JavaPackage;
import org.b333vv.metric.task.ClassFitnessFunctionsTask;
import org.b333vv.metric.task.PackageFitnessFunctionsTask;

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
            ProjectTreeModelCalculator calculator = new ProjectTreeModelCalculator(project);
            ProjectTreeTask task = new ProjectTreeTask(project, () -> calculator.calculate(), cacheService);
            taskQueueService.queue(task);
        }
    }

    @Override
    public void calculatePieChart() {
        List<MetricPieChartBuilder.PieChartStructure> pieChartList = cacheService.getUserData(CacheService.PIE_CHART_LIST);
        if (pieChartList != null) {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).pieChartIsReady();
        } else {
            PieChartTask task = new PieChartTask(project);
            taskQueueService.queue(task);
        }
    }

    @Override
    public void calculateCategoryChart() {
        CategoryChart categoryChart = cacheService.getUserData(CacheService.CATEGORY_CHART);
        if (categoryChart != null) {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).categoryChartIsReady();
        } else {
            CategoryChartTask task = new CategoryChartTask(project);
            taskQueueService.queue(task);
        }
    }

    @Override
    public void calculateMetricTreeMap() {
        MetricTreeMap<JavaCode> metricTreeMap = cacheService.getUserData(CacheService.METRIC_TREE_MAP);
        if (metricTreeMap != null) {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).metricTreeMapIsReady();
        } else {
            MetricTreeMapTask task = new MetricTreeMapTask(project);
            taskQueueService.queue(task);
        }
    }

    @Override
    public void calculateXyChart() {
        XYChart xyChart = cacheService.getUserData(CacheService.XY_CHART);
        if (xyChart != null) {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).xyChartIsReady();
        } else {
            XyChartTask task = new XyChartTask(project);
            taskQueueService.queue(task);
        }
    }

    @Override
    public void calculateProfileBoxCharts() {
        List<ProfileBoxChartBuilder.BoxChartStructure> boxCharts = cacheService.getUserData(CacheService.BOX_CHARTS);
        if (boxCharts != null) {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).profilesBoxChartIsReady();
        } else {
            ProfileBoxChartsTask task = new ProfileBoxChartsTask(project);
            taskQueueService.queue(task);
        }
    }

    @Override
    public void calculateProfileCategoryChart() {
        CategoryChart profileCategoryChart = cacheService.getUserData(CacheService.PROFILE_CATEGORY_CHART);
        if (profileCategoryChart != null) {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).profilesCategoryChartIsReady();
        } else {
            ProfileCategoryChartTask task = new ProfileCategoryChartTask(project);
            taskQueueService.queue(task);
        }
    }

    @Override
    public void calculateProfileHeatMapChart() {
        HeatMapChart heatMapChart = cacheService.getUserData(CacheService.HEAT_MAP_CHART);
        if (heatMapChart != null) {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).profilesHeatMapChartIsReady();
        } else {
            ProfileHeatMapChartTask task = new ProfileHeatMapChartTask(project);
            taskQueueService.queue(task);
        }
    }

    @Override
    public void calculateProfileRadarCharts() {
        List<ProfileRadarChartBuilder.RadarChartStructure> radarChart = cacheService.getUserData(CacheService.RADAR_CHART);
        if (radarChart != null) {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).profilesRadarChartIsReady();
        } else {
            ProfileRadarChartsTask task = new ProfileRadarChartsTask(project);
            taskQueueService.queue(task);
        }
    }

    @Override
    public void calculateProfileTreeMap() {
        MetricTreeMap<JavaCode> profileTreeMap = cacheService.getUserData(CacheService.PROFILE_TREE_MAP);
        if (profileTreeMap != null) {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).profileTreeMapIsReady();
        } else {
            ProfileTreeMapTask task = new ProfileTreeMapTask(project);
            taskQueueService.queue(task);
        }
    }

    @Override
    public void calculateProjectMetricsHistoryChart() {
        XYChart projectMetricsHistoryChart = cacheService.getUserData(CacheService.PROJECT_METRICS_HISTORY_XY_CHART);
        if (projectMetricsHistoryChart != null) {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).projectMetricsHistoryXyChartIsReady();
        } else {
            ProjectMetricsHistoryChartTask task = new ProjectMetricsHistoryChartTask(project);
            taskQueueService.queue(task);
        }
    }

    @Override
    public void exportToXml(String fileName) {
        ExportToXmlTask task = new ExportToXmlTask(project, fileName);
        taskQueueService.queue(task);
    }

    @Override
    public void exportClassMetricsToCsv(String fileName) {
        ExportClassMetricsToCsvTask task = new ExportClassMetricsToCsvTask(project, fileName);
        taskQueueService.queue(task);
    }

    @Override
    public void exportMethodMetricsToCsv(String fileName) {
        ExportMethodMetricsToCsvTask task = new ExportMethodMetricsToCsvTask(project, fileName);
        taskQueueService.queue(task);
    }

    @Override
    public void exportPackageMetricsToCsv(String fileName) {
        ExportPackageMetricsToCsvTask task = new ExportPackageMetricsToCsvTask(project, fileName);
        taskQueueService.queue(task);
    }

    @Override
    public void calculateClassFitnessFunctions() {
        Map<FitnessFunction, Set<JavaClass>> classFitnessFunctions = cacheService.getUserData(CacheService.CLASS_LEVEL_FITNESS_FUNCTION);
        if (classFitnessFunctions != null) {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).classLevelFitnessFunctionIsReady();
        } else {
            ClassFitnessFunctionsTask task = new ClassFitnessFunctionsTask(project);
            taskQueueService.queue(task);
        }
    }

    @Override
    public void calculatePackageFitnessFunctions() {
        Map<FitnessFunction, Set<JavaPackage>> packageFitnessFunctions = cacheService.getUserData(CacheService.PACKAGE_LEVEL_FITNESS_FUNCTION);
        if (packageFitnessFunctions != null) {
            project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).packageLevelFitnessFunctionIsReady();
        } else {
            PackageFitnessFunctionsTask task = new PackageFitnessFunctionsTask(project);
            taskQueueService.queue(task);
        }
    }
}
