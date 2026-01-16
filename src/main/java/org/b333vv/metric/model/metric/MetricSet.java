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

package org.b333vv.metric.model.metric;

public enum MetricSet {
    UNDEFINED("", MetricLevel.UNDEFINED),
    HALSTEAD_METHOD("Halstead Metric Set", MetricLevel.METHOD),
    HALSTEAD_CLASS("Halstead Metric Set", MetricLevel.CLASS),
    HALSTEAD_PACKAGE("Halstead Metric Set", MetricLevel.PACKAGE),
    HALSTEAD_PROJECT("Halstead Metric Set", MetricLevel.PROJECT),
    CHIDAMBER_KEMERER("Chidamber-Kemerer Metric Set", MetricLevel.CLASS),
    LORENZ_KIDD("Lorenz-Kidd Metric Set", MetricLevel.CLASS),
    LI_HENRY("Li-Henry Metric Set", MetricLevel.CLASS),
    LANZA_MARINESCU("Lanza-Marinescu Metric Set", MetricLevel.CLASS),
    BIEMAN_KANG("Bieman-Kang Metric Set", MetricLevel.CLASS),
    CLEMENS_LEE("Chr. Clemens Lee Metric Set", MetricLevel.CLASS),
    R_MARTIN("Robert C. Martin Metric Set", MetricLevel.PACKAGE),
    MOOD("MOOD Metric Set", MetricLevel.PROJECT),
    STATISTIC("Statistic", MetricLevel.PROJECT_PACKAGE),
    QMOOD("QMOOD Quality Attributes Set", MetricLevel.PROJECT),

    CAMPBELL_METHOD("G. Ann Campbell Metric Set", MetricLevel.METHOD),
    CAMPBELL_CLASS("G. Ann Campbell Metric Set", MetricLevel.CLASS),

    //MAINTAINABILITY_INDEX
    METHOD_MAINTAINABILITY_INDEX("Maintainability Index", MetricLevel.METHOD),
    CLASS_MAINTAINABILITY_INDEX("Maintainability Index", MetricLevel.CLASS),
    PACKAGE_MAINTAINABILITY_INDEX("Maintainability Index", MetricLevel.PACKAGE),
    PROJECT_MAINTAINABILITY_INDEX("Maintainability Index", MetricLevel.PROJECT);

    private final String set;
    private final MetricLevel level;
    private final String url;

    MetricSet(String set, MetricLevel level) {
        this.set = set;
        this.level = level;
        this.url = "/html/" + name() + ".html";
    }

    public String set() {
        return set;
    }
    public MetricLevel level() {
        return level;
    }
    public String url() {
        return url;
    }
}
