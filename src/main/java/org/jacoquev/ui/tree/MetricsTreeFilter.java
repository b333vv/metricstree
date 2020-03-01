package org.jacoquev.ui.tree;

public class MetricsTreeFilter {
    private boolean projectMetricsVisible;
    private boolean packageMetricsVisible;
    private boolean classMetricsVisible;
    private boolean methodMetricsVisible;
    private boolean allowedValueMetricsVisible;
    private boolean disallowedValueMetricsVisible;
    private boolean notSetValueMetricsVisible;
    private boolean notApplicableMetricsVisible;
    private boolean chidamberKemererMetricsSetVisible;
    private boolean lorenzKiddMetricsSetVisible;
    private boolean robertMartinMetricsSetVisible;
    private boolean moodMetricsSetVisible;
    private boolean liHenryMetricsSetVisible;

    public MetricsTreeFilter() {
        this.projectMetricsVisible = true;
        this.packageMetricsVisible = true;
        this.classMetricsVisible = true;
        this.methodMetricsVisible = true;
        this.allowedValueMetricsVisible = true;
        this.disallowedValueMetricsVisible = true;
        this.notSetValueMetricsVisible = true;
        this.notApplicableMetricsVisible = true;
        this.chidamberKemererMetricsSetVisible = true;
        this.lorenzKiddMetricsSetVisible = true;
        this.robertMartinMetricsSetVisible = true;
        this.moodMetricsSetVisible = true;
        this.liHenryMetricsSetVisible = true;
    }

    public boolean isProjectMetricsVisible() {
        return projectMetricsVisible;
    }

    public void setProjectMetricsVisible(boolean projectMetricsVisible) {
        this.projectMetricsVisible = projectMetricsVisible;
    }

    public boolean isPackageMetricsVisible() {
        return packageMetricsVisible;
    }

    public void setPackageMetricsVisible(boolean packageMetricsVisible) {
        this.packageMetricsVisible = packageMetricsVisible;
    }

    public boolean isClassMetricsVisible() {
        return classMetricsVisible;
    }

    public void setClassMetricsVisible(boolean classMetricsVisible) {
        this.classMetricsVisible = classMetricsVisible;
    }

    public boolean isMethodMetricsVisible() {
        return methodMetricsVisible;
    }

    public void setMethodMetricsVisible(boolean methodMetricsVisible) {
        this.methodMetricsVisible = methodMetricsVisible;
    }

    public boolean isAllowedValueMetricsVisible() {
        return allowedValueMetricsVisible;
    }

    public void setAllowedValueMetricsVisible(boolean allowedValueMetricsVisible) {
        this.allowedValueMetricsVisible = allowedValueMetricsVisible;
    }

    public boolean isDisallowedValueMetricsVisible() {
        return disallowedValueMetricsVisible;
    }

    public void setDisallowedValueMetricsVisible(boolean disallowedValueMetricsVisible) {
        this.disallowedValueMetricsVisible = disallowedValueMetricsVisible;
    }

    public boolean isNotSetValueMetricsVisible() {
        return notSetValueMetricsVisible;
    }

    public void setNotSetValueMetricsVisible(boolean notSetValueMetricsVisible) {
        this.notSetValueMetricsVisible = notSetValueMetricsVisible;
    }

    public boolean isNotApplicableMetricsVisible() {
        return notApplicableMetricsVisible;
    }

    public void setNotApplicableMetricsVisible(boolean notApplicableMetricsVisible) {
        this.notApplicableMetricsVisible = notApplicableMetricsVisible;
    }

    public boolean isChidamberKemererMetricsSetVisible() {
        return chidamberKemererMetricsSetVisible;
    }

    public void setChidamberKemererMetricsSetVisible(boolean chidamberKemererMetricsSetVisible) {
        this.chidamberKemererMetricsSetVisible = chidamberKemererMetricsSetVisible;
    }

    public boolean isLorenzKiddMetricsSetVisible() {
        return lorenzKiddMetricsSetVisible;
    }

    public void setLorenzKiddMetricsSetVisible(boolean lorenzKiddMetricsSetVisible) {
        this.lorenzKiddMetricsSetVisible = lorenzKiddMetricsSetVisible;
    }

    public boolean isRobertMartinMetricsSetVisible() {
        return robertMartinMetricsSetVisible;
    }

    public void setRobertMartinMetricsSetVisible(boolean robertMartinMetricsSetVisible) {
        this.robertMartinMetricsSetVisible = robertMartinMetricsSetVisible;
    }

    public boolean isMoodMetricsSetVisible() {
        return moodMetricsSetVisible;
    }

    public void setMoodMetricsSetVisible(boolean moodMetricsSetVisible) {
        this.moodMetricsSetVisible = moodMetricsSetVisible;
    }

    public boolean isLiHenryMetricsSetVisible() {
        return liHenryMetricsSetVisible;
    }

    public void setLiHenryMetricsSetVisible(boolean liHenryMetricsSetVisible) {
        this.liHenryMetricsSetVisible = liHenryMetricsSetVisible;
    }
}
