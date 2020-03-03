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

@State(name = "ClassMetricsTreeSettings", storages = {@Storage("class-metrics-tree.xml")})
public final class ClassMetricsTreeSettings implements PersistentStateComponent<ClassMetricsTreeSettings>, ProjectComponent {

    private List<MetricsTreeSettingsStub> classTreeMetrics = new ArrayList<>();

    public ClassMetricsTreeSettings() {
        loadInitialValues();
    }

    private void loadInitialValues() {
        //Chidamber-Kemerer metrics set
        classTreeMetrics.add(new MetricsTreeSettingsStub("WMC", "Weighted Methods Per Class",
                "Class level", "Chidamber-Kemerer metrics set", true));
        classTreeMetrics.add(new MetricsTreeSettingsStub("DIT", "Depth Of Inheritance Tree",
                "Class level", "Chidamber-Kemerer metrics set", true));
        classTreeMetrics.add(new MetricsTreeSettingsStub("NOC", "Number Of Children",
                "Class level", "Chidamber-Kemerer metrics set", true));
        classTreeMetrics.add(new MetricsTreeSettingsStub("RFC", "Response For A Class",
                "Class level", "Chidamber-Kemerer metrics set", true));
        classTreeMetrics.add(new MetricsTreeSettingsStub("LCOM", "Lack Of Cohesion In Methods",
                "Class level", "Chidamber-Kemerer metrics set", true));

        //Lorenz-Kidd metrics set
        classTreeMetrics.add(new MetricsTreeSettingsStub("NOA", "Number Of Attributes",
                "Class level", "Lorenz-Kidd metrics set", true));
        classTreeMetrics.add(new MetricsTreeSettingsStub("NOO", "Number Of Operations",
                "Class level", "Lorenz-Kidd metrics set", true));
        classTreeMetrics.add(new MetricsTreeSettingsStub("NOAM", "Number Of Added Methods",
                "Class level", "Lorenz-Kidd metrics set", true));
        classTreeMetrics.add(new MetricsTreeSettingsStub("NOOM", "Number of Overridden Methods",
                "Class level", "Lorenz-Kidd metrics set", true));

        //Li-Henry metrics set
        classTreeMetrics.add(new MetricsTreeSettingsStub("SIZE2", "Number Of Attributes And Methods",
                "Class level", "Li-Henry metrics set", true));
        classTreeMetrics.add(new MetricsTreeSettingsStub("NOM", "Number Of Methods",
                "Class level", "Li-Henry metrics set", true));
        classTreeMetrics.add(new MetricsTreeSettingsStub("MPC", "Message Passing Coupling",
                "Class level", "Li-Henry metrics set", true));
        classTreeMetrics.add(new MetricsTreeSettingsStub("DAC", "Data Abstraction Coupling",
                "Class level", "Li-Henry metrics set", true));

        //Methods metrics set
        classTreeMetrics.add(new MetricsTreeSettingsStub("CND", "Condition Nesting Depth",
                "Method level", "", true));
        classTreeMetrics.add(new MetricsTreeSettingsStub("LOC", "Lines Of Code",
                "Method level", "", true));
        classTreeMetrics.add(new MetricsTreeSettingsStub("LND", "Loop Nesting Depth",
                "Method level", "", true));
        classTreeMetrics.add(new MetricsTreeSettingsStub("CC", "McCabe Cyclomatic Complexity",
                "Method level", "", true));
        classTreeMetrics.add(new MetricsTreeSettingsStub("NOL", "Number Of Loops",
                "Method level", "", true));
//        classTreeMetrics.add(new MetricsTreeSettingsStub("FANIN", "Fan-In",
//                "Method level", "", true));
//        classTreeMetrics.add(new MetricsTreeSettingsStub("FANOUT", "Fan-Out",
//                "Method level", "", true));
    }

    public List<MetricsTreeSettingsStub> getClassTreeMetrics() {
        return new ArrayList<>(classTreeMetrics);
    }

    public void setClassTreeMetrics(List<MetricsTreeSettingsStub> metrics) {
        this.classTreeMetrics.clear();
        this.classTreeMetrics.addAll(metrics);
    }

    @Override
    public synchronized ClassMetricsTreeSettings getState() {
        return this;
    }

    @Override
    public synchronized void loadState(@NotNull ClassMetricsTreeSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "ClassMetricsTreeSettings";
    }

    public List<MetricsTreeSettingsStub> getMetricsList() {
        Comparator<MetricsTreeSettingsStub> compareByLevelAndName = Comparator
                .comparing(MetricsTreeSettingsStub::getLevel)
                .thenComparing(MetricsTreeSettingsStub::getName);
        return classTreeMetrics.stream()
                .sorted(compareByLevelAndName)
                .collect(Collectors.toList());
    }

}
