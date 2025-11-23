package org.b333vv.metric.service;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.b333vv.metric.model.code.ClassElement;
import org.b333vv.metric.model.code.CodeElement;
import org.b333vv.metric.model.code.PackageElement;
import org.b333vv.metric.ui.chart.builder.ProfileBoxChartBuilder;
import org.b333vv.metric.ui.chart.builder.ProfileRadarChartBuilder;
import org.b333vv.metric.ui.fitnessfunction.FitnessFunction;
import org.b333vv.metric.ui.treemap.presentation.MetricTreeMap;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.HeatMapChart;
import org.knowm.xchart.XYChart;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.swing.tree.DefaultTreeModel;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CacheServiceTest extends BasePlatformTestCase {

    private CacheService cacheService;
    @Mock
    private Project project;
    @Mock
    private Module module1;
    @Mock
    private Module module2;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.openMocks(this);
        when(module1.getName()).thenReturn("Module1");
        when(module2.getName()).thenReturn("Module2");
        cacheService = new CacheService(project);
    }

    public void testModuleAwareCaching() {
        // Test Project Tree Cache
        DefaultTreeModel treeModel1 = mock(DefaultTreeModel.class);
        DefaultTreeModel treeModel2 = mock(DefaultTreeModel.class);
        cacheService.putProjectTree(module1, treeModel1);
        cacheService.putProjectTree(module2, treeModel2);

        assertEquals(treeModel1, cacheService.getProjectTree(module1));
        assertEquals(treeModel2, cacheService.getProjectTree(module2));
        assertNull(cacheService.getProjectTree(null)); // Project Root

        // Test Pie Chart Cache
        List<org.b333vv.metric.ui.chart.builder.MetricPieChartBuilder.PieChartStructure> pieCharts1 = Collections
                .emptyList();
        List<org.b333vv.metric.ui.chart.builder.MetricPieChartBuilder.PieChartStructure> pieCharts2 = Collections
                .emptyList();
        cacheService.putPieChartList(module1, pieCharts1);
        cacheService.putPieChartList(module2, pieCharts2);

        assertEquals(pieCharts1, cacheService.getPieChartList(module1));
        assertEquals(pieCharts2, cacheService.getPieChartList(module2));

        // Test Category Chart Cache
        CategoryChart categoryChart1 = mock(CategoryChart.class);
        CategoryChart categoryChart2 = mock(CategoryChart.class);
        cacheService.putCategoryChart(module1, categoryChart1);
        cacheService.putCategoryChart(module2, categoryChart2);

        assertEquals(categoryChart1, cacheService.getCategoryChart(module1));
        assertEquals(categoryChart2, cacheService.getCategoryChart(module2));

        // Test XY Chart Cache
        XYChart xyChart1 = mock(XYChart.class);
        XYChart xyChart2 = mock(XYChart.class);
        cacheService.putXyChart(module1, xyChart1);
        cacheService.putXyChart(module2, xyChart2);

        assertEquals(xyChart1, cacheService.getXyChart(module1));
        assertEquals(xyChart2, cacheService.getXyChart(module2));

        // Test Metric Tree Map Cache
        MetricTreeMap<CodeElement> treeMap1 = mock(MetricTreeMap.class);
        MetricTreeMap<CodeElement> treeMap2 = mock(MetricTreeMap.class);
        cacheService.putMetricTreeMap(module1, treeMap1);
        cacheService.putMetricTreeMap(module2, treeMap2);

        assertEquals(treeMap1, cacheService.getMetricTreeMap(module1));
        assertEquals(treeMap2, cacheService.getMetricTreeMap(module2));

        // Test Class Level Fitness Functions
        Map<FitnessFunction, Set<ClassElement>> classFitness1 = Collections.emptyMap();
        Map<FitnessFunction, Set<ClassElement>> classFitness2 = Collections.emptyMap();
        cacheService.putClassLevelFitnessFunctions(module1, classFitness1);
        cacheService.putClassLevelFitnessFunctions(module2, classFitness2);

        assertEquals(classFitness1, cacheService.getClassLevelFitnessFunctions(module1));
        assertEquals(classFitness2, cacheService.getClassLevelFitnessFunctions(module2));

        // Test Package Level Fitness Functions
        Map<FitnessFunction, Set<PackageElement>> packageFitness1 = Collections.emptyMap();
        Map<FitnessFunction, Set<PackageElement>> packageFitness2 = Collections.emptyMap();
        cacheService.putPackageLevelFitnessFunctions(module1, packageFitness1);
        cacheService.putPackageLevelFitnessFunctions(module2, packageFitness2);

        assertEquals(packageFitness1, cacheService.getPackageLevelFitnessFunctions(module1));
        assertEquals(packageFitness2, cacheService.getPackageLevelFitnessFunctions(module2));

        // Test Profile Box Charts
        List<ProfileBoxChartBuilder.BoxChartStructure> boxCharts1 = Collections.emptyList();
        List<ProfileBoxChartBuilder.BoxChartStructure> boxCharts2 = Collections.emptyList();
        cacheService.putBoxCharts(module1, boxCharts1);
        cacheService.putBoxCharts(module2, boxCharts2);

        assertEquals(boxCharts1, cacheService.getBoxCharts(module1));
        assertEquals(boxCharts2, cacheService.getBoxCharts(module2));

        // Test Heat Map Chart
        HeatMapChart heatMap1 = mock(HeatMapChart.class);
        HeatMapChart heatMap2 = mock(HeatMapChart.class);
        cacheService.putHeatMapChart(module1, heatMap1);
        cacheService.putHeatMapChart(module2, heatMap2);

        assertEquals(heatMap1, cacheService.getHeatMapChart(module1));
        assertEquals(heatMap2, cacheService.getHeatMapChart(module2));

        // Test Radar Charts
        List<ProfileRadarChartBuilder.RadarChartStructure> radarCharts1 = Collections.emptyList();
        List<ProfileRadarChartBuilder.RadarChartStructure> radarCharts2 = Collections.emptyList();
        cacheService.putRadarCharts(module1, radarCharts1);
        cacheService.putRadarCharts(module2, radarCharts2);

        assertEquals(radarCharts1, cacheService.getRadarCharts(module1));
        assertEquals(radarCharts2, cacheService.getRadarCharts(module2));

        // Test Profile Category Chart
        CategoryChart profileCategoryChart1 = mock(CategoryChart.class);
        CategoryChart profileCategoryChart2 = mock(CategoryChart.class);
        cacheService.putProfileCategoryChart(module1, profileCategoryChart1);
        cacheService.putProfileCategoryChart(module2, profileCategoryChart2);

        assertEquals(profileCategoryChart1, cacheService.getProfileCategoryChart(module1));
        assertEquals(profileCategoryChart2, cacheService.getProfileCategoryChart(module2));

        // Test Profile Tree Map
        MetricTreeMap<CodeElement> profileTreeMap1 = mock(MetricTreeMap.class);
        MetricTreeMap<CodeElement> profileTreeMap2 = mock(MetricTreeMap.class);
        cacheService.putProfileTreeMap(module1, profileTreeMap1);
        cacheService.putProfileTreeMap(module2, profileTreeMap2);

        assertEquals(profileTreeMap1, cacheService.getProfileTreeMap(module1));
        assertEquals(profileTreeMap2, cacheService.getProfileTreeMap(module2));
    }

    public void testInvalidateUserData() {
        DefaultTreeModel treeModel = mock(DefaultTreeModel.class);
        cacheService.putProjectTree(module1, treeModel);
        assertNotNull(cacheService.getProjectTree(module1));

        cacheService.invalidateUserData();
        assertNull(cacheService.getProjectTree(module1));
    }
}
