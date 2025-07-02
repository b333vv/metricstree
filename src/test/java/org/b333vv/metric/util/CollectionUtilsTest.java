package org.b333vv.metric.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CollectionUtilsTest {

    @Test
    void testSortByValueReversed() {
        // 1. Create a HashMap<String, Integer>
        Map<String, Integer> originalMap = new HashMap<>();

        // 2. Add some entries to the map with varying integer values
        originalMap.put("alpha", 5);
        originalMap.put("beta", 10);
        originalMap.put("gamma", 2);
        originalMap.put("delta", 10); // For testing stability with equal values
        originalMap.put("epsilon", 7);

        // Create a copy for checking non-modification
        Map<String, Integer> copyOfOriginalMap = new HashMap<>(originalMap);

        // 3. Call CollectionUtils.sortByValueReversed with this map
        Map<String, Integer> sortedMap = CollectionUtils.sortByValueReversed(originalMap);

        // 4. Assert that the returned map has the entries sorted in descending order of their values
        assertNotSame(originalMap, sortedMap, "The returned map should be a new instance.");

        List<Map.Entry<String, Integer>> sortedEntries = sortedMap.entrySet().stream().collect(Collectors.toList());

        assertEquals(10, sortedEntries.get(0).getValue(), "First entry value should be 10.");
        assertEquals(10, sortedEntries.get(1).getValue(), "Second entry value should be 10.");
        // For entries with the same value, their relative order is not strictly defined by sortByValueReversed,
        // so we check if the keys are among the expected ones.
        List<String> keysForValue10 = Arrays.asList(sortedEntries.get(0).getKey(), sortedEntries.get(1).getKey());
        assertTrue(keysForValue10.contains("beta"), "Keys for value 10 should include 'beta'");
        assertTrue(keysForValue10.contains("delta"), "Keys for value 10 should include 'delta'");


        assertEquals(7, sortedEntries.get(2).getValue(), "Third entry value should be 7.");
        assertEquals("epsilon", sortedEntries.get(2).getKey(), "Third entry key should be 'epsilon'.");

        assertEquals(5, sortedEntries.get(3).getValue(), "Fourth entry value should be 5.");
        assertEquals("alpha", sortedEntries.get(3).getKey(), "Fourth entry key should be 'alpha'.");

        assertEquals(2, sortedEntries.get(4).getValue(), "Fifth entry value should be 2.");
        assertEquals("gamma", sortedEntries.get(4).getKey(), "Fifth entry key should be 'gamma'.");

        assertEquals(originalMap.size(), sortedMap.size(), "Sorted map should have the same size as the original.");

        // 5. Assert that the original map is not modified
        assertEquals(copyOfOriginalMap, originalMap, "The original map should not be modified.");
    }
}
