package org.jacoquev.util;

import org.jacoquev.model.metric.meter.type.DepthOfInheritanceTree;
import org.jacoquev.model.metric.meter.type.NumberOfChildren;

public class RunnerFactory {
    public static Runner getProcessor() {
        Runner runner = new Runner();

//        runner.registerTypeCalculator(new RawTotalLinesOfCodeMeter());
//
//        runner.registerTypeCalculator(new NumberOfFieldsMeter());
//
//        runner.registerProjectCalculator(new TotalLinesOfCodeCalculator.ProjectMeter());
//        runner.registerPackageCalculator(new TotalLinesOfCodeCalculator.PackageMeter());
//        runner.registerTypeCalculator(new TotalLinesOfCodeCalculator.TypeMeter());
//        runner.registerMethodCalculator(new TotalLinesOfCodeCalculator.MethodMeter());
//
//        runner.registerMethodCalculator(new CyclomaticComplexityMeter());
//        runner.registerTypeCalculator(new WeightedMethodsMeter());
//
//        runner.registerMethodCalculator(new NumberOfParametersMeter());
//        runner.registerPackageCalculator(new NumberOfClassesMeter());
//
//        runner.registerTypeCalculator(new SpecializationIndexMeter());
//
//        runner.registerPackageCalculator(new RobertMartinCouplingMeter());
//
//        runner.registerMethodCalculator(new NestedBlockDepthMeter());
//        runner.registerTypeCalculator(new LackOfCohesionMethodsMeter());
//
//        runner.registerTypeCalculator(new ClassInheritanceMeter());
//
//        runner.registerTypeCalculator(new MethodAndAttributeInheritanceMeter());
//
//        runner.registerMethodCalculator(new FanMeter());
//        runner.registerTypeCalculator(new LinkMeter());
//        runner.registerMethodCalculator(new McclureMeter());
//
//        runner.registerTypeCalculator(new TypeAggregatorMeter());
//        runner.registerPackageCalculator(new PackageAggregatorMeter());

        runner.registerTypeMeter(new NumberOfChildren());
        runner.registerTypeMeter(new DepthOfInheritanceTree());
        return runner;
    }
}
