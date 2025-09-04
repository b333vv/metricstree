/*
 * Copyright 2025 b333vv
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

package org.b333vv.metric.util;

import org.b333vv.metric.ui.settings.other.CalculationEngine;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;

/**
 * Centralized configuration loader for the Metricstree plugin.
 *
 * Reads configuration from classpath resource META-INF/metricstree.properties
 * and exposes typed getters for configuration values.
 */
public final class MetricstreeConfig {
    private static final String CONFIG_RESOURCE = "META-INF/metricstree.properties";
    private static final String KEY_CALCULATION_ENGINE = "calculation.engine";

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = MetricstreeConfig.class.getClassLoader().getResourceAsStream(CONFIG_RESOURCE)) {
            if (in != null) {
                PROPS.load(in);
            }
        } catch (IOException ignored) {
            // Fallbacks will be used
        }
    }

    private MetricstreeConfig() {}

    public static CalculationEngine getCalculationEngine() {
        String value = PROPS.getProperty(KEY_CALCULATION_ENGINE, "PSI");
        try {
            return CalculationEngine.valueOf(Objects.requireNonNull(value).trim().toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            return CalculationEngine.PSI;
        }
    }
}
