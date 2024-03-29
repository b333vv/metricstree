/*
 * Copyright 2020 b333vv
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.b333vv.metric.ui.tree;

public class MetricsTreeFilter {
    private boolean metricsGroupedByMetricSets;
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
    private boolean lanzaMarinescuMetricsSetVisible;

    public MetricsTreeFilter() {
        this.metricsGroupedByMetricSets = true;
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
        this.lanzaMarinescuMetricsSetVisible = true;
    }

    public boolean isMetricsGroupedByMetricSets() {
        return metricsGroupedByMetricSets;
    }

    public void setMetricsGroupedByMetricSets(boolean metricsGroupedByMetricSets) {
        this.metricsGroupedByMetricSets = metricsGroupedByMetricSets;
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

    public boolean isLanzaMarinescuMetricsSetVisible() {
        return lanzaMarinescuMetricsSetVisible;
    }

    public void setLanzaMarinescuMetricsSetVisible(boolean lanzaMarinescuMetricsSetVisible) {
        this.lanzaMarinescuMetricsSetVisible = lanzaMarinescuMetricsSetVisible;
    }
}
