package org.jacoquev.ui.tree;

public class MetricsTreeFilter {
    private boolean isProjectMetricsVisible;
    private boolean isPackageMetricsVisible;
    private boolean isClassMetricsVisible;
    private boolean isMethodMetricsVisible;
    private boolean isAllowedValueMetricsVisible;
    private boolean isDisallowedValueMetricsVisible;
    private boolean isNotSetValueMetricsVisible;

    public MetricsTreeFilter() {
        this.isProjectMetricsVisible = true;
        this.isPackageMetricsVisible = true;
        this.isClassMetricsVisible = true;
        this.isMethodMetricsVisible = true;
        this.isAllowedValueMetricsVisible = true;
        this.isDisallowedValueMetricsVisible = true;
        this.isNotSetValueMetricsVisible = true;
    }

    public MetricsTreeFilter(boolean isProjectMetricsVisible,
                             boolean isPackageMetricsVisible,
                             boolean isClassMetricsVisible,
                             boolean isMethodMetricsVisible,
                             boolean isAllowedValueMetricsVisible,
                             boolean isDisallowedValueMetricsVisible,
                             boolean isNotSetValueMetricsVisible) {
        this.isProjectMetricsVisible = isProjectMetricsVisible;
        this.isPackageMetricsVisible = isPackageMetricsVisible;
        this.isClassMetricsVisible = isClassMetricsVisible;
        this.isMethodMetricsVisible = isMethodMetricsVisible;
        this.isAllowedValueMetricsVisible = isAllowedValueMetricsVisible;
        this.isDisallowedValueMetricsVisible = isDisallowedValueMetricsVisible;
        this.isNotSetValueMetricsVisible = isNotSetValueMetricsVisible;
    }

    public boolean isProjectMetricsVisible() {
        return isProjectMetricsVisible;
    }

    public void setProjectMetricsVisible(boolean projectMetricsVisible) {
        isProjectMetricsVisible = projectMetricsVisible;
    }

    public boolean isPackageMetricsVisible() {
        return isPackageMetricsVisible;
    }

    public void setPackageMetricsVisible(boolean packageMetricsVisible) {
        isPackageMetricsVisible = packageMetricsVisible;
    }

    public boolean isClassMetricsVisible() {
        return isClassMetricsVisible;
    }

    public void setClassMetricsVisible(boolean classMetricsVisible) {
        isClassMetricsVisible = classMetricsVisible;
    }

    public boolean isMethodMetricsVisible() {
        return isMethodMetricsVisible;
    }

    public void setMethodMetricsVisible(boolean methodMetricsVisible) {
        isMethodMetricsVisible = methodMetricsVisible;
    }

    public boolean isAllowedValueMetricsVisible() {
        return isAllowedValueMetricsVisible;
    }

    public void setAllowedValueMetricsVisible(boolean allowedValueMetricsVisible) {
        isAllowedValueMetricsVisible = allowedValueMetricsVisible;
    }

    public boolean isDisallowedValueMetricsVisible() {
        return isDisallowedValueMetricsVisible;
    }

    public void setDisallowedValueMetricsVisible(boolean disallowedValueMetricsVisible) {
        isDisallowedValueMetricsVisible = disallowedValueMetricsVisible;
    }

    public boolean isNotSetValueMetricsVisible() {
        return isNotSetValueMetricsVisible;
    }

    public void setNotSetValueMetricsVisible(boolean notSetValueMetricsVisible) {
        isNotSetValueMetricsVisible = notSetValueMetricsVisible;
    }
}
