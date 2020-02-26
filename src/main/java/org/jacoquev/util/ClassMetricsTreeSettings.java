package org.jacoquev.util;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@State(name = "ClassMetricsTreeSettings", storages = {@Storage("class-metrics-tree.xml")})
public final class ClassMetricsTreeSettings implements PersistentStateComponent<ClassMetricsTreeSettings>, ProjectComponent {

    private List<ClassMetricsTreeStub> classTreeMetrics = new ArrayList<>();

    public ClassMetricsTreeSettings() {
        loadInitialValues();
    }

    private void loadInitialValues() {
        //Chidamber-Kemerer metrics set
        classTreeMetrics.add(new ClassMetricsTreeStub("WMC", "Weighted Methods Per Class",
                "Class level", "Chidamber-Kemerer metrics set", true));
        classTreeMetrics.add(new ClassMetricsTreeStub("DIT", "Depth Of Inheritance Tree",
                "Class level", "Chidamber-Kemerer metrics set", true));
        classTreeMetrics.add(new ClassMetricsTreeStub("NOC", "Number Of Children",
                "Class level", "Chidamber-Kemerer metrics set", true));
        classTreeMetrics.add(new ClassMetricsTreeStub("RFC", "Response For A Class",
                "Class level", "Chidamber-Kemerer metrics set", true));
        classTreeMetrics.add(new ClassMetricsTreeStub("LCOM", "Lack Of Cohesion In Methods",
                "Class level", "Chidamber-Kemerer metrics set", true));

        //Lorenz-Kidd metrics set
        classTreeMetrics.add(new ClassMetricsTreeStub("NOA", "Number Of Attributes",
                "Class level", "Lorenz-Kidd metrics set", true));
        classTreeMetrics.add(new ClassMetricsTreeStub("NOO", "Number Of Operations",
                "Class level", "Lorenz-Kidd metrics set", true));
        classTreeMetrics.add(new ClassMetricsTreeStub("NOAM", "Number Of Added Methods",
                "Class level", "Lorenz-Kidd metrics set", true));
        classTreeMetrics.add(new ClassMetricsTreeStub("NOOM", "Number of Overridden Methods",
                "Class level", "Lorenz-Kidd metrics set", true));

        //Li-Henry metrics set
        classTreeMetrics.add(new ClassMetricsTreeStub("SIZE2", "Number Of Attributes And Methods",
                "Class level", "Li-Henry metrics set", true));

        //Methods metrics set
        classTreeMetrics.add(new ClassMetricsTreeStub("CND", "Condition Nesting Depth",
                "Method level", "", true));
        classTreeMetrics.add(new ClassMetricsTreeStub("LOC", "Lines Of Code",
                "Method level", "", true));
        classTreeMetrics.add(new ClassMetricsTreeStub("LND", "Loop Nesting Depth",
                "Method level", "", true));
        classTreeMetrics.add(new ClassMetricsTreeStub("CC", "McCabe Cyclomatic Complexity",
                "Method level", "", true));
        classTreeMetrics.add(new ClassMetricsTreeStub("NOL", "Number Of Loops",
                "Method level", "", true));
        classTreeMetrics.add(new ClassMetricsTreeStub("FANIN", "Fan-In",
                "Method level", "", true));
        classTreeMetrics.add(new ClassMetricsTreeStub("FANOUT", "Fan-Out",
                "Method level", "", true));
    }

    public List<ClassMetricsTreeStub> getClassTreeMetrics() {
        return new ArrayList<>(classTreeMetrics);
    }

    public void setClassTreeMetrics(List<ClassMetricsTreeStub> metrics) {
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

    public List<ClassMetricsTreeStub> getMetricsList() {
        return new ArrayList<>(classTreeMetrics);
    }

    public static class ClassMetricsTreeStub {
        private String name;
        private String description;
        private String level;
        private String set;
        private boolean needToConsider;

        public ClassMetricsTreeStub(String name, String description, String level,
                                    String set, boolean needToConsider) {
            this.name = name;
            this.description = description;
            this.level = level;
            this.set = set;
            this.needToConsider = needToConsider;
        }

        public ClassMetricsTreeStub() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }

        public String getSet() {
            return set;
        }

        public void setSet(String set) {
            this.set = set;
        }

        public boolean isNeedToConsider() {
            return needToConsider;
        }

        public void setNeedToConsider(boolean needToConsider) {
            this.needToConsider = needToConsider;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ClassMetricsTreeStub)) return false;
            ClassMetricsTreeStub that = (ClassMetricsTreeStub) o;
            return isNeedToConsider() == that.isNeedToConsider() &&
                    getName().equals(that.getName()) &&
                    getDescription().equals(that.getDescription()) &&
                    getLevel().equals(that.getLevel()) &&
                    getSet().equals(that.getSet());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getName(), getDescription(), getLevel(), getSet(), isNeedToConsider());
        }
    }

}
