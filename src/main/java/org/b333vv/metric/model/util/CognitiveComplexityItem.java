package org.b333vv.metric.model.util;

public class CognitiveComplexityItem {
    private final int complexity;
    private final int nesting;
    private final CogntiveComplexityElementType type;

    public CognitiveComplexityItem(int complexity, int nesting, CogntiveComplexityElementType type) {
        this.complexity = complexity;
        this.nesting = nesting;
        this.type = type;
    }

    public int getComplexity() {
        return complexity;
    }

    public int getNesting() {
        return nesting;
    }

    public CogntiveComplexityElementType getType() {
        return type;
    }
}
