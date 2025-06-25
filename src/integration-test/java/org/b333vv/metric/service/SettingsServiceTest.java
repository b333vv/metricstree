package org.b333vv.metric.service;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.b333vv.metric.util.SettingsService;
import org.b333vv.metric.ui.settings.composition.ClassMetricsTreeSettings;
import org.b333vv.metric.ui.settings.fitnessfunction.ClassLevelFitnessFunctions;
import org.b333vv.metric.ui.settings.fitnessfunction.PackageLevelFitnessFunctions;
import org.b333vv.metric.ui.settings.other.OtherSettings;
import org.b333vv.metric.ui.settings.ranges.BasicMetricsValidRangesSettings;
import org.b333vv.metric.ui.settings.ranges.DerivativeMetricsValidRangesSettings;

public class SettingsServiceTest extends BasePlatformTestCase {

    public void testGettersReturnNonNullSettings() {
        SettingsService settingsService = getProject().getService(SettingsService.class);
        assertNotNull(settingsService);

        BasicMetricsValidRangesSettings basicMetricsSettings = settingsService.getBasicMetricsSettings();
        assertNotNull(basicMetricsSettings);

        DerivativeMetricsValidRangesSettings derivativeMetricsSettings = settingsService.getDerivativeMetricsSettings();
        assertNotNull(derivativeMetricsSettings);

        ClassMetricsTreeSettings classMetricsTreeSettings = settingsService.getClassMetricsTreeSettings();
        assertNotNull(classMetricsTreeSettings);

        OtherSettings otherSettings = settingsService.getOtherSettings();
        assertNotNull(otherSettings);

        ClassLevelFitnessFunctions classLevelFitnessFunctions = settingsService.getClassLevelFitnessFunctions();
        assertNotNull(classLevelFitnessFunctions);

        PackageLevelFitnessFunctions packageLevelFitnessFunctions = settingsService.getPackageLevelFitnessFunctions();
        assertNotNull(packageLevelFitnessFunctions);
    }

    public void testSettingsModificationIsPersisted() {
        SettingsService settingsService1 = getProject().getService(SettingsService.class);
        OtherSettings otherSettings1 = settingsService1.getOtherSettings();
        assertTrue(otherSettings1.isProjectMetricsStampStored()); // Default value is true

        otherSettings1.setProjectMetricsStampStored(false);

        SettingsService settingsService2 = getProject().getService(SettingsService.class);
        OtherSettings otherSettings2 = settingsService2.getOtherSettings();
        assertFalse(otherSettings2.isProjectMetricsStampStored());

        // Reset to default for other tests
        otherSettings2.setProjectMetricsStampStored(true);
    }
}
