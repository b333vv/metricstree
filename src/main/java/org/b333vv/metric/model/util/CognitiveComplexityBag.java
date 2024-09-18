package org.b333vv.metric.model.util;

import java.util.ArrayList;
import java.util.List;

public class CognitiveComplexityBag {
    private int nesting = 0;
    private final List<CognitiveComplexityItem> points = new ArrayList<>();

    public void decreaseNesting() {
        if (nesting > 0) nesting--;
    }

    public void increaseNesting() {
        nesting++;
    }

    public void increaseComplexity(CogntiveComplexityElementType type) {
        increaseComplexity(1, type);
    }

    public void increaseComplexity(int amount, CogntiveComplexityElementType type) {
        points.add(new CognitiveComplexityItem(amount, nesting, type));
    }

    public void increaseComplexityAndNesting(CogntiveComplexityElementType type) {
        points.add(new CognitiveComplexityItem(1 + nesting, nesting++, type));
    }

    public int getComplexity() {
        return points.stream().mapToInt(CognitiveComplexityItem::getComplexity).sum();
    }

    public int getNesting() {
        return nesting;
    }

    public List<CognitiveComplexityItem> getPoints() {
        return List.copyOf(points);
    }
}


