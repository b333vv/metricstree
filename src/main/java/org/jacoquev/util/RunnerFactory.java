package org.jacoquev.util;

import org.jacoquev.model.metric.meter.type.DepthOfInheritanceTree;
import org.jacoquev.model.metric.meter.type.NumberOfChildren;
import org.jacoquev.model.metric.meter.type.WeightedMethodCount;

public class RunnerFactory {
    public static Runner getProcessor() {
        Runner runner = new Runner();

        runner.registerTypeMeter(new NumberOfChildren());
        runner.registerTypeMeter(new DepthOfInheritanceTree());
        runner.registerTypeMeter(new WeightedMethodCount());
        return runner;
    }
}
