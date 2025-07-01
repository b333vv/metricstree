package org.b333vv.metric.builder;

import com.intellij.openapi.project.Project;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.ui.chart.builder.ProfileRadarChartBuilder;
import org.b333vv.metric.ui.profile.FitnessFunction;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProfileRadarDataCalculator {
    public List<ProfileRadarChartBuilder.RadarChartStructure> calculate(Map<FitnessFunction, Set<JavaClass>> classesByProfile, Project project) {
        ProfileRadarChartBuilder profileRadarChartBuilder = new ProfileRadarChartBuilder(project, classesByProfile);
        return profileRadarChartBuilder.getChartStructures();
    }
}
