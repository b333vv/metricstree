package org.b333vv.metric.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UIStateServiceTest {
    private UIStateService uiStateService;

    @BeforeEach
    void setUp() {
        uiStateService = new UIStateService();
    }

    @Test
    void testDefaultValues() {
        assertTrue(uiStateService.isProjectAutoScrollable());
        assertTrue(uiStateService.isProfileAutoScrollable());
        assertTrue(uiStateService.isClassMetricsTreeExists());
        assertFalse(uiStateService.isProjectTreeActive());
        assertFalse(uiStateService.isClassMetricsValuesEvolutionCalculationPerforming());
        assertFalse(uiStateService.isClassMetricsValuesEvolutionAdded());
    }

    @Test
    void testProjectAutoScrollable() {
        uiStateService.setProjectAutoScrollable(false);
        assertFalse(uiStateService.isProjectAutoScrollable());
        uiStateService.setProjectAutoScrollable(true);
        assertTrue(uiStateService.isProjectAutoScrollable());
    }

    @Test
    void testProfileAutoScrollable() {
        uiStateService.setProfileAutoScrollable(false);
        assertFalse(uiStateService.isProfileAutoScrollable());
        uiStateService.setProfileAutoScrollable(true);
        assertTrue(uiStateService.isProfileAutoScrollable());
    }

    @Test
    void testClassMetricsTreeExists() {
        uiStateService.setClassMetricsTreeExists(false);
        assertFalse(uiStateService.isClassMetricsTreeExists());
        uiStateService.setClassMetricsTreeExists(true);
        assertTrue(uiStateService.isClassMetricsTreeExists());
    }

    @Test
    void testProjectTreeActive() {
        uiStateService.setProjectTreeActive(true);
        assertTrue(uiStateService.isProjectTreeActive());
        uiStateService.setProjectTreeActive(false);
        assertFalse(uiStateService.isProjectTreeActive());
    }

    @Test
    void testClassMetricsValuesEvolutionCalculationPerforming() {
        uiStateService.setClassMetricsValuesEvolutionCalculationPerforming(true);
        assertTrue(uiStateService.isClassMetricsValuesEvolutionCalculationPerforming());
        uiStateService.setClassMetricsValuesEvolutionCalculationPerforming(false);
        assertFalse(uiStateService.isClassMetricsValuesEvolutionCalculationPerforming());
    }

    @Test
    void testClassMetricsValuesEvolutionAdded() {
        uiStateService.setClassMetricsValuesEvolutionAdded(true);
        assertTrue(uiStateService.isClassMetricsValuesEvolutionAdded());
        uiStateService.setClassMetricsValuesEvolutionAdded(false);
        assertFalse(uiStateService.isClassMetricsValuesEvolutionAdded());
    }
}
