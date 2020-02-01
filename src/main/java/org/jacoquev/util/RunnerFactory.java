package org.jacoquev.util;

import org.jacoquev.model.metric.meter.method.*;
import org.jacoquev.model.metric.meter.type.*;

public class RunnerFactory {
    public static Runner getProcessor() {
        Runner runner = new Runner();

        runner.registerTypeMeter(new NumberOfChildren());
        runner.registerTypeMeter(new DepthOfInheritanceTree());
        runner.registerTypeMeter(new WeightedMethodCount());
        runner.registerTypeMeter(new ResponseForClass());
        runner.registerTypeMeter(new LackOfCohesionOfMethods());

        runner.registerTypeMeter(new NumberOfAttributes());
        runner.registerTypeMeter(new NumberOfOperations());
        runner.registerTypeMeter(new NumberOfAddedMethods());
        runner.registerTypeMeter(new NumberOfOverriddenMethods());


        runner.registerMethodMeter(new LinesOfCode());
        runner.registerMethodMeter(new McCabeCyclomaticComplexity());
        runner.registerMethodMeter(new LoopNestingDepth());
        runner.registerMethodMeter(new ConditionNestingDepth());
        runner.registerMethodMeter(new NumberOfConditions());
        runner.registerMethodMeter(new NumberOfLoops());


        return runner;
    }
}
