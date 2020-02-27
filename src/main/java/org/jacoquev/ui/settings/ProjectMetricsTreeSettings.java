package org.jacoquev.ui.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@State(name = "ProjectMetricsTreeSettings", storages = {@Storage("project-metrics-tree.xml")})
public final class ProjectMetricsTreeSettings implements PersistentStateComponent<ProjectMetricsTreeSettings>, ProjectComponent {

    private List<MetricsTreeSettingsStub> projectTreeMetrics = new ArrayList<>();
    private boolean needToConsiderProjectMetrics;
    private boolean needToConsiderPackageMetrics;

    public ProjectMetricsTreeSettings() {
        loadInitialValues();
    }

    private void loadInitialValues() {
        needToConsiderProjectMetrics = true;
        needToConsiderPackageMetrics = true;

        //Chidamber-Kemerer metrics set
        projectTreeMetrics.add(new MetricsTreeSettingsStub("WMC", "Weighted Methods Per Class",
                "Class level", "Chidamber-Kemerer metrics set", true));
        projectTreeMetrics.add(new MetricsTreeSettingsStub("DIT", "Depth Of Inheritance Tree",
                "Class level", "Chidamber-Kemerer metrics set", true));
        projectTreeMetrics.add(new MetricsTreeSettingsStub("NOC", "Number Of Children",
                "Class level", "Chidamber-Kemerer metrics set", true));
        projectTreeMetrics.add(new MetricsTreeSettingsStub("RFC", "Response For A Class",
                "Class level", "Chidamber-Kemerer metrics set", true));
        projectTreeMetrics.add(new MetricsTreeSettingsStub("LCOM", "Lack Of Cohesion In Methods",
                "Class level", "Chidamber-Kemerer metrics set", true));
        projectTreeMetrics.add(new MetricsTreeSettingsStub("CBO", "Coupling Between Objects",
                "Class level", "Chidamber-Kemerer metrics set", true));

        //Lorenz-Kidd metrics set
        projectTreeMetrics.add(new MetricsTreeSettingsStub("NOA", "Number Of Attributes",
                "Class level", "Lorenz-Kidd metrics set", true));
        projectTreeMetrics.add(new MetricsTreeSettingsStub("NOO", "Number Of Operations",
                "Class level", "Lorenz-Kidd metrics set", true));
        projectTreeMetrics.add(new MetricsTreeSettingsStub("NOAM", "Number Of Added Methods",
                "Class level", "Lorenz-Kidd metrics set", true));
        projectTreeMetrics.add(new MetricsTreeSettingsStub("NOOM", "Number of Overridden Methods",
                "Class level", "Lorenz-Kidd metrics set", true));

        //Li-Henry metrics set
        projectTreeMetrics.add(new MetricsTreeSettingsStub("SIZE2", "Number Of Attributes And Methods",
                "Class level", "Li-Henry metrics set", true));

        //Methods metrics set
        projectTreeMetrics.add(new MetricsTreeSettingsStub("CND", "Condition Nesting Depth",
                "Method level", "", true));
        projectTreeMetrics.add(new MetricsTreeSettingsStub("LOC", "Lines Of Code",
                "Method level", "", true));
        projectTreeMetrics.add(new MetricsTreeSettingsStub("LND", "Loop Nesting Depth",
                "Method level", "", true));
        projectTreeMetrics.add(new MetricsTreeSettingsStub("CC", "McCabe Cyclomatic Complexity",
                "Method level", "", true));
        projectTreeMetrics.add(new MetricsTreeSettingsStub("NOL", "Number Of Loops",
                "Method level", "", true));
        projectTreeMetrics.add(new MetricsTreeSettingsStub("FANIN", "Fan-In",
                "Method level", "", true));
        projectTreeMetrics.add(new MetricsTreeSettingsStub("FANOUT", "Fan-Out",
                "Method level", "", true));
    }

    public List<MetricsTreeSettingsStub> getProjectTreeMetrics() {
        return new ArrayList<>(projectTreeMetrics);
    }

    public void setProjectTreeMetrics(List<MetricsTreeSettingsStub> metrics) {
        this.projectTreeMetrics.clear();
        this.projectTreeMetrics.addAll(metrics);
    }

    @Override
    public synchronized ProjectMetricsTreeSettings getState() {
        return this;
    }

    @Override
    public synchronized void loadState(@NotNull ProjectMetricsTreeSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "ProjectMetricsTreeSettings";
    }

    public List<MetricsTreeSettingsStub> getMetricsList() {
        Comparator<MetricsTreeSettingsStub> compareByLevelAndName = Comparator
                .comparing(MetricsTreeSettingsStub::getLevel)
                .thenComparing(MetricsTreeSettingsStub::getName);
        return projectTreeMetrics.stream()
                .sorted(compareByLevelAndName)
                .collect(Collectors.toList());
//        return new ArrayList<>(projectTreeMetrics);
    }

    public boolean isNeedToConsiderProjectMetrics() {
        return needToConsiderProjectMetrics;
    }

    public void setNeedToConsiderProjectMetrics(boolean needToConsiderProjectMetrics) {
        this.needToConsiderProjectMetrics = needToConsiderProjectMetrics;
    }

    public boolean isNeedToConsiderPackageMetrics() {
        return needToConsiderPackageMetrics;
    }

    public void setNeedToConsiderPackageMetrics(boolean needToConsiderPackageMetrics) {
        this.needToConsiderPackageMetrics = needToConsiderPackageMetrics;
    }
}
